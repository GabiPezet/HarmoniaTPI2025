package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class StopPreviewUseCaseTest {

    private lateinit var audioMixerRepository: AudioMixerRepository
    private lateinit var stopPreviewUseCase: StopPreviewUseCase

    @Before
    fun setUp() {
        audioMixerRepository = mockk(relaxed = true)
        stopPreviewUseCase = StopPreviewUseCase(audioMixerRepository)
    }

    @Test
    fun `invoke calls stopPreview on repository`() {
        stopPreviewUseCase()
        verify(exactly = 1) { audioMixerRepository.stopPreview() }
    }
}