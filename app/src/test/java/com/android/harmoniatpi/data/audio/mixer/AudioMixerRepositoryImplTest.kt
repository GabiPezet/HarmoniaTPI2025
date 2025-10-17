package com.android.harmoniatpi.data.audio.mixer

import android.content.Context
import com.android.harmoniatpi.data.audio.AudioMixerRepositoryImpl
import com.android.harmoniatpi.di.TrackFactory
import com.android.harmoniatpi.domain.model.audio.Track
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.math.roundToLong

@ExperimentalCoroutinesApi
class AudioMixerRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var trackFactory: TrackFactory
    private lateinit var repository: AudioMixerRepositoryImpl

    @Before
    fun setUp() {
        context = mockk()
        trackFactory = mockk()
        repository = AudioMixerRepositoryImpl(context, trackFactory)

        val filesDir = mockk<File>()
        every { context.filesDir } returns filesDir
        every { filesDir.absolutePath } returns "/fake/path"
    }

    @Test
    fun `createTrack adds a new track to the list`() = runTest {
        val mockTrack = mockk<Track>(relaxed = true)
        every { trackFactory.create(any()) } returns mockTrack

        repository.createTrack()

        val tracks = repository.getTracks().first()
        assertEquals(1, tracks.size)
        assertEquals(mockTrack, tracks[0])
        verify { trackFactory.create("/fake/path") }
    }

    @Test
    fun `removeTrack removes the correct track and calls delete`() = runTest {
        val trackIdToRemove = 1L
        val mockTrack1 = mockk<Track>(relaxed = true) { every { id } returns trackIdToRemove }
        val mockTrack2 = mockk<Track>(relaxed = true) { every { id } returns 2L }

        every { trackFactory.create(any()) }.returns(mockTrack1).andThen(mockTrack2)
        repository.createTrack()
        repository.createTrack()

        assertEquals(2, repository.getTracks().first().size)

        repository.removeTrack(trackIdToRemove)

        val tracks = repository.getTracks().first()
        assertEquals(1, tracks.size)
        assertEquals(mockTrack2, tracks[0])
        verify { mockTrack1.delete() }
    }

    @Test
    fun `play calls play on all tracks`() = runTest {
        val mockTrack1 = mockk<Track>(relaxed = true) { every { hasAudio() } returns true }
        val mockTrack2 = mockk<Track>(relaxed = true) { every { hasAudio() } returns true }
        every { trackFactory.create(any()) }.returns(mockTrack1).andThen(mockTrack2)
        repository.createTrack()
        repository.createTrack()

        repository.play()

        verify { mockTrack1.play() }
        verify { mockTrack2.play() }
    }
    
    @Test
    fun `play only calls play on tracks with audio`() = runTest {
        val mockTrackWithAudio = mockk<Track>(relaxed = true) { every { hasAudio() } returns true }
        val mockTrackWithoutAudio = mockk<Track>(relaxed = true) { every { hasAudio() } returns false }
        every { trackFactory.create(any()) }.returns(mockTrackWithAudio).andThen(mockTrackWithoutAudio)
        repository.createTrack()
        repository.createTrack()

        repository.play()

        verify { mockTrackWithAudio.play() }
        verify(exactly = 0) { mockTrackWithoutAudio.play() }
    }

    @Test
    fun `pause calls pause on all tracks`() = runTest {
        val mockTrack1 = mockk<Track>(relaxed = true)
        val mockTrack2 = mockk<Track>(relaxed = true)
        every { trackFactory.create(any()) }.returns(mockTrack1).andThen(mockTrack2)
        repository.createTrack()
        repository.createTrack()

        repository.pause()

        verify { mockTrack1.pause() }
        verify { mockTrack2.pause() }
    }

    @Test
    fun `stop calls stop on all tracks`() = runTest {
        val mockTrack1 = mockk<Track>(relaxed = true)
        val mockTrack2 = mockk<Track>(relaxed = true)
        every { trackFactory.create(any()) }.returns(mockTrack1).andThen(mockTrack2)
        repository.createTrack()
        repository.createTrack()

        repository.stop()

        verify { mockTrack1.stop() }
        verify { mockTrack2.stop() }
    }

    @Test
    fun `allTracksWerePlayed becomes true only after all tracks complete`() = runTest {
        val callbackSlot1 = slot<() -> Unit>()
        val callbackSlot2 = slot<() -> Unit>()
        val mockTrack1 = mockk<Track>(relaxed = true) {
            every { hasAudio() } returns true
            every { setOnPlaybackCompletedCallback(capture(callbackSlot1)) } just Runs
        }
        val mockTrack2 = mockk<Track>(relaxed = true) {
            every { hasAudio() } returns true
            every { setOnPlaybackCompletedCallback(capture(callbackSlot2)) } just Runs
        }

        every { trackFactory.create(any()) }.returns(mockTrack1).andThen(mockTrack2)
        repository.createTrack()
        repository.createTrack()

        repository.play()

        assertFalse(repository.allTracksWerePlayed().first())

        callbackSlot1.captured.invoke()
        assertFalse(repository.allTracksWerePlayed().first())

        callbackSlot2.captured.invoke()
        assertTrue(repository.allTracksWerePlayed().first())
    }

    @Test
    fun `trimTrack does nothing if track not found`() = runTest {
        val result = repository.trimTrack(999L, 0L, 1000L)
        assertTrue(result.isFailure)
    }

    @Test
    fun `trimTrack creates backup and trims file`() = runTest {
        val trackId = 1L
        // Create a file large enough for trimming in ms
        val originalContent = ByteArray(100 * 1024) { it.toByte() } 
        val tempFile = File.createTempFile("original_track", ".pcm")
        tempFile.writeBytes(originalContent)
        
        val backupPath = tempFile.absolutePath.replace(".pcm", "_original.pcm")
        val backupFile = File(backupPath)

        val mockTrack = mockk<Track>(relaxed = true) {
            every { id } returns trackId
            every { path } returns tempFile.absolutePath
            every { originalPath } returns backupPath
        }
        
        every { trackFactory.create(any()) } returns mockTrack
        repository.createTrack()

        val startMs = 100L
        val endMs = 500L
        val result = repository.trimTrack(trackId, startMs, endMs)

        assertTrue("trimTrack should succeed", result.isSuccess)
        assertTrue("Backup file should be created", backupFile.exists())
        assertTrue("Backup file content should match original", originalContent.contentEquals(backupFile.readBytes()))
        
        val sampleRate = 44100
        val bytesPerSample = 2

        val startSamples = (startMs * sampleRate / 1000f).roundToLong()
        val endSamples = (endMs * sampleRate / 1000f).roundToLong()

        val startByte = startSamples * bytesPerSample
        val endByte = endSamples * bytesPerSample
        val expectedTrimmedSize = endByte - startByte
        
        assertEquals("Trimmed file should have new size", expectedTrimmedSize, tempFile.length())

        tempFile.delete()
        backupFile.delete()
    }

    @Test
    fun `undoTrim restores from backup and deletes it`() = runTest {
        val trackId = 1L
        val originalContent = ByteArray(100 * 1024)
        val tempFile = File.createTempFile("original_track_for_undo", ".pcm")
        tempFile.writeBytes(originalContent)
        
        val backupPath = tempFile.absolutePath + ".bak"
        val backupFile = File(backupPath)

        val mockTrack = mockk<Track>(relaxed = true) {
            every { id } returns trackId
            every { path } returns tempFile.absolutePath
            every { originalPath } returns backupPath
        }
        
        every { trackFactory.create(any()) } returns mockTrack
        repository.createTrack()
        
        // Create a backup manually for the test
        tempFile.copyTo(backupFile, overwrite = true)
        tempFile.writeBytes(ByteArray(10)) // Simulate a trimmed file
        
        assertTrue("Pre-condition: Backup file must exist", backupFile.exists())
        assertFalse("Pre-condition: Files should be different", originalContent.contentEquals(tempFile.readBytes()))

        val result = repository.undoTrim(trackId)
        
        assertTrue(result.isSuccess)
        assertFalse("Backup file should be deleted after undo", backupFile.exists())
        assertTrue("File content should be restored", originalContent.contentEquals(tempFile.readBytes()))

        tempFile.delete()
    }

    @Test
    fun `undoTrim fails if no backup exists`() = runTest {
        val trackId = 1L
        val tempFile = File.createTempFile("no_backup_track", ".pcm")
        tempFile.writeBytes(ByteArray(100))

        val backupPath = tempFile.absolutePath + ".bak"
        val backupFile = File(backupPath)

        val mockTrack = mockk<Track>(relaxed = true) {
            every { id } returns trackId
            every { path } returns tempFile.absolutePath
            every { originalPath } returns backupPath
        }

        every { trackFactory.create(any()) } returns mockTrack
        repository.createTrack()

        assertFalse("Pre-condition: Backup file should not exist", backupFile.exists())
        
        val result = repository.undoTrim(trackId)

        assertTrue(result.isFailure)
        assertEquals("File should not have changed", 100L, tempFile.length())

        tempFile.delete()
    }
}
