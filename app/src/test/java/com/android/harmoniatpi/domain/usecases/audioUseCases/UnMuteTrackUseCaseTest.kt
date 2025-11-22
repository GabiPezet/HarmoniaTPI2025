package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class UnMuteTrackUseCaseTest {

    private lateinit var audioMixerRepository: AudioMixerRepository
    private lateinit var unMuteTrackUseCase: UnMuteTrackUseCase

    @Before
    fun setUp() {
        audioMixerRepository = mockk(relaxed = true)
        unMuteTrackUseCase = UnMuteTrackUseCase(audioMixerRepository)
    }

    @Test
    fun `invoke calls unMuteTrack on repository`() {
        val trackId = 1L
        unMuteTrackUseCase(trackId)
        verify(exactly = 1) { audioMixerRepository.unMuteTrack(trackId) }
    }
}