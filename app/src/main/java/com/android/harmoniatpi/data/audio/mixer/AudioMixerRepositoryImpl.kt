package com.android.harmoniatpi.data.audio.mixer

import android.content.Context
import com.android.harmoniatpi.di.TrackFactory
import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.model.audio.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class AudioMixerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackFactory: TrackFactory
) : AudioMixerRepository {
    private val tracks = MutableStateFlow<List<Track>>(emptyList())

    override fun play() {
        tracks.value.forEach {
            it.play()
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

}