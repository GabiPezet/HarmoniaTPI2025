package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.usecases.audioUseCases.ApplyFlangerEffectUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ApplyFlangerEffectUseCaseTest {

    private lateinit var audioMixerRepository: AudioMixerRepository
    private lateinit var useCase: ApplyFlangerEffectUseCase

    @Before
    fun setUp() {
        audioMixerRepository = mockk(relaxed = true)
        useCase = ApplyFlangerEffectUseCase(audioMixerRepository)
    }

    @Test
    fun `invoke calls applyFlangerEffect on repository`() = runTest {
        val trackId = 1L
        val rate = 0.5f
        val wet = 0.8f
        coEvery { audioMixerRepository.applyFlangerEffect(trackId, rate, wet) } returns Result.success(Unit)

        val result = useCase(trackId, rate, wet)

        coVerify(exactly = 1) { audioMixerRepository.applyFlangerEffect(trackId, rate, wet) }
        assertTrue(result.isSuccess)
    }
}