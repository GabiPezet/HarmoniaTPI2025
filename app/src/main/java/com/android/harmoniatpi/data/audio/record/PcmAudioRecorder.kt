package com.android.harmoniatpi.data.audio.record

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import android.util.Log
import androidx.annotation.RequiresPermission
import com.android.harmoniatpi.domain.interfaces.AudioRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * Utiliza AudioRecord para la grabación de audio en archivos .pcm.
 * **La grabación en el archivo se realiza en un hilo separado**.
 */
class PcmAudioRecorder @Inject constructor() : AudioRecorder {
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var outputFile: File? = null
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize: Int
        get() = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun setOutputFile(path: String) {
        outputFile = File(path)
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun startRecording(audioSource: Int): Result<Unit> {
        Log.i(TAG, "Starting recording. Path: ${outputFile?.path}")
        if (recordingJob != null) {
            Log.w(TAG, "Recording already in progress")
            return Result.failure(IllegalStateException("Recording already in progress"))
        }

        audioRecord = AudioRecord(
            audioSource,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            audioRecord = null
            return Result.failure(IllegalStateException("Error initializing AudioRecord"))
        }

        try {
            val audioSessionId = audioRecord!!.audioSessionId

            if (AutomaticGainControl.isAvailable()) {
                val agc = AutomaticGainControl.create(audioSessionId)
                if (agc != null) {
                    agc.enabled = false
                    Log.i(TAG, "Automatic Gain Control (AGC) deshabilitado.")
                }
            }

            if (NoiseSuppressor.isAvailable()) {
                val ns = NoiseSuppressor.create(audioSessionId)
                if (ns != null) {
                    ns.enabled = false
                    Log.i(TAG, "Noise Suppressor (NS) deshabilitado.")
                }
            }

            //cancelador de eco para que la pista de fondo quede sin registrar
            if (AcousticEchoCanceler.isAvailable()) {
                val aec = AcousticEchoCanceler.create(audioSessionId)
                if (aec != null) {
                    aec.enabled = true
                    Log.i(TAG, "Acoustic Echo Canceler (AEC) habilitado.")
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error al configurar los efectos de audio (AGC/NS/AEC)", e)
        }


        audioRecord?.startRecording()
        Log.i(TAG, "Recording started")

        recordingJob = scope.launch {
            val buffer = ByteArray(bufferSize)
            try {
                outputFile?.let { file ->
                    FileOutputStream(file).use { fos ->
                        while (isActive && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                            val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                            if (read > 0) {
                                fos.write(buffer, 0, read)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during recording", e)
            }
        }
        return Result.success(Unit)
    }

    override fun stopRecording(): Result<Unit> =
        try {
            recordingJob?.cancel()
            recordingJob = null
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            Log.i(TAG, "Recording stopped")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error during stopping recording", e)
            Result.failure(e)
        }

    companion object {
        private const val TAG = "AndroidAudioRecorder"
    }
}