package com.android.harmoniatpi.domain.model.project

import com.android.harmoniatpi.data.database.entities.ProjectEntity
import com.android.harmoniatpi.di.util.JsonUtils
import com.android.harmoniatpi.domain.model.userPreferences.Comment

data class Project(
    val id: String,
    val ownerId: String,
    val name: String,
    val lastName: String,
    val title: String,
    val description: String,
    val duration: Long,
    val createdAt: String,
    val status: Boolean,
    val likes: Int,
    val totalShared: Int,
    val comments: List<Comment> = emptyList(),
    val urlCompleteAudio: String? = null,
    val urlAudioTracks: List<AudioTrack> = emptyList(),
    val hashtags: List<String>,
    val forkedByUserIds: List<String> = emptyList(),
    val originalProjectId: String? = null,
    val isPublished: Boolean = false
) {
    fun toDataBase(jsonUtils: JsonUtils) = ProjectEntity(
        id = id,
        ownerId = ownerId,
        name = name,
        lastName = lastName,
        title = title,
        description = description,
        duration = duration.toString(),
        createdAt = createdAt,
        status = status,
        likes = likes,
        totalShared = totalShared,
        comments = jsonUtils.encodeToJson(comments),
        urlCompleteAudio = urlCompleteAudio,
        urlAudioTracks = jsonUtils.encodeToJson(urlAudioTracks),
        hashtags = jsonUtils.encodeToJson(hashtags),
        forkedByUserIds = jsonUtils.encodeToJson(forkedByUserIds),
        originalProjectId = originalProjectId,
        isPublished = isPublished
    )
}