package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.usecases.audioUseCases.SetTrackVolumeUseCase
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SetTrackVolumeUseCaseTest {

    private lateinit var audioMixerRepository: AudioMixerRepository
    private lateinit var setTrackVolumeUseCase: SetTrackVolumeUseCase

    @Before
    fun setUp() {
        audioMixerRepository = mockk(relaxed = true)
        setTrackVolumeUseCase = SetTrackVolumeUseCase(audioMixerRepository)
    }

    @Test
    fun `invoke calls setTrackVolume on repository`() {
        val trackId = 1L
        val volume = 0.5f

        setTrackVolumeUseCase(trackId, volume)

        verify(exactly = 1) { audioMixerRepository.setTrackVolume(trackId, volume) }
    }
}