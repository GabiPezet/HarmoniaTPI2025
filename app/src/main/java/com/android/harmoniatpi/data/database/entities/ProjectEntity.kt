package com.android.harmoniatpi.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.harmoniatpi.data.local.model.ProjectFirebaseModel
import com.android.harmoniatpi.di.util.JsonUtils
import com.android.harmoniatpi.domain.model.project.AudioTrack
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.domain.model.userPreferences.Comment

@Entity(tableName = "project")
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    val ownerId: String,
    val name: String,
    val lastName: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val duration: String,
    val createdAt: String,
    val status: Boolean,
    val likes: Int,
    val totalShared: Int,
    val comments: String,
    val urlCompleteAudio: String?,
    val urlAudioTracks: String,
    val hashtags: String,
    val forkedByUserIds: String,
    val originalProjectId: String? = null,
    val isPublished: Boolean,
    val tracksJsonUrl: String? = null
) {
    fun toDomain(jsonUtils: JsonUtils) = Project(
        id = id,
        ownerId = ownerId,
        name = name,
        lastName = lastName,
        title = title,
        description = description,
        imageUrl = imageUrl,
        duration = duration.toLong(),
        createdAt = createdAt,
        status = status,
        likes = likes,
        totalShared = totalShared,
        comments = if (comments.isNotBlank()) jsonUtils.decodeJsonToListObject<Comment>(comments) else emptyList(),
        urlCompleteAudio = urlCompleteAudio,
        urlAudioTracks = if (urlAudioTracks.isNotBlank()) jsonUtils.decodeJsonToListObject<AudioTrack>(urlAudioTracks) else emptyList(),
        hashtags = if (hashtags.isNotBlank()) jsonUtils.decodeJsonToListObject<String>(hashtags) else emptyList(),
        forkedByUserIds = if (forkedByUserIds.isNotBlank()) jsonUtils.decodeJsonToListObject<String>(forkedByUserIds) else emptyList(),
        originalProjectId = originalProjectId,
        isPublished = isPublished,
        tracksJsonUrl = tracksJsonUrl
    )

    fun toFirebaseModel(): ProjectFirebaseModel {
        return ProjectFirebaseModel(
            id = this.id,
            ownerId = this.ownerId,
            name = this.name,
            lastName = this.lastName,
            title = this.title,
            description = this.description,
            imageUrl = this.imageUrl,
            duration = this.duration.toLongOrNull() ?: 0L,
            createdAt = this.createdAt,
            hashtags = this.hashtags,
            forkedByUserIds = this.forkedByUserIds,
            publishedAudioUrl = this.urlCompleteAudio,
            publishedTrackUrls = this.urlAudioTracks,
            likes = this.likes,
            totalShared = this.totalShared,
            isPublished = this.isPublished,
            tracksJsonUrl = this.tracksJsonUrl,
            originalProjectId = this.originalProjectId
        )
    }
}