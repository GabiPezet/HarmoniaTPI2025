package com.android.harmoniatpi.domain.model.audio

import android.util.Log
import com.android.harmoniatpi.domain.interfaces.AudioPlayer
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

/**
 * Representa una pista grabada o lista para grabar.
 * @param folderPath Ruta al directorio donde se guardarán la pista del track.
 * La pista se guardara en "forlderPath/timestamp.pcm"
 * @property player Instancia de AudioPlayer para reproducir, pausar o parar la pista.
 */
class Track @AssistedInject constructor(
    @Assisted folderPath: String,
    private val player: AudioPlayer
) {
    val id = System.currentTimeMillis()
    val path = folderPath.plus("/$id.pcm")

    init {
        player.setFile(path)
        Log.i(TAG, "Track $id created")
    }

    /**
     * Reproduce la pista.
     */
    fun play() {
        player.play()
            .onSuccess {
                Log.i(TAG, "Track $id played")
            }
            .onFailure {
                Log.e(TAG, "Error playing track $id", it)
            }
    }

    /**
     * Pausa la reproducción de la pista.
     */
    fun pause() {
        player.pause()
        Log.i(TAG, "Track $id paused")
    }

    /**
     * Para la reproducción de la pista.
     */
    fun stop() {
        player.stop()
        Log.i(TAG, "Track $id stopped")
    }

    /**
     * Borra la pista. Primero liberando los recursos de [AudioPlayer]
     * y luego borrando el archivo de la pista.
     */
    fun delete() {
        player.stop()
        player.release()
        if (deleteFile()) {
            Log.i(TAG, "Track $id deleted")
        } else {
            Log.e(TAG, "Error deleting track $id")
        }
    }

    /**
     * Establece un callback que se ejecutará cuando se complete la reproducción del audio.
     */
    fun setOnPlaybackCompletedCallback(callback: () -> Unit) {
        player.setOnPlaybackCompletedCallback {
            callback()
        }
    }

    /**
     * Verifica si la pista contiene audio.
     * @return true si la pista contiene audio, false en caso contrario.
     */
    fun hasAudio(): Boolean {
        val file = File(path)
        return file.exists() && file.length() > 0
    }

    /**
     * Borra el archivo de la pista.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     */
    private fun deleteFile(): Boolean {
        val file = File(path)
        return file.exists() && file.delete()
    }

    private companion object {
        const val TAG = "Track"
    }
}
