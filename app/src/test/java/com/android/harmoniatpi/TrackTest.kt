package com.android.harmoniatpi

import com.android.harmoniatpi.domain.interfaces.AudioPlayer
import com.android.harmoniatpi.domain.model.audio.AudioSourceType
import com.android.harmoniatpi.domain.model.audio.Track
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class TrackTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var mockPlayer: AudioPlayer
    private lateinit var track: Track
    private lateinit var folderPath: String

    @Before
    fun setUp() {
        mockPlayer = mockk(relaxed = true)
        folderPath = tempFolder.newFolder().absolutePath

        every { mockPlayer.setFile(any()) } just runs
        every { mockPlayer.play() } returns Result.success(Unit)
        every { mockPlayer.playSegment(any(), any()) } returns Result.success(Unit)

        track = Track(
            folderPath = folderPath,
            existingFilePath = null,
            idExist = null,
            sourceType = AudioSourceType.VOICE,
            player = mockPlayer
        )

    }

    @After
    fun tearDown() {
        tempFolder.delete()
    }

    @Test
    fun `init calls setFile on player`() {
        val freshMockPlayer = mockk<AudioPlayer>()
        val pathSlot = slot<String>()
        every { freshMockPlayer.setFile(capture(pathSlot)) } just runs

        val newTrack = Track(
            folderPath = folderPath,
            existingFilePath = null,
            idExist = null,
            sourceType = AudioSourceType.VOICE,
            player = freshMockPlayer
        )

        verify(exactly = 1) { freshMockPlayer.setFile(any()) }
        // Verificamos que el path capturado es el mismo que generó la clase Track
        Assert.assertEquals(newTrack.path, pathSlot.captured)
        Assert.assertTrue(pathSlot.captured.startsWith(folderPath))
        Assert.assertTrue(pathSlot.captured.endsWith(".pcm"))
    }

    @Test
    fun `init uses existingFilePath when provided`() {
        val freshMockPlayer = mockk<AudioPlayer>()
        val pathSlot = slot<String>()
        every { freshMockPlayer.setFile(capture(pathSlot)) } just runs

        val existingPath = "$folderPath/mi_audio_existente.pcm"

        val newTrack = Track(
            folderPath = folderPath,
            existingFilePath = existingPath,
            idExist = 123L,
            sourceType = AudioSourceType.VOICE,
            player = freshMockPlayer,
        )

        // Verificamos que se usó el path exacto que le pasamos
        verify(exactly = 1) { freshMockPlayer.setFile(existingPath) }
        Assert.assertEquals(existingPath, pathSlot.captured)
        Assert.assertEquals(existingPath, newTrack.path)
        Assert.assertEquals(123L, newTrack.id) // Verificamos que el ID se asignó
    }

    @Test
    fun `play calls play on player`() {
        track.play()
        verify(exactly = 1) { mockPlayer.play() }
    }

    @Test
    fun `playSegment calls playSegment on player`() {
        val startMs = 1000L
        val endMs = 5000L
        track.playSegment(startMs, endMs)
        verify(exactly = 1) { mockPlayer.playSegment(startMs, endMs) }
    }

    @Test
    fun `pause calls pause on player`() {
        track.pause()
        verify(exactly = 1) { mockPlayer.pause() }
    }

    @Test
    fun `stop calls stop on player`() {
        track.stop()
        verify(exactly = 1) { mockPlayer.stop() }
    }

    @Test
    fun `setOnPlaybackCompletedCallback sets callback on player`() {
        val callbackSlot = slot<() -> Unit>()
        every { mockPlayer.setOnPlaybackCompletedCallback(capture(callbackSlot)) } just runs

        val myCallback = { }
        track.setOnPlaybackCompletedCallback(myCallback)

        verify(exactly = 1) { mockPlayer.setOnPlaybackCompletedCallback(any()) }
    }

    @Test
    fun `delete calls stop, release and deletes file`() {
        val trackFile = File(track.path)
        trackFile.createNewFile()
        Assert.assertTrue(trackFile.exists())

        track.delete()

        verify(exactly = 1) { mockPlayer.stop() }
        verify(exactly = 1) { mockPlayer.release() }
        Assert.assertFalse(trackFile.exists())
    }

    @Test
    fun `hasAudio returns false when file does not exist`() {
        val trackFile = File(track.path)
        if (trackFile.exists()) {
            trackFile.delete()
        }
        Assert.assertFalse(track.hasAudio())
    }

    @Test
    fun `hasAudio returns false when file is empty`() {
        val trackFile = File(track.path)
        trackFile.createNewFile()
        Assert.assertTrue(trackFile.exists())
        Assert.assertEquals(0, trackFile.length())

        Assert.assertFalse(track.hasAudio())
    }

    @Test
    fun `hasAudio returns true when file has content`() {
        val trackFile = File(track.path)
        trackFile.writeBytes(ByteArray(10))
        Assert.assertTrue(trackFile.exists())
        Assert.assertTrue(trackFile.length() > 0)

        Assert.assertTrue(track.hasAudio())
    }
}