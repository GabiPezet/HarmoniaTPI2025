package com.android.harmoniatpi.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.model.audio.AudioSourceType
import com.android.harmoniatpi.domain.usecases.audioUseCases.AddTrackUseCase
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class AddTrackUseCaseTest {

    private lateinit var audioMixerRepository: AudioMixerRepository
    private lateinit var addTrackUseCase: AddTrackUseCase

    @Before
    fun setUp() {
        audioMixerRepository = mockk(relaxed = true)
        addTrackUseCase = AddTrackUseCase(audioMixerRepository)
    }

    @Test
    fun `invoke calls createTrack on repository`() {
        addTrackUseCase(AudioSourceType.VOICE)
        verify(exactly = 1) { audioMixerRepository.createTrack(AudioSourceType.VOICE) }
    }
}
