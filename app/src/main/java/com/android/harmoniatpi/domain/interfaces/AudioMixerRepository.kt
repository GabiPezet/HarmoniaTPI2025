package com.android.harmoniatpi.domain.interfaces

import com.android.harmoniatpi.domain.model.audio.Track
import kotlinx.coroutines.flow.StateFlow

interface AudioMixerRepository {
    fun play()
    fun pause()
    fun stop()
    fun createTrack()
    fun removeTrack(id: Long)
    suspend fun getTracks(): StateFlow<List<Track>>
    suspend fun allTracksWerePlayed(): StateFlow<Boolean>
}
