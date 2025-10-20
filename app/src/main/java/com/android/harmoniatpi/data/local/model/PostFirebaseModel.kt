package com.android.harmoniatpi.data.local.model

import com.android.harmoniatpi.di.util.JsonUtils
import com.android.harmoniatpi.domain.model.userPreferences.Post

data class PostFirebaseModel(
    val id: String = "",
    val userID: String = "",
    val userImagePathURL : String = "",
    val title: String = "",
    val description: String = "",
    val name: String = "",
    val lasName: String = "",
    val hashtags: String = "",
    val idProject: String = "",
    val urlCompleteAudio: String = "",
    val urlAudioTracks: String = "",
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
            userImagePathURL = userImagePathURL,
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
}