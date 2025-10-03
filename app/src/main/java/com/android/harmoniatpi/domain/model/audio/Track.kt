package com.android.harmoniatpi.domain.model.audio

import android.util.Log
import com.android.harmoniatpi.domain.interfaces.AudioPlayer
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

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

    fun play() {
        player.play()
            .onSuccess {
                Log.i(TAG, "Track $id played")
            }
            .onFailure {
                Log.e(TAG, "Error playing track $id", it)
            }
    }

    fun pause() {
        player.pause()
        Log.i(TAG, "Track $id paused")
    }

    fun stop() {
        player.stop()
        Log.i(TAG, "Track $id stopped")
    }

    fun delete() {
        player.stop()
        player.release()
        if (deleteFile()) {
            Log.i(TAG, "Track $id deleted")
        } else {
            Log.e(TAG, "Error deleting track $id")
        }
    }

    private fun deleteFile(): Boolean {
        val file = File(path)
        return file.exists() && file.delete()
    }

    private companion object {
        const val TAG = "Track"
    }
}
