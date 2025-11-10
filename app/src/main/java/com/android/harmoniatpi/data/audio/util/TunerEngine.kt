package com.android.harmoniatpi.data.audio.util


import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class TunerEngine @Inject constructor() {

    private val TAG = "TunerEngine"

    private var audioRecord: AudioRecord? = null
    private var tunerThread: Thread? = null
    private var isTunerRunning = false

    private val TARSOS_TUNER_FORMAT = TarsosDSPAudioFormat(
        22050f,
        16,
        1,
        true,
        false
    )

    private val _tunerNote = MutableStateFlow("")
    val tunerNoteFlow = _tunerNote.asStateFlow()

    private val noteNames = arrayOf("Do", "Do#", "Re", "Re#", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "La#", "Si")

    @SuppressLint("MissingPermission")
    fun start() {
        if (isTunerRunning) return
        isTunerRunning = true
        Log.d(TAG, "Iniciando Afinador...")

        val sampleRate = TARSOS_TUNER_FORMAT.sampleRate.toInt()
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT

        val minBufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        if (minBufferSizeInBytes == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Parámetros de AudioRecord no soportados por el dispositivo.")
            isTunerRunning = false
            return
        }

        val tarsosBufferSize = minBufferSizeInBytes / TARSOS_TUNER_FORMAT.frameSize

        val pitchDetectionHandler = PitchDetectionHandler { res, _ ->
            if (res.pitch > 0) {
                _tunerNote.value = pitchToNote(res.pitch)
            }
        }

        val pitchProcessor = PitchProcessor(
            PitchEstimationAlgorithm.YIN,
            sampleRate.toFloat(),
            tarsosBufferSize,
            pitchDetectionHandler
        )

        val converter = TarsosDSPAudioFloatConverter.getConverter(TARSOS_TUNER_FORMAT)
        val audioEvent = AudioEvent(TARSOS_TUNER_FORMAT)
        val byteBuffer = ByteArray(minBufferSizeInBytes)
        val floatBuffer = FloatArray(tarsosBufferSize)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                minBufferSizeInBytes
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord no se pudo inicializar. Micrófono en uso?")
                isTunerRunning = false
                return
            }

            audioRecord?.startRecording()

            tunerThread = Thread {
                Log.d(TAG, "Hilo del afinador iniciado.")
                while (isTunerRunning && tunerThread?.isInterrupted == false) {
                    val bytesRead = audioRecord!!.read(byteBuffer, 0, minBufferSizeInBytes)
                    if (bytesRead > 0) {
                        audioEvent.setBytesProcessing(bytesRead)
                        converter.toFloatArray(byteBuffer, floatBuffer)
                        audioEvent.setFloatBuffer(floatBuffer)
                        pitchProcessor.process(audioEvent)
                    } else if (bytesRead < 0) {
                        Log.e(TAG, "Error al leer de AudioRecord: $bytesRead")
                        isTunerRunning = false
                    }
                }
                Log.d(TAG, "Hilo del afinador detenido.")
            }
            tunerThread?.start()

        } catch (e: Exception) {
            Log.e(TAG, "Error al iniciar AudioRecord del afinador", e)
            isTunerRunning = false
        }
    }

    fun stop() {
        if (!isTunerRunning) return

        isTunerRunning = false
        tunerThread?.interrupt()
        tunerThread = null

        try {
            if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                audioRecord?.stop()
            }
            audioRecord?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error al detener/liberar AudioRecord", e)
        }

        audioRecord = null
        _tunerNote.value = ""
        Log.d(TAG, "Afinador detenido y liberado.")
    }

    private fun pitchToNote(pitch: Float): String {
        if (pitch <= 0) return "--"

        val noteNum = 12 * (Math.log(pitch / 440.0) / Math.log(2.0))
        val roundedNote = (noteNum + 69).roundToInt()

        val noteIndex = (roundedNote % 12)
        if (noteIndex < 0 || noteIndex >= noteNames.size) return "--"

        return noteNames[noteIndex]
    }
}