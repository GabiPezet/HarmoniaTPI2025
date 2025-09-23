package com.android.harmoniatpi.data.audio.player

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject


class PcmAudioPlayer @Inject constructor() : AudioPlayer {

    private var audioTrack: AudioTrack? = null
    private var playJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var lastPos: Long = 0L

    private val sampleRate = 44100
    private val channel = AudioFormat.CHANNEL_OUT_MONO
    private val encoding = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioTrack.getMinBufferSize(sampleRate, channel, encoding)

    override fun play(file: File): Result<Unit> {
        if (playJob != null) {
            return Result.failure(IllegalStateException("Playback already in progress"))
        }

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(encoding)
                    .setSampleRate(sampleRate)
                    .setChannelMask(channel)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .build()

        if (audioTrack?.state != AudioTrack.STATE_INITIALIZED) {
            return Result.failure(IllegalStateException("Error initializing AudioTrack"))
        }
        audioTrack?.play()

        playJob = scope.launch {
            val buffer = ByteArray(bufferSize)
            try {
                FileInputStream(file).use { fis ->
                    fis.skip(lastPos)
                    var read: Int
                    while (fis.read(buffer).also { read = it } > 0 && isActive) {
                        while (audioTrack?.playState == AudioTrack.PLAYSTATE_PAUSED && isActive) {
                            delay(50)
                        }
                        audioTrack?.write(buffer, 0, read)
                        lastPos += read
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during playback", e)
            }
        }

        return Result.success(Unit)
    }

    override fun pause() {
        audioTrack?.pause()
    }

    override fun stop() {
        playJob?.cancel()
        playJob = null
        audioTrack?.stop()
        audioTrack?.flush()
        audioTrack?.release()
        audioTrack = null
        lastPos = 0L
    }

    companion object {
        private const val TAG = "PcmAudioPlayer"
    }
}