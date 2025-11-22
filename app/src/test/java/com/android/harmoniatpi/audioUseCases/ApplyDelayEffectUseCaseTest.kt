package com.android.harmoniatpi.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.usecases.audioUseCases.ApplyDelayEffectUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ApplyDelayEffectUseCaseTest {

    private lateinit var audioMixerRepository: AudioMixerRepository
    private lateinit var useCase: ApplyDelayEffectUseCase

    @Before
    fun setUp() {
        audioMixerRepository = mockk(relaxed = true)
        useCase = ApplyDelayEffectUseCase(audioMixerRepository)
    }

    @Test
    fun `invoke calls applyDelayEffect on repository`() = runTest {
        val trackId = 1L
        val delayTime = 0.5f
        val decay = 0.3f
        coEvery { audioMixerRepository.applyDelayEffect(trackId, delayTime, decay) } returns Result.success(Unit)

        val result = useCase(trackId, delayTime, decay)

        coVerify(exactly = 1) { audioMixerRepository.applyDelayEffect(trackId, delayTime, decay) }
        assertTrue(result.isSuccess)
    }
}