package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.AudioRecorderRepository
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
        coEvery { audioRecorderRepository.startRecording(fileName) } returns Result.success(Unit)

        startRecordingAudioUseCase(fileName)

        coVerify(exactly = 1) { audioRecorderRepository.startRecording(fileName) }
    }

    @Test
    fun `when repository returns success, it should return success`() = runTest {
        val fileName = "test_audio"
        coEvery { audioRecorderRepository.startRecording(fileName) } returns Result.success(Unit)

        val result = startRecordingAudioUseCase(fileName)

        assert(result.isSuccess)
    }

    @Test
    fun `when repository returns failure, it should return failure`() = runTest {
        val fileName = "test_audio"
        val exception = RuntimeException("Recording failed")
        coEvery { audioRecorderRepository.startRecording(fileName) } returns Result.failure(exception)

        val result = startRecordingAudioUseCase(fileName)

        assert(result.isFailure)
        assert(result.exceptionOrNull() == exception)
    }
}
