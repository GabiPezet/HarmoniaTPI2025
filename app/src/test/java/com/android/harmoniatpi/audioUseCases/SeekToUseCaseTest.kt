package com.android.harmoniatpi.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.usecases.audioUseCases.SeekToUseCase
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SeekToUseCaseTest {

    private lateinit var audioMixerRepository: AudioMixerRepository
    private lateinit var seekToUseCase: SeekToUseCase

    @Before
    fun setUp() {
        audioMixerRepository = mockk(relaxed = true)
        seekToUseCase = SeekToUseCase(audioMixerRepository)
    }

    @Test
    fun `invoke calls seekTo on repository`() {
        val positionMs = 5000L
        seekToUseCase(positionMs)
        verify(exactly = 1) { audioMixerRepository.seekTo(positionMs) }
    }
}