package com.android.harmoniatpi.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioRecorderRepository
import com.android.harmoniatpi.domain.usecases.audioUseCases.StartRecordingAudioUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class StartRecordingAudioUseCaseTest {

    private lateinit var audioRecorderRepository: AudioRecorderRepository
    private lateinit var startRecordingAudioUseCase: StartRecordingAudioUseCase

    @Before
    fun setUp() {
        audioRecorderRepository = mockk(relaxed = true)
        startRecordingAudioUseCase = StartRecordingAudioUseCase(audioRecorderRepository)
    }

    @Test
    fun `when use case is executed, it should call startRecording on the repository`() = runTest {
        val fileName = "test_audio"
        coEvery { audioRecorderRepository.startRecording(fileName, 1) } returns Result.success(Unit)

        startRecordingAudioUseCase(fileName, 1)

        coVerify(exactly = 1) { audioRecorderRepository.startRecording(fileName, 1) }
    }

    @Test
    fun `when repository returns success, it should return success`() = runTest {
        val fileName = "test_audio"
        coEvery { audioRecorderRepository.startRecording(fileName, 1) } returns Result.success(Unit)

        val result = startRecordingAudioUseCase(fileName, 1)

        assert(result.isSuccess)
    }

    @Test
    fun `when repository returns failure, it should return failure`() = runTest {
        val fileName = "test_audio"
        val exception = RuntimeException("Recording failed")
        coEvery { audioRecorderRepository.startRecording(fileName, 1) } returns Result.failure(exception)

        val result = startRecordingAudioUseCase(fileName, 1)

        assert(result.isFailure)
        assert(result.exceptionOrNull() == exception)
    }
}
