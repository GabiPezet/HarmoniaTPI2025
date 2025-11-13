package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.interfaces.AudioPlayer
import com.android.harmoniatpi.domain.model.audio.AudioSourceType
import com.android.harmoniatpi.domain.model.audio.Track
import com.android.harmoniatpi.domain.usecases.audioUseCases.GetTracksUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetTracksUseCaseTest {

    private lateinit var audioMixerRepository: AudioMixerRepository
    private lateinit var getTracksUseCase: GetTracksUseCase
    private lateinit var audioPlayer: AudioPlayer

    @Before
    fun setUp() {
        audioMixerRepository = mockk()
        getTracksUseCase = GetTracksUseCase(audioMixerRepository)
        audioPlayer = mockk(relaxed = true)
    }

    @Test
    fun `invoke returns flow of tracks from repository`() = runBlocking {
        val fakeTracks = listOf(Track("folderPath", "existing",1, AudioSourceType.VOICE, audioPlayer))
        val stateFlow = MutableStateFlow(fakeTracks)
        coEvery { audioMixerRepository.getTracks() } returns stateFlow

        val result = getTracksUseCase()

        assertEquals(fakeTracks, result.first())
    }
}
