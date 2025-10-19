package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.usecases.audioUseCases.PauseAudioUseCase
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class PauseAudioUseCaseTest {

    private lateinit var audioMixerRepository: AudioMixerRepository
    private lateinit var pauseAudioUseCase: PauseAudioUseCase

    @Before
    fun setUp() {
        audioMixerRepository = mockk(relaxed = true)
        pauseAudioUseCase = PauseAudioUseCase(audioMixerRepository)
    }

    @Test
    fun `invoke calls pause on repository`() = runBlocking {
        pauseAudioUseCase()
        coVerify(exactly = 1) { audioMixerRepository.pause() }
    }
}
