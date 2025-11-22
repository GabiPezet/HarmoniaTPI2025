package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetCurrentPlaybackPositionUseCaseTest {

    private lateinit var audioMixerRepository: AudioMixerRepository
    private lateinit var useCase: GetCurrentPlaybackPositionUseCase

    @Before
    fun setUp() {
        audioMixerRepository = mockk(relaxed = true)
        useCase = GetCurrentPlaybackPositionUseCase(audioMixerRepository)
    }

    @Test
    fun `invoke returns position flow from repository`() = runTest {
        val positionFlow = MutableStateFlow(1234L)
        coEvery { audioMixerRepository.getCurrentPlaybackPosition() } returns positionFlow

        val result = useCase()

        assertEquals(1234L, result.first())
    }
}