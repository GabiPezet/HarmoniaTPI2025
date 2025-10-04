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
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

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

    override suspend fun getTracks(): StateFlow<List<Track>> = tracks.asStateFlow()
    override suspend fun allTracksWerePlayed(): StateFlow<Boolean> = tracksCompleted.asStateFlow()

    private companion object {
        const val TAG = "AudioMixerRepository"
    }
}