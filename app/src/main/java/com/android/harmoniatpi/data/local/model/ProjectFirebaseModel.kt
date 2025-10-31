package com.android.harmoniatpi.data.local.model

import com.android.harmoniatpi.data.database.entities.ProjectEntity
import com.android.harmoniatpi.di.util.JsonUtils
import com.android.harmoniatpi.domain.model.project.Project

data class ProjectFirebaseModel(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val lastName: String = "",
    val title: String = "",
    val description: String = "",
    val duration: Long = 0L,
    val createdAt: String = "",
    val hashtags: String = "",
    val forkedByUserIds: String = "",
    val publishedAudioUrl: String? = null,
    val publishedTrackUrls: String = "",
    val likes: Int = 0,
    val totalShared: Int = 0,
    val isPublished: Boolean = true,
    val originalProjectId: String? = null
) {
    fun toEntity(): ProjectEntity {
        return ProjectEntity(
            id = this.id,
            ownerId = this.ownerId,
            name = this.name,
            lastName = this.lastName,
            title = this.title,
            description = this.description,
            duration = this.duration.toString(),
            createdAt = this.createdAt,
            status = true,
            likes = this.likes,
            totalShared = this.totalShared,
            comments = "[]",
            urlCompleteAudio = this.publishedAudioUrl,
            urlAudioTracks = this.publishedTrackUrls,
            hashtags = this.hashtags,
            forkedByUserIds = this.forkedByUserIds,
            originalProjectId = this.originalProjectId,
            isPublished = this.isPublished
        )
    }
}
