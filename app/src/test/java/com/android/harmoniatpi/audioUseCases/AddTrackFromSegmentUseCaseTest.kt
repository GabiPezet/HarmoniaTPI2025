package com.android.harmoniatpi.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.usecases.audioUseCases.AddTrackFromSegmentUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddTrackFromSegmentUseCaseTest {

    private lateinit var audioMixerRepository: AudioMixerRepository
    private lateinit var useCase: AddTrackFromSegmentUseCase

    @Before
    fun setUp() {
        audioMixerRepository = mockk(relaxed = true)
        useCase = AddTrackFromSegmentUseCase(audioMixerRepository)
    }

    @Test
    fun `invoke calls addTrackFromSegment on repository`() = runTest {
        val sourcePath = "path/to/source.pcm"
        val startMs = 1000L
        val endMs = 3000L
        coEvery { audioMixerRepository.addTrackFromSegment(sourcePath, startMs, endMs) } returns Result.success(Unit)

        val result = useCase(sourcePath, startMs, endMs)

        coVerify(exactly = 1) { audioMixerRepository.addTrackFromSegment(sourcePath, startMs, endMs) }
        assertTrue(result.isSuccess)
    }
}