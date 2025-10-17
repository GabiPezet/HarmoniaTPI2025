package com.android.harmoniatpi.data.audio.mixer

import android.content.Context
import android.media.AudioTrack
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.android.harmoniatpi.data.audio.player.PcmAudioPlayer
import com.android.harmoniatpi.data.audio.util.AudioConverter
import com.android.harmoniatpi.di.TrackFactory
import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.model.audio.AudioSourceType
import com.android.harmoniatpi.domain.model.audio.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlin.math.roundToLong

/**
 * Maneja las pistas creadas y se encarga de reproducirlas, pausarlas y pararlas.
 */
class AudioMixerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackFactory: TrackFactory,
    private val audioConverter: AudioConverter
) : AudioMixerRepository {
    /**
     * Lista de pistas disponibles
     */
    private val tracks = MutableStateFlow<List<Track>>(emptyList())

    /**
     * Estado observable para saber cuándo se terminaron de reproducir todas las pistas,
     * por más que difieran en duración
     */
    private val tracksCompleted = MutableStateFlow(false)

    /**
     * Contador de pistas que se han completado. El uso de AtomicInteger es seguro en hilos.
     */
    private val completedCount = AtomicInteger(0)

    private val currentPlaybackMs = MutableStateFlow(0L)
    private var masterPlaybackJob: Job? = null
    private val playerList: List<PcmAudioPlayer>
        get() = tracks.value.map { it.player as PcmAudioPlayer }


    override fun play() {
        masterPlaybackJob?.cancel()

        val validTracks = tracks.value.filter { it.hasAudio() }
        if (validTracks.isEmpty()) {
            tracksCompleted.value = true
            return
        }

        val globalStartMs = currentPlaybackMs.value
        completedCount.set(0)
        tracksCompleted.value = false
        masterPlaybackJob = CoroutineScope(Dispatchers.IO).launch {
            startPlaybackTracking()

            validTracks.forEach { track ->
                launch {
                    val delayMs = (track.startOffsetMs - globalStartMs).coerceAtLeast(0L)
                    val internalPlayPos = (globalStartMs - track.startOffsetMs).coerceAtLeast(0L)

                    if (delayMs > 0) {
                        delay(delayMs)
                    }

                    if (isActive) {
                        track.setOnPlaybackCompletedCallback {
                            val count = completedCount.incrementAndGet()
                            if (count >= validTracks.size) {
                                stop() // Llama a stop para limpiar todo.
                            }
                        }

                        track.play(internalPlayPos)
                    }
                }
            }
        }
    }

    override fun pause() {
        masterPlaybackJob?.cancel()
        stopPlaybackTracking()
        tracks.value.forEach { it.pause() }
    }

    override fun stop() {
        masterPlaybackJob?.cancel()
        stopPlaybackTracking()
        currentPlaybackMs.value = 0L
        tracks.value.forEach { it.stop() }
        tracksCompleted.value = true
    }

    override fun createTrack(sourceType: AudioSourceType) {
        val track = trackFactory.create(context.filesDir.absolutePath, sourceType)
        tracks.update { it + track }
    }

    override fun removeTrack(id: Long) {
        tracks.value.find { it.id == id }?.let { track ->
            track.delete()
            tracks.update { it -> it.filterNot { track -> track.id == id } }
        }
    }


    private var playbackTrackingJob: Job? = null
    private fun startPlaybackTracking() {
        playbackTrackingJob?.cancel()
        playbackTrackingJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                val activePlayers = playerList.filter { it.audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING }
                if (activePlayers.isNotEmpty()) {

                    val maxPos = activePlayers.maxOfOrNull { player ->
                        val track = tracks.value.find { it.player == player }
                        (track?.startOffsetMs ?: 0L) + player.getCurrentPositionMs()
                    } ?: currentPlaybackMs.value
                    currentPlaybackMs.value = maxPos
                }
                delay(50)
            }
        }
    }

    private fun stopPlaybackTracking() {
        playbackTrackingJob?.cancel()
    }

    override suspend fun getCurrentPlaybackPosition(): StateFlow<Long> = currentPlaybackMs.asStateFlow()

    override fun seekTo(ms: Long) {
        val wasPlaying = playerList.any { it.audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING }

        stopPlaybackTracking()
        stop()

        currentPlaybackMs.value = ms.coerceAtLeast(0L)

        if (wasPlaying) {
            play()
        }
    }

    override fun setTrackOffset(id: Long, offsetMs: Long) {
        tracks.update { current ->
            current.map { track ->
                if (track.id == id) {
                    track.apply { startOffsetMs = offsetMs.coerceAtLeast(0L) }
                } else {
                    track
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun createTrackFromFile(sourceFilePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        val sourceFile = File(sourceFilePath)
        if (!sourceFile.exists()) {
            return@withContext Result.failure(FileNotFoundException("Archivo de origen temporal no encontrado."))
        }

        val track = trackFactory.create(
            context.filesDir.absolutePath,
            sourceType = AudioSourceType.INSTRUMENT,
        )
        val destinationFile = File(track.path)

        try {
            val inputUri = Uri.fromFile(sourceFile)

            audioConverter.convertToPcm(inputUri, destinationFile)
                .onFailure { error ->
                    sourceFile.delete()
                    return@withContext Result.failure(error)
                }

            tracks.update { it + track }

            sourceFile.delete()

            return@withContext Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error importando/convirtiendo pista: ${e.message}", e)
            destinationFile.delete()
            sourceFile.delete()
            return@withContext Result.failure(e)
        }
    }


    override fun trimTrack(id: Long, startMs: Long, endMs: Long): Result<Unit> {
        return tracks.value.find { it.id == id }?.let { track ->
            val originalFile = File(track.path)
            val backupFile = File(track.originalPath) // nueva ruta del backup

            if (!originalFile.exists()) {
                return Result.failure(Exception("Archivo de audio original no encontrado: ${track.path}"))
            }

            try {
                // si no hay backup, creo copia
                if (!backupFile.exists()) {
                    originalFile.copyTo(backupFile, overwrite = true)
                    Log.i(TAG, "Copia de seguridad creada en ${track.originalPath}")
                }

                val sampleRate = 44100
                val bytesPerSample = 2 // pcm de 16 bit

                val startSamples = (startMs * sampleRate / 1000f).roundToLong()
                val endSamples = (endMs * sampleRate / 1000f).roundToLong()

                val startByte = startSamples * bytesPerSample
                val endByte = endSamples * bytesPerSample

                val length = originalFile.length()
                if (startByte < 0 || endByte > length || startByte >= endByte) {
                    return Result.failure(IllegalArgumentException("Rango de recorte inválido: fuera de límites o invertido."))
                }

                // archivo temporal
                val tempFile = File(track.path.replace(".pcm", "_temp.pcm"))
                val bufferSize = 8192

                FileInputStream(originalFile).use { fis ->
                    FileOutputStream(tempFile).use { fos ->

                        fis.skip(startByte)

                        val bytesToRead = endByte - startByte
                        var totalRead = 0L
                        val buffer = ByteArray(bufferSize)

                        while (totalRead < bytesToRead) {
                            val remaining =
                                (bytesToRead - totalRead).toInt().coerceAtMost(bufferSize)
                            val readCount = fis.read(buffer, 0, remaining)

                            if (readCount <= 0) break

                            fos.write(buffer, 0, readCount)
                            totalRead += readCount
                        }
                    }
                }

                // reemplazo original por recortado
                if (originalFile.delete()) {
                    if (tempFile.renameTo(originalFile)) {
                        return Result.success(Unit)
                    } else {
                        Log.e(TAG, "Fallo al renombrar el archivo temporal a la ruta original.")
                        return Result.failure(IOException("Fallo al aplicar el recorte: no se pudo renombrar el archivo temporal."))
                    }
                } else {
                    tempFile.delete()
                    Log.e(TAG, "Fallo al eliminar el archivo original para el reemplazo.")
                    return Result.failure(IOException("Fallo al aplicar el recorte: no se pudo eliminar el archivo original."))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error recortando pista ${track.id}", e)
                File(track.originalPath).delete() // limpio backup si falla el recorte
                return Result.failure(e)
            }
        } ?: Result.failure(NoSuchElementException("Pista con ID $id no encontrada"))
    }


    override fun undoTrim(id: Long): Result<Unit> {
        return tracks.value.find { it.id == id }?.let { track ->
            val currentFile = File(track.path)
            val backupFile = File(track.originalPath)

            if (!backupFile.exists()) {
                Log.w(
                    TAG,
                    "No hay copia de seguridad para deshacer el recorte en la pista ${track.id}"
                )
                return Result.failure(FileNotFoundException("No hay copia de seguridad disponible."))
            }

            try {
                if (currentFile.exists()) {
                    currentFile.delete()
                }

                // restauro el original a partir de un rename
                if (backupFile.renameTo(currentFile)) {
                    Log.i(TAG, "Recorte deshecho exitosamente para la pista ${track.id}")
                    return Result.success(Unit)
                } else {
                    Log.e(TAG, "Fallo al renombrar el backup como archivo actual.")
                    return Result.failure(IOException("Fallo al deshacer el recorte."))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error al deshacer el recorte para la pista ${track.id}", e)
                return Result.failure(e)
            }

        } ?: Result.failure(NoSuchElementException("Pista con ID $id no encontrada"))
    }


    override suspend fun getTracks(): StateFlow<List<Track>> = tracks.asStateFlow()
    override suspend fun allTracksWerePlayed(): StateFlow<Boolean> = tracksCompleted.asStateFlow()

    override fun muteTrack(id: Long) {
        tracks.update { current ->
            current.map { track ->
                if (track.id == id) {
                    track.mute()
                }
                track
            }
        }
    }

    override fun unMuteTrack(id: Long) {
        tracks.update { current ->
            current.map { track ->
                if (track.id == id) {
                    track.unMute()
                }
                track
            }
        }
    }

    override fun setTrackVolume(id: Long, volume: Float) {
        tracks.update { current ->
            current.map { track ->
                if (track.id == id) {
                    track.setVolume(volume)
                }
                track
            }
        }
    }

    private companion object {
        const val TAG = "AudioMixerRepository"
    }
}