package com.android.harmoniatpi.data.local.model

import com.android.harmoniatpi.di.util.JsonUtils
import com.android.harmoniatpi.domain.model.userPreferences.Post

data class PostFirebaseModel(
    val id: String = "",
    val userID: String = "",
    val title: String = "",
    val description: String = "",
    val name: String = "",
    val lasName: String = "",
    val hashtags: String = "", // JSON String
    val idProject: String = "",
    val urlCompleteAudio: String = "",
    val urlAudioTracks: String = "", // JSON String
    val imageUrl: String = "",
    val createdAt: String = "",
    val likes: Int = 0,
    val comments: List<CommentFirebaseModel> = emptyList(),
    val totalShared: Int = 0,
    val clonedOption: Boolean = false
) {
    fun toDomain(jsonUtils: JsonUtils): Post {
        return Post(
            id = id,
            userID = userID,
            title = title,
            description = description,
            name = name,
            lasName = lasName,
            hashtags = jsonUtils.decodeJsonToListObject(hashtags),
            idProject = idProject,
            urlCompleteAudio = urlCompleteAudio,
            urlAudioTracks = jsonUtils.decodeJsonToListObject(urlAudioTracks),
            imageUrl = imageUrl,
            createdAt = createdAt,
            likes = likes,
            comments = comments.map { it.toDomain() },
            totalShared = totalShared,
            clonedOption = clonedOption
        )
    }

    companion object {
        fun fromDomain(post: Post, jsonUtils: JsonUtils): PostFirebaseModel {
            return PostFirebaseModel(
                id = post.id,
                userID = post.userID,
                title = post.title,
                description = post.description,
                name = post.name,
                lasName = post.lasName,
                hashtags = jsonUtils.encodeToJson(post.hashtags),
                idProject = post.idProject,
                urlCompleteAudio = post.urlCompleteAudio,
                urlAudioTracks = jsonUtils.encodeToJson(post.urlAudioTracks),
                imageUrl = post.imageUrl,
                createdAt = post.createdAt,
                likes = post.likes,
                comments = post.comments.map { CommentFirebaseModel.fromDomain(it) },
                totalShared = post.totalShared,
                clonedOption = post.clonedOption
            )
        }
    }
}