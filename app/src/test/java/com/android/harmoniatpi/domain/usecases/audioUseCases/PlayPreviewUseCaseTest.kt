package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class PlayPreviewUseCaseTest {

    private lateinit var audioMixerRepository: AudioMixerRepository
    private lateinit var playPreviewUseCase: PlayPreviewUseCase

    @Before
    fun setUp() {
        audioMixerRepository = mockk(relaxed = true)
        playPreviewUseCase = PlayPreviewUseCase(audioMixerRepository)
    }

    @Test
    fun `invoke calls playPreview on repository`() {
        val filePath = "path/to/preview.pcm"
        playPreviewUseCase(filePath)
        verify(exactly = 1) { audioMixerRepository.playPreview(filePath) }
    }
}