package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class PlayAudioUseCaseTest {

    private lateinit var audioMixerRepository: AudioMixerRepository
    private lateinit var playAudioUseCase: PlayAudioUseCase

    @Before
    fun setUp() {
        audioMixerRepository = mockk(relaxed = true)
        playAudioUseCase = PlayAudioUseCase(audioMixerRepository)
    }

    @Test
    fun `invoke calls play on repository`() = runBlocking {
        playAudioUseCase()
        coVerify(exactly = 1) { audioMixerRepository.play() }
    }
}
