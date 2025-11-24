package com.android.harmoniatpi.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.usecases.audioUseCases.StopAudioUseCase
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class StopAudioUseCaseTest {

    private lateinit var audioMixerRepository: AudioMixerRepository
    private lateinit var stopAudioUseCase: StopAudioUseCase

    @Before
    fun setUp() {
        audioMixerRepository = mockk(relaxed = true)
        stopAudioUseCase = StopAudioUseCase(audioMixerRepository)
    }

    @Test
    fun `invoke calls stop on repository`() = runBlocking {
        stopAudioUseCase()
        coVerify(exactly = 1) { audioMixerRepository.stop() }
    }
}
