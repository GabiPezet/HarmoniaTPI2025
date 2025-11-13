package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.usecases.audioUseCases.CutAudioSegmentUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CutAudioSegmentUseCaseTest {

    private lateinit var audioMixerRepository: AudioMixerRepository
    private lateinit var useCase: CutAudioSegmentUseCase

    @Before
    fun setUp() {
        audioMixerRepository = mockk(relaxed = true)
        useCase = CutAudioSegmentUseCase(audioMixerRepository)
    }

    @Test
    fun `invoke calls cutAudioSegment on repository and returns success`() {
        val trackId = 1L
        val startMs = 1000L
        val endMs = 2000L
        every { audioMixerRepository.cutAudioSegment(trackId, startMs, endMs) } returns Result.success(Unit)

        val result = useCase(trackId, startMs, endMs)

        verify(exactly = 1) { audioMixerRepository.cutAudioSegment(trackId, startMs, endMs) }
        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke returns failure when repository fails`() {
        val trackId = 1L
        val startMs = 1000L
        val endMs = 2000L
        val exception = Exception("Cut failed")
        every { audioMixerRepository.cutAudioSegment(trackId, startMs, endMs) } returns Result.failure(exception)

        val result = useCase(trackId, startMs, endMs)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Exception)
    }
}