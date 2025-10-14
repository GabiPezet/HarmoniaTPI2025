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
import kotlin.math.roundToLong

/**
 * Utiliza AudioTrack para reproducir archivos .pcm.
 * **La reproducción en el archivo se realiza en un hilo separado**.
 */
class PcmAudioPlayer @Inject constructor() : AudioPlayer {

    private var file: File? = null
    private var playJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var lastPos: Long = 0L
    private val sampleRate = 44100
    private val channel = AudioFormat.CHANNEL_OUT_MONO
    private val encoding = AudioFormat.ENCODING_PCM_16BIT
    private val bytesPerSample = 2 // 16-bit PCM
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
    private var isMuted = false
    private var volume = 1f

    private fun msToByteOffset(ms: Long): Long {
        val samples = (ms * sampleRate / 1000f).roundToLong()
        return samples * bytesPerSample
    }

    override fun play(): Result<Unit> {
        return playRange(0L, Long.MAX_VALUE)
    }

    override fun playSegment(startMs: Long, endMs: Long): Result<Unit> {
        setAudioTrackVolume(1f)
        playJob?.invokeOnCompletion {
            if (isMuted) {
                mute()
            } else {
                setAudioTrackVolume(volume)
            }
        }
        return playRange(startMs, endMs)
    }

    private fun playRange(startMs: Long, endMs: Long): Result<Unit> { // Función auxiliar unificada
        if (playJob != null && audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
            return Result.failure(IllegalStateException("Playback already in progress"))
        }

        if (audioTrack.state != AudioTrack.STATE_INITIALIZED) {
            return Result.failure(IllegalStateException("Error initializing AudioTrack"))
        }

        // defino pos inicial y final con bytes
        val startByteOffset = if (endMs == Long.MAX_VALUE) lastPos else msToByteOffset(startMs)
        val endByteOffset =
            if (endMs == Long.MAX_VALUE) file?.length() ?: Long.MAX_VALUE else msToByteOffset(endMs)

        if (startByteOffset >= endByteOffset) {
            Log.e(TAG, "Rango de reproducción inválido o vacío: $startMs ms a $endMs ms")
            return Result.failure(IllegalArgumentException("Rango de reproducción inválido."))
        }

        audioTrack.play()
        playJob?.cancel() // cancelo lo anterior

        playJob = scope.launch {
            val buffer = ByteArray(bufferSize)
            try {
                file?.let { f ->
                    FileInputStream(f).use { fis ->
                        fis.skip(startByteOffset)
                        lastPos = startByteOffset

                        var read: Int
                        while (fis.read(buffer).also { read = it } > 0 && isActive) {

                            // compruebo
                            if (lastPos + read > endByteOffset) {
                                val remainingBytes = (endByteOffset - lastPos).toInt()
                                if (remainingBytes > 0) {
                                    audioTrack.write(buffer, 0, remainingBytes)
                                    lastPos = endByteOffset
                                }
                                break // Detener al alcanzar el fin del segmento
                            }

                            while (audioTrack.playState == AudioTrack.PLAYSTATE_PAUSED && isActive) {
                                delay(50)
                            }

                            audioTrack.write(buffer, 0, read)
                            lastPos += read
                        }
                    }
                }

                audioTrack.stop()
                audioTrack.flush()
                lastPos = 0
                onPlaybackCompletedCallback?.invoke()
            } catch (e: Exception) {
                Log.e(TAG, "Error during playback", e)
                lastPos = 0L
            }
        }

        return Result.success(Unit)
    }

    override fun pause() {
        if (audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.pause()
        }
    }

    override fun stop() {
        playJob?.cancel()
        playJob = null
        lastPos = 0L
        try {
            if (audioTrack.playState != AudioTrack.PLAYSTATE_STOPPED) {
                audioTrack.stop()
                audioTrack.flush()
            }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Error stopping AudioTrack", e)
        }
    }

    override fun release() {
        try {
            stop()
            audioTrack.release()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Error during release", e)
        }
    }

    override fun setFile(path: String) {
        file = File(path)
    }

    override fun setOnPlaybackCompletedCallback(callback: () -> Unit) {
        onPlaybackCompletedCallback = callback
    }

    override fun mute() {
        isMuted = true
        setAudioTrackVolume(0f)
    }

    override fun unMute() {
        isMuted = false
        setAudioTrackVolume(volume)
    }

    override fun isMuted(): Boolean = isMuted

    override fun setVolume(volume: Float) {
        this.volume = volume.coerceIn(0f, 1f)
        setAudioTrackVolume(this.volume)
    }

    override fun getVolume(): Float = volume

    private fun setAudioTrackVolume(value: Float) {
        val result = audioTrack.setVolume(value)
        if (result == AudioTrack.SUCCESS) {
            Log.d(TAG, "AudioTrack volume set to $value")
        } else {
            Log.e(TAG, "Failed to set AudioTrack volume to $value")
        }
    }

    companion object {
        private const val TAG = "PcmAudioPlayer"
    }
}
