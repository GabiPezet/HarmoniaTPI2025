package com.android.harmoniatpi.helperUtil

import com.android.harmoniatpi.domain.model.audio.AudioSourceType
import com.android.harmoniatpi.domain.model.project.AudioTrack
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.domain.model.song.DerivedVersion
import com.android.harmoniatpi.domain.model.song.Song
import com.android.harmoniatpi.domain.model.song.SongDetails
import com.android.harmoniatpi.domain.model.song.VersionType
import com.android.harmoniatpi.domain.model.user.User
import com.android.harmoniatpi.domain.model.userPreferences.Comment

object MockHelperTestUnit {

    val mockAudioTracks = listOf(
        AudioTrack(
            id = 1L,
            path = "/storage/emulated/0/Music/audio1.mp3",
            title = "Track de Prueba 1",
            sourceType = AudioSourceType.INSTRUMENT,
            selected = false,
            waveForm = listOf(0.1f, 0.3f, 0.5f, 0.2f),
            durationMs = 120000L,
            isUndoAvailable = false,
            isMuted = false,
            volume = 1f,
            startOffsetMs = 0L,
            trimStartMs = 0L,
            trimEndMs = 119000L,
            remoteUrl = null
        ),
        AudioTrack(
            id = 2L,
            path = "/storage/emulated/0/Music/audio2.wav",
            title = "Track de Prueba 2",
            sourceType = AudioSourceType.INSTRUMENT,
            selected = true,
            waveForm = listOf(0.2f, 0.4f, 0.6f, 0.3f),
            durationMs = 90000L,
            isUndoAvailable = true,
            isMuted = false,
            volume = 0.8f,
            startOffsetMs = 500L,
            trimStartMs = 1000L,
            trimEndMs = 88000L,
            remoteUrl = null
        ),
        AudioTrack(
            id = 3L,
            path = "",
            title = "Track Remoto",
            sourceType = AudioSourceType.INSTRUMENT,
            selected = false,
            waveForm = null,
            durationMs = 60000L,
            isUndoAvailable = false,
            isMuted = true,
            volume = 0f,
            startOffsetMs = 0L,
            trimStartMs = 0L,
            trimEndMs = -1L,
            remoteUrl = "https://example.com/audio3.mp3"
        )
    )

    fun createProject(
        id: String = "123",
        name: String = "Proyecto Test",
        description: String = "Descripción de Prueba",
        ownerId: String = "123",
        title: String = "Titulo de Prueba",
        createdAt: String = "2023-08-22T15:30:00.000Z",
        status: Boolean = true,
        likes: Int = 10,
        totalShared: Int = 5,
        comments: List<Comment> = emptyList(),
        urlCompleteAudio: String = "https://example.com/audio.mp3",
        urlAudioTracks: List<AudioTrack> = mockAudioTracks,
        hashtags: List<String> = emptyList(),
        forkedByUserIds: List<String> = emptyList(),
        originalProjectId: String? = null,
        isPublished: Boolean = false,
        duration: Long = 0L,
        imageUrl: String? = null,
        lastName: String = ""
    ): Project {
        return Project(
            id = id,
            name = name,
            description = description,
            ownerId = ownerId,
            title = title,
            createdAt = createdAt,
            status = status,
            likes = likes,
            totalShared = totalShared,
            comments = comments,
            urlCompleteAudio = urlCompleteAudio,
            urlAudioTracks = urlAudioTracks,
            hashtags = hashtags,
            forkedByUserIds = forkedByUserIds,
            originalProjectId = originalProjectId,
            isPublished = isPublished,
            duration = duration,
            imageUrl = imageUrl,
            lastName = lastName
        )
    }


    // ------- MOCK USER -------
    fun mockUser(
        id: String = "user123",
        name: String = "Juan Pérez",
        email: String = "juan@test.com"
    ): User {
        return User(
            id = id,
            name = name,
            avatarUrl = email
        )
    }


    // ------- MOCK SONG -------
    fun mockSong(
        id: String = "song123",
        title: String = "Canción Test",
        durationMs: Long = 120000L
    ): Song {
        return Song(
            id = id,
            title = title,
            durationMillis = durationMs,
            creator = mockUser(),
            imageUrl = "https://example.com/image.jpg",
            audioUrl = "https://example.com/audio.mp3",
            projectId = null,
            versionType = VersionType.ORIGINAL
        )
    }

    // ------- MOCK DERIVED VERSION -------
    fun mockDerivedVersion(
        id: String = "derived123",
        creator: User = mockUser(),
        projectId: String? = "projectABC",
        audioUrl: String? = "https://example.com/audio.mp3",
        durationMillis: Long = 60000L
    ): DerivedVersion {
        return DerivedVersion(
            id = id,
            creator = creator,
            projectId = projectId,
            audioUrl = audioUrl,
            durationMillis = durationMillis
        )
    }

    // ------- MOCK SONG DETAILS -------
    fun mockSongDetails(): SongDetails {
        return SongDetails(
            originalSong = mockSong(),
            derivedVersions = listOf(
                mockDerivedVersion(id = "v1"),
                mockDerivedVersion(id = "v2")
            )
        )
    }
}