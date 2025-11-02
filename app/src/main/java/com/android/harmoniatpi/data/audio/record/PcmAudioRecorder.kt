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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import javax.inject.Inject

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
}