package com.android.harmoniatpi.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.usecases.audioUseCases.UndoEffectUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UndoEffectUseCaseTest {

    private lateinit var audioMixerRepository: AudioMixerRepository
    private lateinit var useCase: UndoEffectUseCase

    @Before
    fun setUp() {
        audioMixerRepository = mockk(relaxed = true)
        useCase = UndoEffectUseCase(audioMixerRepository)
    }

    @Test
    fun `invoke calls undoEffect on repository and returns success`() {
        val trackId = 1L
        every { audioMixerRepository.undoEffect(trackId) } returns Result.success(Unit)

        val result = useCase(trackId)

        verify(exactly = 1) { audioMixerRepository.undoEffect(trackId) }
        assertTrue(result.isSuccess)
    }
}