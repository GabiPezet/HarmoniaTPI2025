package com.android.harmoniatpi.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
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
    val duration: String,
    val createdAt: String,
    val status: Boolean,
    val likes: Int,
    val totalShared: Int,
    val comments: String,
    val urlCompleteAudio: String?,
    val urlAudioTracks: String,
    val hashtags: String
) {
    fun toDomain(jsonUtils: JsonUtils) = Project(
        id = id,
        ownerId = ownerId,
        name = name,
        lastName = lastName,
        title = title,
        description = description,
        duration = duration.toLong(),
        createdAt = createdAt,
        status = status,
        likes = likes,
        totalShared = totalShared,
        comments = jsonUtils.decodeJsonToListObject<Comment>(comments),
        urlCompleteAudio = if (urlCompleteAudio.isNullOrEmpty()) null else jsonUtils.decodeJsonToObject<AudioTrack>(
            urlCompleteAudio
        ),
        urlAudioTracks = jsonUtils.decodeJsonToListObject<AudioTrack>(urlAudioTracks),
        hashtags = jsonUtils.decodeJsonToListObject<String>(hashtags)
    )
}