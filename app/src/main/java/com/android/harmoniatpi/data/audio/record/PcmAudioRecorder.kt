package com.android.harmoniatpi.data.audio.record

import android.Manifest
import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import android.util.Log
import androidx.annotation.RequiresPermission
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory
import be.tarsos.dsp.writer.WriterProcessor
import com.android.harmoniatpi.domain.interfaces.AudioRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import javax.inject.Inject
import kotlin.math.abs

private const val SAMPLE_RATE = 44100
private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
private const val BUFFER_SIZE_IN_BYTES = 4096

val TARSOS_FORMAT = TarsosDSPAudioFormat(
    SAMPLE_RATE.toFloat(),
    16,
    1,
    true,
    false
)

class PcmAudioRecorder @Inject constructor() : AudioRecorder {

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var outputFilePath: String? = null
    private val liveWaveform = MutableSharedFlow<List<Float>>(replay = 1)

    override fun setOutputFile(path: String) {
        this.outputFilePath = path
    }

    @SuppressLint("MissingPermission")
    override fun startRecording(audioSource: Int): Result<Unit> {
        return runCatching {
            if (recordingJob?.isActive == true) {
                Log.w("PcmAudioRecorder", "Grabación ya en progreso.")
                return@runCatching
            }

            val path = outputFilePath
                ?: throw IllegalStateException("Ruta de archivo no establecida. Llamar a setOutputFile() primero.")

            // 1. Inicializar AudioRecord
            audioRecord = AudioRecord(
                audioSource,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE_IN_BYTES
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                throw IllegalStateException("AudioRecord no pudo inicializarse. ¿Micrófono en uso?")
            }

            recordingJob = CoroutineScope(Dispatchers.IO).launch {
                Log.i("PcmAudioRecorder", "Grabación NATIVA iniciada: $path")
                audioRecord?.startRecording()

                FileOutputStream(File(path)).use { outputStream ->
                    val buffer = ByteArray(BUFFER_SIZE_IN_BYTES)
                    while (isActive) {
                        val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                        if (read > 0) {
                            outputStream.write(buffer, 0, read)

                            val shortArray = byteArrayToShortArray(buffer.copyOf(read))
                            val waveformChunk = pcmShortArrayToNormalizedWaveform(shortArray)
                            liveWaveform.tryEmit(waveformChunk)
                        }
                    }
                }
            }
        }
    }

    override fun stopRecording(): Result<Unit> {
        return runCatching {
            if (recordingJob?.isActive == true) {
                recordingJob?.cancel()
            }

            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null

            outputFilePath = null
            Log.i("PcmAudioRecorder", "Grabación NATIVA detenida.")
        }
    }

    override fun getLiveWaveform(): SharedFlow<List<Float>> = liveWaveform.asSharedFlow()

    private fun byteArrayToShortArray(pcmBytes: ByteArray): ShortArray {
        val shorts = ShortArray(pcmBytes.size / 2)
        for (i in shorts.indices) {
            val low = pcmBytes[i * 2].toInt() and 0xFF
            val high = pcmBytes[i * 2 + 1].toInt()
            shorts[i] = ((high shl 8) or low).toShort()
        }
        return shorts
    }

    private fun pcmShortArrayToNormalizedWaveform(
        pcm: ShortArray,
        samplesPerPeak: Int = 256 // Cada cuantos samples se tomará un pico
    ): List<Float> {
        if (pcm.isEmpty()) return emptyList()

        val numWindows = pcm.size / samplesPerPeak
        val peaks = mutableListOf<Float>()

        for (i in 0 until numWindows) {
            val start = i * samplesPerPeak
            val end = (start + samplesPerPeak).coerceAtMost(pcm.size)
            if (start >= end) continue

            // Tomamos una "ventana" de la señal de audio
            val window = pcm.slice(start until end)

            // Encontramos el valor más alto y más bajo en esa ventana
            peaks.add(window.maxOrNull()?.toFloat() ?: 0f) // Pico máximo
            peaks.add(window.minOrNull()?.toFloat() ?: 0f) // Pico mínimo
        }

        // Normalizamos la lista de picos para que estén en el rango de -1.0 a 1.0
        val maxAbsValue = peaks.maxOfOrNull { abs(it) }
        if (maxAbsValue == null || maxAbsValue == 0f) return peaks.map { 0f }

        return peaks.map { it / maxAbsValue }
    }
}