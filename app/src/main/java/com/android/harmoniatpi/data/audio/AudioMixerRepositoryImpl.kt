package com.android.harmoniatpi.data.audio

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.android.harmoniatpi.data.audio.util.AudioConverter
import com.android.harmoniatpi.di.TrackFactory
import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.model.audio.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    override fun play() {
        val validTracks = tracks.value.filter { it.hasAudio() }
        val totalTracks = validTracks.size
        completedCount.set(0)
        tracksCompleted.value = false

        tracks.value.forEach {
            it.stop()
        }


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
        val id = System.currentTimeMillis()
        val file = File(context.filesDir, "$id.pcm")

        // 🔹 Crea el archivo físico vacío
        if (!file.exists()) {
            file.createNewFile()
        }

        val track = trackFactory.create(
            folderPath = context.filesDir.absolutePath,
            existingFilePath = file.absolutePath,
            idExist = id
        )

        tracks.update { it + track }

        Log.i("AudioMixerRepository", "🎶 Nueva pista creada: ${file.absolutePath}")
        Log.i("AudioMixerRepository", "🎶 Nueva pista creada: ${tracks.value}")
    }

    override fun removeTrack(id: Long) {
        tracks.value.find { it.id == id }?.let { track ->
            track.delete()
            tracks.update { it -> it.filterNot { track -> track.id == id } }
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun createTrackFromFile(sourceFilePath: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val sourceFile = File(sourceFilePath)
            if (!sourceFile.exists()) {
                return@withContext Result.failure(FileNotFoundException("Archivo de origen temporal no encontrado."))
            }

            val track = trackFactory.create(context.filesDir.absolutePath)
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


    override fun getTracks(): StateFlow<List<Track>> = tracks.asStateFlow()
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

    override suspend fun loadPcmTrack(file: File,id: Long) {
        if (!file.exists()) {
            Log.e(TAG, "PCM file not found: ${file.absolutePath}")
            return
        }

        val track = trackFactory.create(file.parent!!, file.absolutePath,id)
        tracks.update { it + track }
        Log.i(TAG, "Track restored from PCM: ${file.name} with path ${track.path}")
    }

    override fun clearAllTracks() {
        tracks.update { emptyList() }
        Log.i("AudioMixerRepository", "🧹 Tracks limpiados del repositorio")
    }


    private companion object {
        const val TAG = "AudioMixerRepository"
    }
}