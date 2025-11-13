package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.usecases.audioUseCases.MuteTrackUseCase
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class MuteTrackUseCaseTest {

    private lateinit var audioMixerRepository: AudioMixerRepository
    private lateinit var muteTrackUseCase: MuteTrackUseCase

    @Before
    fun setUp() {
        audioMixerRepository = mockk(relaxed = true)
        muteTrackUseCase = MuteTrackUseCase(audioMixerRepository)
    }

    @Test
    fun `invoke calls muteTrack on repository`() {
        val trackId = 1L

        muteTrackUseCase(trackId)

        verify(exactly = 1) { audioMixerRepository.muteTrack(trackId) }
    }
}