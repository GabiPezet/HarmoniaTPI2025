package com.android.harmoniatpi.data.audio.player

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.android.harmoniatpi.domain.interfaces.AudioPlayer
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

    private var file: File? = null
    private var playJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var lastPos: Long = 0L
    private val sampleRate = 44100
    private val channel = AudioFormat.CHANNEL_OUT_MONO
    private val encoding = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioTrack.getMinBufferSize(sampleRate, channel, encoding)
    private val audioTrack = AudioTrack.Builder()
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
    private var onPlaybackCompletedCallback: (() -> Unit)? = null

    override fun play(): Result<Unit> {
        if (playJob != null && audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
            return Result.failure(IllegalStateException("Playback already in progress"))
        }

        if (audioTrack.state != AudioTrack.STATE_INITIALIZED) {
            return Result.failure(IllegalStateException("Error initializing AudioTrack"))
        }
        audioTrack.play()

        if (playJob == null) {
            playJob = scope.launch {
                val buffer = ByteArray(bufferSize)
                try {
                    file?.let { f ->
                        FileInputStream(f).use { fis ->
                            fis.skip(lastPos)
                            var read: Int
                            while (fis.read(buffer).also { read = it } > 0 && isActive) {
                                while (audioTrack.playState == AudioTrack.PLAYSTATE_PAUSED && isActive) {
                                    delay(50)
                                }
                                audioTrack.write(buffer, 0, read)
                                lastPos += read
                            }
                        }
                    }
                    audioTrack.stop()
                    lastPos = 0
                    onPlaybackCompletedCallback?.invoke()
                } catch (e: Exception) {
                    Log.e(TAG, "Error during playback", e)
                }
            }
            playJob?.invokeOnCompletion {
                release()
                playJob = null
            }
        }
        return Result.success(Unit)
    }

    override fun pause() {
        audioTrack.pause()
    }

    override fun stop() {
        playJob?.cancel()
        playJob = null
        lastPos = 0L
    }

    override fun release() {
        try {
            audioTrack.stop()
            audioTrack.flush()
            audioTrack.release()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Error during release", e)
        }
    }

    override fun setFile(path: String) {
        file = File(path)
    }

    override fun getFilePath(): String = file?.absolutePath ?: ""

    override fun setOnPlaybackCompletedCallback(callback: () -> Unit) {
        onPlaybackCompletedCallback = callback
    }

    companion object {
        private const val TAG = "PcmAudioPlayer"
    }
}