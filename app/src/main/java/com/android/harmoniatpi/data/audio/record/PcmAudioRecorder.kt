package com.android.harmoniatpi.data.audio.record

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class PcmAudioRecorder @Inject constructor() : AudioRecorder {
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null

    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize: Int
        get() = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun startRecording(outputFile: File): Result<Unit> {
        if (recordingJob != null) {
            Log.w(TAG, "Recording already in progress")
            return Result.failure(IllegalStateException("Recording already in progress"))
        }

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            audioRecord = null
            return Result.failure(IllegalStateException("Error initializing AudioRecord"))
        }

        audioRecord?.startRecording()
        Log.i(TAG, "Recording started")

        recordingJob = scope.launch {
            val buffer = ByteArray(bufferSize)
            try {
                FileOutputStream(outputFile).use { fos ->
                    while (isActive && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                        val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                        if (read > 0) {
                            fos.write(buffer, 0, read)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during recording", e)
            }
        }
        return Result.success(Unit)
    }

    override fun stopRecording() {
        recordingJob?.cancel()
        recordingJob = null
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        Log.i(TAG, "Recording stopped")
    }

    companion object {
        private const val TAG = "AndroidAudioRecorder"
    }
}