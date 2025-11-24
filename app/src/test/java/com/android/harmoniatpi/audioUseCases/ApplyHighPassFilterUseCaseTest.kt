package com.android.harmoniatpi.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.usecases.audioUseCases.ApplyHighPassFilterUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ApplyHighPassFilterUseCaseTest {

    private lateinit var audioMixerRepository: AudioMixerRepository
    private lateinit var useCase: ApplyHighPassFilterUseCase

    @Before
    fun setUp() {
        audioMixerRepository = mockk(relaxed = true)
        useCase = ApplyHighPassFilterUseCase(audioMixerRepository)
    }

    @Test
    fun `invoke calls applyHighPassFilter on repository`() = runTest {
        val trackId = 1L
        val frequency = 300f
        coEvery { audioMixerRepository.applyHighPassFilter(trackId, frequency) } returns Result.success(Unit)

        val result = useCase(trackId, frequency)

        coVerify(exactly = 1) { audioMixerRepository.applyHighPassFilter(trackId, frequency) }
        assertTrue(result.isSuccess)
    }
}