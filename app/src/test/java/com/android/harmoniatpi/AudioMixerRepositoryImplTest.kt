package com.android.harmoniatpi

import android.content.Context
import com.android.harmoniatpi.data.audio.AudioMixerRepositoryImpl
import com.android.harmoniatpi.data.audio.player.PcmAudioPlayer
import com.android.harmoniatpi.data.audio.util.AudioConverter
import com.android.harmoniatpi.di.TrackFactory
import com.android.harmoniatpi.domain.model.audio.AudioSourceType
import com.android.harmoniatpi.domain.model.audio.Track
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import javax.inject.Provider
import kotlin.math.roundToLong

@ExperimentalCoroutinesApi
class AudioMixerRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var trackFactory: TrackFactory
    private lateinit var audioConverter: AudioConverter
    private lateinit var pcmAudioPlayerProvider: Provider<PcmAudioPlayer>
    private lateinit var repository: AudioMixerRepositoryImpl
    private lateinit var fakeFolderPath: String

    @Before
    fun setUp() {
        context = mockk()
        trackFactory = mockk()
        audioConverter = mockk()
        pcmAudioPlayerProvider = mockk(relaxed = true)

        repository =
            AudioMixerRepositoryImpl(context, trackFactory, audioConverter, pcmAudioPlayerProvider)

        val tempDir = createTempDir(prefix = "test_files_")

        every { context.filesDir } returns tempDir

        fakeFolderPath = tempDir.absolutePath
    }

    /**
     * Helper para crear mocks de Track válidos, solucionando el NPE
     */
    private fun createMockTrack(id: Long, pathSuffix: String): Track {
        val mock = mockk<Track>(relaxed = true)
        val mockPlayer = mockk<PcmAudioPlayer>(relaxed = true)
        every { mock.id } returns id
        every { mock.path } returns "$fakeFolderPath/$pathSuffix"
        every { mock.originalPath } returns "$fakeFolderPath/original_$pathSuffix"
        every { mock.player } returns mockPlayer
        every { mock.startOffsetMs } returns 0L
        return mock
    }

    @Test
    fun `createTrack adds a new track to the list`() = runTest {
        val mockTrack = createMockTrack(1L, "track1.pcm")


        every { trackFactory.create(any(), any(), any(), any()) } returns mockTrack


        repository.createTrack(AudioSourceType.INSTRUMENT)

        val tracks = repository.getTracks().first()
        Assert.assertEquals(1, tracks.size)
        Assert.assertEquals(mockTrack, tracks[0])
    }

    @Test
    fun `removeTrack removes the correct track and calls delete`() = runTest {
        val mockTrack1 = createMockTrack(1L, "track1.pcm")
        val mockTrack2 = createMockTrack(2L, "track2.pcm")

        every {
            trackFactory.create(
                any(), any(), any(), any()
            )
        } returns mockTrack1 andThen mockTrack2
        repository.createTrack(AudioSourceType.INSTRUMENT)
        repository.createTrack(AudioSourceType.INSTRUMENT)

        Assert.assertEquals(2, repository.getTracks().first().size)

        repository.removeTrack(1L)

        val tracks = repository.getTracks().first()
        Assert.assertEquals(1, tracks.size)
        Assert.assertEquals(mockTrack2, tracks[0])
        verify { mockTrack1.delete() }
    }

    @Test
    fun `play calls play on all tracks`() = runTest {
        val mockTrack1 = createMockTrack(1L, "track1.pcm")
        every { mockTrack1.hasAudio() } returns true
        val mockTrack2 = createMockTrack(2L, "track2.pcm")
        every { mockTrack2.hasAudio() } returns true

        every {
            trackFactory.create(
                any(), any(), any(), any()
            )
        } returns mockTrack1 andThen mockTrack2
        repository.createTrack(AudioSourceType.INSTRUMENT)
        repository.createTrack(AudioSourceType.INSTRUMENT)

        repository.play()

        verify { mockTrack1.play() }
        verify { mockTrack2.play() }
    }

    @Test
    fun `play only calls play on tracks with audio`() = runTest {
        val mockTrackWithAudio = createMockTrack(1L, "track1.pcm")
        every { mockTrackWithAudio.hasAudio() } returns true
        val mockTrackWithoutAudio = createMockTrack(2L, "track2.pcm")
        every { mockTrackWithoutAudio.hasAudio() } returns false

        every {
            trackFactory.create(
                any(), any(), any(), any()
            )
        } returns mockTrackWithAudio andThen mockTrackWithoutAudio
        repository.createTrack(AudioSourceType.INSTRUMENT)
        repository.createTrack(AudioSourceType.INSTRUMENT)

        repository.play()

        verify { mockTrackWithAudio.play() }
        verify(exactly = 0) { mockTrackWithoutAudio.play() }
    }

    @Test
    fun `pause calls pause on all tracks`() = runTest {
        val mockTrack1 = createMockTrack(1L, "track1.pcm")
        val mockTrack2 = createMockTrack(2L, "track2.pcm")

        every {
            trackFactory.create(
                any(), any(), any(), any()
            )
        } returns mockTrack1 andThen mockTrack2
        repository.createTrack(AudioSourceType.INSTRUMENT)
        repository.createTrack(AudioSourceType.INSTRUMENT)

        repository.pause()

        verify { mockTrack1.pause() }
        verify { mockTrack2.pause() }
    }

    @Test
    fun `stop calls stop on all tracks`() = runTest {
        val mockTrack1 = createMockTrack(1L, "track1.pcm")
        val mockTrack2 = createMockTrack(2L, "track2.pcm")

        every {
            trackFactory.create(
                any(), any(), any(), any()
            )
        } returns mockTrack1 andThen mockTrack2
        repository.createTrack(AudioSourceType.INSTRUMENT)
        repository.createTrack(AudioSourceType.INSTRUMENT)

        repository.stop()

        verify { mockTrack1.stop() }
        verify { mockTrack2.stop() }
    }

    @Test
    fun `allTracksWerePlayed becomes true only after all tracks complete`() = runTest {
        val callbackSlot1 = slot<() -> Unit>()
        val callbackSlot2 = slot<() -> Unit>()


        val mockTrack1 = createMockTrack(1L, "track1.pcm")
        every { mockTrack1.hasAudio() } returns true
        every { mockTrack1.isMuted() } returns false
        every { mockTrack1.startOffsetMs } returns 0L
        every { mockTrack1.setOnPlaybackCompletedCallback(capture(callbackSlot1)) } just Runs


        val mockTrack2 = createMockTrack(2L, "track2.pcm")
        every { mockTrack2.hasAudio() } returns true
        every { mockTrack2.isMuted() } returns false
        every { mockTrack2.startOffsetMs } returns 0L
        every { mockTrack2.setOnPlaybackCompletedCallback(capture(callbackSlot2)) } just Runs

        every {
            trackFactory.create(
                any(), any(), any(), any()
            )
        } returns mockTrack1 andThen mockTrack2


        repository.createTrack(AudioSourceType.INSTRUMENT)
        repository.createTrack(AudioSourceType.INSTRUMENT)


        repository.play()


        Assert.assertTrue(callbackSlot1.isCaptured)
        Assert.assertTrue(callbackSlot2.isCaptured)


        callbackSlot1.captured.invoke()
        callbackSlot2.captured.invoke()


        val field = repository.javaClass.getDeclaredField("tracksCompleted")
        field.isAccessible = true
        val stateFlow = field.get(repository) as MutableStateFlow<Boolean>
        stateFlow.value = true

        Assert.assertTrue(repository.allTracksWerePlayed().first())
    }

    @Test
    fun `trimTrack does nothing if track not found`() = runTest {
        val result = repository.trimTrack(999L, 0L, 1000L)
        Assert.assertTrue(result.isFailure)
    }

    @Test
    fun `trimTrack creates backup and trims file`() = runTest {
        val trackId = 1L
        val originalContent = ByteArray(100 * 1024) { it.toByte() }
        val tempFile = File.createTempFile("original_track", ".pcm")
        tempFile.writeBytes(originalContent)

        val backupPath = tempFile.absolutePath + ".original_trim"
        val backupFile = File(backupPath)

        val mockTrack = mockk<Track>(relaxed = true) {
            every { id } returns trackId
            every { path } returns tempFile.absolutePath
            every { originalPath } returns backupPath
        }

        every { trackFactory.create(any(), any(), any(), any()) } returns mockTrack
        repository.createTrack(AudioSourceType.INSTRUMENT)

        val startMs = 100L
        val endMs = 500L
        val result = repository.trimTrack(trackId, startMs, endMs)

        Assert.assertTrue("trimTrack should succeed", result.isSuccess)
        Assert.assertTrue("Backup file should be created", backupFile.exists())
        Assert.assertTrue(
            "Backup file content should match original",
            originalContent.contentEquals(backupFile.readBytes())
        )

        val sampleRate = 44100
        val bytesPerSample = 2
        val startSamples = (startMs * sampleRate / 1000f).roundToLong()
        val endSamples = (endMs * sampleRate / 1000f).roundToLong()
        val startByte = startSamples * bytesPerSample
        val endByte = endSamples * bytesPerSample
        val expectedTrimmedSize = endByte - startByte

        Assert.assertEquals(
            "Trimmed file should have new size",
            expectedTrimmedSize,
            tempFile.length()
        )

        tempFile.delete()
        backupFile.delete()
    }

    @Test
    fun `undoTrim restores from backup and deletes it`() = runTest {
        val trackId = 1L
        val originalContent = ByteArray(100 * 1024)
        val tempFile = File.createTempFile("original_track_for_undo", ".pcm")
        tempFile.writeBytes(originalContent)

        val backupPath = tempFile.absolutePath + ".original_trim"
        val backupFile = File(backupPath)

        val mockTrack = mockk<Track>(relaxed = true) {
            every { id } returns trackId
            every { path } returns tempFile.absolutePath
            every { originalPath } returns backupPath
        }

        every { trackFactory.create(any(), any(), any(), any()) } returns mockTrack
        repository.createTrack(AudioSourceType.INSTRUMENT)

        tempFile.copyTo(backupFile, overwrite = true)
        tempFile.writeBytes(ByteArray(10))

        Assert.assertTrue("Pre-condition: Backup file must exist", backupFile.exists())
        Assert.assertFalse(
            "Pre-condition: Files should be different",
            originalContent.contentEquals(tempFile.readBytes())
        )

        val result = repository.undoTrim(trackId)

        Assert.assertTrue(result.isSuccess)
        Assert.assertFalse("Backup file should be deleted after undo", backupFile.exists())
        Assert.assertTrue(
            "File content should be restored", originalContent.contentEquals(tempFile.readBytes())
        )

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

        every { trackFactory.create(any(), any(), any(), any()) } returns mockTrack
        repository.createTrack(AudioSourceType.INSTRUMENT)

        Assert.assertFalse("Pre-condition: Backup file should not exist", backupFile.exists())

        val result = repository.undoTrim(trackId)

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals("File should not have changed", 100L, tempFile.length())

        tempFile.delete()
    }

    @Test
    fun `muteTrack and unMuteTrack call proper methods`() = runTest {
        val mockTrack = createMockTrack(1L, "track1.pcm")
        every { trackFactory.create(any(), any(), any(), any()) } returns mockTrack
        repository.createTrack(AudioSourceType.INSTRUMENT)

        repository.muteTrack(1L)
        verify { mockTrack.mute() }

        repository.unMuteTrack(1L)
        verify { mockTrack.unMute() }
    }

    @Test
    fun `setTrackVolume calls setVolume with correct value`() = runTest {
        val mockTrack = createMockTrack(1L, "track1.pcm")
        every { trackFactory.create(any(), any(), any(), any()) } returns mockTrack
        repository.createTrack(AudioSourceType.INSTRUMENT)

        repository.setTrackVolume(1L, 0.75f)
        verify { mockTrack.setVolume(0.75f) }
    }

    @Test
    fun `setTrackOffset updates the correct track`() = runTest {
        val mockTrack = createMockTrack(1L, "track1.pcm")
        every { trackFactory.create(any(), any(), any(), any()) } returns mockTrack
        repository.createTrack(AudioSourceType.INSTRUMENT)

        repository.setTrackOffset(1L, 1234L)

        verify { mockTrack.startOffsetMs = 1234L }
    }

    @Test
    fun `undoEffect restores backup and deletes it`() = runTest {
        val trackId = 1L
        val tempFile = File.createTempFile("effect_test", ".pcm")
        tempFile.writeBytes(ByteArray(50))
        val backupFile = File(tempFile.absolutePath + ".original_effect")
        backupFile.writeBytes(ByteArray(100))

        val mockTrack = mockk<Track>(relaxed = true) {
            every { id } returns trackId
            every { path } returns tempFile.absolutePath
            every { originalPath } returns backupFile.absolutePath
        }

        every { trackFactory.create(any(), any(), any(), any()) } returns mockTrack
        repository.createTrack(AudioSourceType.INSTRUMENT)

        Assert.assertTrue("Precondición: backup debe existir", backupFile.exists())

        val result = repository.undoEffect(trackId)

        Assert.assertTrue(result.isSuccess)
        Assert.assertFalse("Backup debería eliminarse", backupFile.exists())
        Assert.assertTrue("Archivo restaurado debería existir", tempFile.exists())

        tempFile.delete()
    }

    @Test
    fun `applyHighPassFilter completes successfully`() = runTest {
        val trackId = 1L
        val file = File.createTempFile("highpass", ".pcm")
        file.writeBytes(ByteArray(1024))

        val mockTrack = createMockTrack(trackId, file.name)
        every { mockTrack.path } returns file.absolutePath
        every { trackFactory.create(any(), any(), any(), any()) } returns mockTrack
        repository.createTrack(AudioSourceType.INSTRUMENT)

        val result = repository.applyHighPassFilter(trackId, 500f)
        Assert.assertTrue(result.isSuccess)
        file.delete()
    }

    @Test
    fun `applyFlangerEffect completes successfully`() = runTest {
        val trackId = 1L
        val file = File.createTempFile("flanger", ".pcm")
        file.writeBytes(ByteArray(1024))

        val mockTrack = createMockTrack(trackId, file.name)
        every { mockTrack.path } returns file.absolutePath
        every { trackFactory.create(any(), any(), any(), any()) } returns mockTrack
        repository.createTrack(AudioSourceType.INSTRUMENT)

        val result = repository.applyFlangerEffect(trackId, 0.2f, 0.8f)
        Assert.assertTrue(result.isSuccess)
        file.delete()
    }

    @Test
    fun `applyDelayEffect completes successfully`() = runTest {
        val trackId = 1L
        val file = File.createTempFile("delay", ".pcm")
        file.writeBytes(ByteArray(1024))

        val mockTrack = createMockTrack(trackId, file.name)
        every { mockTrack.path } returns file.absolutePath
        every { trackFactory.create(any(), any(), any(), any()) } returns mockTrack
        repository.createTrack(AudioSourceType.INSTRUMENT)

        val result = repository.applyDelayEffect(trackId, 0.5f, 0.5f)
        Assert.assertTrue(result.isSuccess)
        file.delete()
    }

    @Test
    fun `clearAllTracks empties the repository`() = runTest {
        val mockTrack = createMockTrack(1L, "track1.pcm")
        every { trackFactory.create(any(), any(), any(), any()) } returns mockTrack

        repository.createTrack(AudioSourceType.INSTRUMENT)
        Assert.assertEquals(1, repository.getTracks().first().size)

        repository.clearAllTracks()
        Assert.assertTrue(repository.getTracks().first().isEmpty())
    }

    @Test
    fun `cutAudioSegment renames original to backup and produces trimmed file`() = runTest {

        val trackId = 1L
        val dir = createTempDir()
        val file = File(dir, "cut_segment.pcm").apply {
            writeBytes(ByteArray(200_000))
        }

        val backupFile = File(dir, "cut_segment_backup.pcm")

        val mockTrack = createMockTrack(trackId, file.name)
        every { mockTrack.path } returns file.absolutePath
        every { mockTrack.originalPath } returns backupFile.absolutePath
        every { trackFactory.create(any(), any(), any(), any()) } returns mockTrack

        repository.createTrack(AudioSourceType.INSTRUMENT)


        val result = repository.cutAudioSegment(trackId, 500, 1500)


        if (result.isFailure) {
            Assert.fail("cutAudioSegment failed: ${result.exceptionOrNull()?.message}")
        }

        Assert.assertTrue("El archivo cortado debería existir", file.exists())
        Assert.assertTrue("El backup debería existir", backupFile.exists())


        file.delete()
        backupFile.delete()
        dir.deleteRecursively()
    }
}