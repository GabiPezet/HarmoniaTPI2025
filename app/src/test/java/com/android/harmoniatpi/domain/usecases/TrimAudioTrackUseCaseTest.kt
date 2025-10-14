
package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TrimAudioTrackUseCaseTest {

    private lateinit var trimAudioTrackUseCase: TrimAudioTrackUseCase
    private val mockMixerRepository: AudioMixerRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        trimAudioTrackUseCase = TrimAudioTrackUseCase(mockMixerRepository)
    }

    @Test
    fun `invoke calls trimTrack on repository and returns success`() {
        val trackId = 1L
        val startMs = 1000L
        val endMs = 5000L
        every { mockMixerRepository.trimTrack(trackId, startMs, endMs) } returns Result.success(Unit)

        val result = trimAudioTrackUseCase(trackId, startMs, endMs)

        verify(exactly = 1) { mockMixerRepository.trimTrack(trackId, startMs, endMs) }
        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke returns failure when repository fails`() {
        val trackId = 1L
        val startMs = 1000L
        val endMs = 5000L
        val exception = Exception("Trim failed")
        every { mockMixerRepository.trimTrack(trackId, startMs, endMs) } returns Result.failure(exception)

        val result = trimAudioTrackUseCase(trackId, startMs, endMs)

        verify(exactly = 1) { mockMixerRepository.trimTrack(trackId, startMs, endMs) }
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Exception)
    }
}
