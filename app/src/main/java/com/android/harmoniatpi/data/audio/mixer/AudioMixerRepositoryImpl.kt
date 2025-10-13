package com.android.harmoniatpi.data.audio.mixer

import android.content.Context
import android.util.Log
import com.android.harmoniatpi.di.TrackFactory
import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.model.audio.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    private val trackFactory: TrackFactory
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

    override fun play() {
        val validTracks = tracks.value.filter { it.hasAudio() }
        val totalTracks = validTracks.size
        completedCount.set(0)
        tracksCompleted.value = false

        if (validTracks.isNotEmpty()) {
            Log.i(TAG, "Tracks with audio: $totalTracks")
            validTracks.forEach { track ->
                track.setOnPlaybackCompletedCallback {
                    val count = completedCount.incrementAndGet()
                    Log.i(TAG, "Track ${track.id} completed. Count: $count")
                    if (count == totalTracks) {
                        tracksCompleted.value = true
                    }
                }
                track.play()
            }
        } else {
            Log.i(TAG, "No tracks with audio")
            tracksCompleted.value = true
        }
    }

    override fun pause() {
        tracks.value.forEach {
            it.pause()
        }
    }

    override fun stop() {
        tracks.value.forEach {
            it.stop()
        }
    }

    override fun createTrack() {
        val track = trackFactory.create(context.filesDir.absolutePath)
        tracks.update { it + track }
    }

    override fun removeTrack(id: Long) {
        tracks.value.find { it.id == id }?.let { track ->
            track.delete()
            tracks.update { it -> it.filterNot { track -> track.id == id } }
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
                val bytesPerSample = 2 // 16-bit PCM

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
                            val remaining = (bytesToRead - totalRead).toInt().coerceAtMost(bufferSize)
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
                Log.w(TAG, "No hay copia de seguridad para deshacer el recorte en la pista ${track.id}")
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

    private companion object {
        const val TAG = "AudioMixerRepository"
    }
}