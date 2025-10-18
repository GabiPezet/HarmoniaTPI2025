package com.android.harmoniatpi.domain.model.project

import com.android.harmoniatpi.data.database.entities.ProjectEntity
import com.android.harmoniatpi.di.util.JsonUtils
import com.android.harmoniatpi.domain.model.userPreferences.Comment

data class Project(
    val id: String,
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
    val urlCompleteAudio: AudioTrack? = null,
    val urlAudioTracks: List<AudioTrack> = emptyList(),
    val hashtags: List<String>,
) {
    fun toDataBase(jsonUtils: JsonUtils) = ProjectEntity(
        id = id,
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
        urlCompleteAudio = if (urlCompleteAudio != null) jsonUtils.encodeToJson(urlCompleteAudio) else null,
        urlAudioTracks = jsonUtils.encodeToJson(urlAudioTracks),
        hashtags = jsonUtils.encodeToJson(hashtags)
    )
}