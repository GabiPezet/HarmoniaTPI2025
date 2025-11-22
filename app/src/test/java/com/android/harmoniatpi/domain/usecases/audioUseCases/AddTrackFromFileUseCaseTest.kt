package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddTrackFromFileUseCaseTest {

    private lateinit var audioMixerRepository: AudioMixerRepository
    private lateinit var useCase: AddTrackFromFileUseCase

    @Before
    fun setUp() {
        audioMixerRepository = mockk(relaxed = true)
        useCase = AddTrackFromFileUseCase(audioMixerRepository)
    }

    @Test
    fun `invoke calls createTrackFromFile on repository`() = runTest {
        val sourcePath = "path/to/source.mp3"
        coEvery { audioMixerRepository.createTrackFromFile(sourcePath) } returns Result.success(Unit)

        val result = useCase(sourcePath)

        coVerify(exactly = 1) { audioMixerRepository.createTrackFromFile(sourcePath) }
        assertTrue(result.isSuccess)
    }
}