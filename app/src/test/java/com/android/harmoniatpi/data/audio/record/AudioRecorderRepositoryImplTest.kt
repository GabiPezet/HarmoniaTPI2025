package com.android.harmoniatpi.data.audio.record

import android.media.MediaRecorder.AudioSource
import com.android.harmoniatpi.domain.interfaces.AudioRecorder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AudioRecorderRepositoryImplTest {

    private lateinit var recorder: AudioRecorder
    private lateinit var repository: AudioRecorderRepositoryImpl

    @Before
    fun setUp() {
        recorder = mockk(relaxed = true)
        repository = AudioRecorderRepositoryImpl(recorder)
    }

    @Test
    fun `startRecording successfully calls recorder`() {
        val filePath = "audio.pcm"
        val audioSource = 1
        val expectedResult = Result.success(Unit)
        every { recorder.startRecording(1) } returns expectedResult

        val result = repository.startRecording(filePath, 1)

        verify { recorder.setOutputFile(filePath) }
        verify { recorder.startRecording(1) }
        assertEquals(expectedResult, result)
    }

    @Test
    fun `startRecording returns failure when recorder fails`() {
        val filePath = "audio.pcm"
        val exception = RuntimeException("Recording failed")
        val expectedResult = Result.failure<Unit>(exception)
        every { recorder.startRecording(1) } returns expectedResult

        val result = repository.startRecording(filePath, 1)

        verify { recorder.setOutputFile(filePath) }
        verify { recorder.startRecording(1) }
        assertEquals(expectedResult, result)
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `stopRecording successfully calls recorder`() {
        val expectedResult = Result.success(Unit)
        every { recorder.stopRecording() } returns expectedResult

        val result = repository.stopRecording()

        verify { recorder.stopRecording() }
        assertEquals(expectedResult, result)
    }

    @Test
    fun `stopRecording returns failure when recorder fails`() {
        val exception = RuntimeException("Stop failed")
        val expectedResult = Result.failure<Unit>(exception)
        every { recorder.stopRecording() } returns expectedResult

        val result = repository.stopRecording()

        verify { recorder.stopRecording() }
        assertEquals(expectedResult, result)
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
