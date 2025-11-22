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
import java.io.FileNotFoundException
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import kotlin.math.roundToLong

/**
 * Utiliza AudioTrack para reproducir archivos .pcm.
 * **La reproducción en el archivo se realiza en un hilo separado**.
 */
class PcmAudioPlayer @Inject constructor() : AudioPlayer {

    internal var file: File? = null
    private var playJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val sampleRate = 44100
    private val channel = AudioFormat.CHANNEL_OUT_MONO
    private val encoding = AudioFormat.ENCODING_PCM_16BIT
    private val bytesPerSample = 2
    private val currentPosMs = AtomicLong(0L)
    private val bufferSize = AudioTrack.getMinBufferSize(sampleRate, channel, encoding)
    val audioTrack = AudioTrack.Builder()
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

    override fun play(startMs: Long, delayPlay: Long): Result<Unit> {
        return playRange(startMs, Long.MAX_VALUE, delayPlay)
    }

    override fun playSegment(startMs: Long, endMs: Long, delayPlay: Long): Result<Unit> {
        setAudioTrackVolume(1f)
        playJob?.invokeOnCompletion {
            if (isMuted) {
                mute()
            } else {
                setAudioTrackVolume(volume)
            }
        }
        return playRange(startMs, endMs, delayPlay)
    }

    private fun playRange(startMs: Long, endMs: Long, delayPlay: Long): Result<Unit> {

        val currentFile = file
        if (currentFile == null || !currentFile.exists()) {
            Log.e(TAG, "playRange - Error: Archivo no establecido o no existe.")
            return Result.failure(FileNotFoundException("Archivo de audio no configurado o no encontrado."))
        }
        val fileLengthBytes = currentFile.length()
        if (fileLengthBytes == 0L) {
            Log.e(TAG, "playRange - Error: El archivo PCM está vacío (${currentFile.path}).")

            return Result.failure(IOException("El archivo de audio está vacío."))
        }

        if (playJob != null && audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
            Log.w(
                TAG,
                "playRange - Advertencia: La reproducción ya estaba en progreso. Deteniendo la anterior."
            )
            stop()
        }

        if (audioTrack.state != AudioTrack.STATE_INITIALIZED) {
            Log.e(
                TAG,
                "playRange - Error: AudioTrack no inicializado (Estado: ${audioTrack.state})."
            )
            return Result.failure(IllegalStateException("Error initializing AudioTrack"))
        }

        val startByteOffset = msToByteOffset(startMs)

        val endByteOffset = if (endMs == Long.MAX_VALUE) {
            fileLengthBytes
        } else {
            msToByteOffset(endMs).coerceAtMost(fileLengthBytes)
        }

        Log.d(TAG, "playRange - Intentando reproducir: ${currentFile.path}")
        Log.d(
            TAG,
            "playRange - Rango (ms): $startMs a ${if (endMs == Long.MAX_VALUE) "Fin" else endMs}"
        )
        Log.d(
            TAG,
            "playRange - Rango (bytes): $startByteOffset a $endByteOffset (Longitud total: $fileLengthBytes)"
        )

        if (startByteOffset < 0 || endByteOffset <= startByteOffset) {
            Log.e(
                TAG,
                "playRange - Error CRÍTICO: Rango de bytes inválido. Start: $startByteOffset, End: $endByteOffset, Length: $fileLengthBytes"
            )
            return Result.failure(IllegalArgumentException("Rango de reproducción inválido."))
        }

        try {
            audioTrack.play()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "playRange - Error al llamar a audioTrack.play()", e)
            return Result.failure(e)
        }
        //playJob?.cancel()
        currentPosMs.set(startMs)
        if (playJob == null || playJob?.isCompleted == true || playJob?.isCancelled == true) {
            playJob = scope.launch {
                if (delayPlay > 0) {
                    delay(delayPlay)
                }
                val buffer = ByteArray(bufferSize)
                try {
                    FileInputStream(currentFile).use { fis ->
                        var skippedBytes = 0L
                        while (skippedBytes < startByteOffset) {
                            val skipped = fis.skip(startByteOffset - skippedBytes)
                            if (skipped <= 0) {
                                Log.e(
                                    TAG,
                                    "playRange - Error: No se pudo saltar a startByteOffset $startByteOffset. Archivo demasiado corto?"
                                )
                                throw IOException("Fallo al buscar el inicio del audio.")
                            }
                            skippedBytes += skipped
                        }
                        var bytesReadFromStart = startByteOffset

                        var read: Int
                        while (fis.read(buffer).also { read = it } > 0 && isActive) {
                            val bytesToWrite = if (bytesReadFromStart + read > endByteOffset) {
                                (endByteOffset - bytesReadFromStart).toInt().coerceAtLeast(0)
                            } else {
                                read
                            }

                            if (bytesToWrite <= 0) {
                                break
                            }

                            while (audioTrack.playState == AudioTrack.PLAYSTATE_PAUSED && isActive) {
                                delay(50)
                            }
                            if (!isActive) break

                            val written = audioTrack.write(buffer, 0, bytesToWrite)
                            if (written < 0) {
                                Log.e(
                                    TAG,
                                    "playRange - Error escribiendo en AudioTrack. Código de error: $written"
                                )
                                throw IOException("Error al escribir datos en AudioTrack")
                            }
                            bytesReadFromStart += written

                            val currentSamples = (bytesReadFromStart / bytesPerSample)
                            currentPosMs.set(currentSamples * 1000L / sampleRate)

                            if (bytesReadFromStart >= endByteOffset) {
                                break
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "playRange - Error durante la reproducción en el Job", e)
                } finally {
                    if (isActive) {
                        Log.d(TAG, "playRange - Reproducción completada.")
                        onPlaybackCompletedCallback?.invoke()
                    } else {
                        Log.d(TAG, "playRange - Reproducción cancelada.")
                    }
                    try {
                        if (audioTrack.playState != AudioTrack.PLAYSTATE_STOPPED) {
                            audioTrack.stop()
                            audioTrack.flush()
                        }
                    } catch (e: IllegalStateException) {
                        Log.w(
                            TAG,
                            "playRange - Advertencia: Excepción al detener/limpiar AudioTrack en finally: ${e.message}"
                        )
                    }
                    currentPosMs.set(0L)
                    playJob = null
                }
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
        currentPosMs.set(0L)
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


    fun getDurationMs(): Long {
        val currentFile = file ?: return 0L
        if (!currentFile.exists() || currentFile.length() == 0L) {
            return 0L
        }
        val totalBytes = currentFile.length()
        val totalSamples = totalBytes / bytesPerSample
        return totalSamples * 1000L / sampleRate
    }


    companion object {
        private const val TAG = "PcmAudioPlayer"
    }
}
