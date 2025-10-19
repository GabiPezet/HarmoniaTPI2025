package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.AudioRecorderRepository
import com.android.harmoniatpi.domain.usecases.audioUseCases.StopRecordingAudioUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class StopRecordingAudioUseCaseTest {

    private lateinit var audioRecorderRepository: AudioRecorderRepository
    private lateinit var stopRecordingAudioUseCase: StopRecordingAudioUseCase

    @Before
    fun setUp() {
        audioRecorderRepository = mockk(relaxed = true)
        stopRecordingAudioUseCase = StopRecordingAudioUseCase(audioRecorderRepository)
    }

    @Test
    fun `when use case is executed, it should call stopRecording on the repository`() = runTest {
        coEvery { audioRecorderRepository.stopRecording() } returns Result.success(Unit)

        stopRecordingAudioUseCase()

        coVerify(exactly = 1) { audioRecorderRepository.stopRecording() }
    }

    @Test
    fun `when repository returns success, it should return success`() = runTest {
        coEvery { audioRecorderRepository.stopRecording() } returns Result.success(Unit)

        val result = stopRecordingAudioUseCase()

        assert(result.isSuccess)
    }

    @Test
    fun `when repository returns failure, it should return failure`() = runTest {
        val exception = RuntimeException("Stop recording failed")
        coEvery { audioRecorderRepository.stopRecording() } returns Result.failure(exception)

        val result = stopRecordingAudioUseCase()

        assert(result.isFailure)
        assert(result.exceptionOrNull() == exception)
    }
}
