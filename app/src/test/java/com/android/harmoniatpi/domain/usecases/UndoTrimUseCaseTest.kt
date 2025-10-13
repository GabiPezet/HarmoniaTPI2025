
package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UndoTrimUseCaseTest {

    private lateinit var undoTrimUseCase: UndoTrimUseCase
    private val mockMixerRepository: AudioMixerRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        undoTrimUseCase = UndoTrimUseCase(mockMixerRepository)
    }

    @Test
    fun `invoke calls undoTrim on repository and returns success`() {
        val trackId = 1L
        every { mockMixerRepository.undoTrim(trackId) } returns Result.success(Unit)

        val result = undoTrimUseCase(trackId)

        verify(exactly = 1) { mockMixerRepository.undoTrim(trackId) }
        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke returns failure when repository fails`() {
        val trackId = 1L
        val exception = Exception("Undo trim failed")
        every { mockMixerRepository.undoTrim(trackId) } returns Result.failure(exception)

        val result = undoTrimUseCase(trackId)
        
        verify(exactly = 1) { mockMixerRepository.undoTrim(trackId) }
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Exception)
    }
}
