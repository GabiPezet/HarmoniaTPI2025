package com.android.harmoniatpi.domain.model.userPreferences

import com.android.harmoniatpi.data.database.entities.MyPostEntity
import com.android.harmoniatpi.data.local.model.PostFirebaseModel
import com.android.harmoniatpi.di.util.JsonUtils
import com.android.harmoniatpi.domain.model.project.CloningAccess

data class Post(
    val id: String,
    val userID: String,
    val userImagePathURL: String = "",
    val title: String,
    val description: String,
    val name: String,
    val lasName: String,
    val hashtags: List<String> = emptyList(),
    val idProject: String = "",
    val urlCompleteAudio: String = "",
    val urlAudioTracks: List<String> = emptyList(),
    val imageUrl: String = "",
    val createdAt: String,
    val likes: Int = 0,
    val comments: List<Comment> = emptyList(),
    val totalShared: Int = 0,
    val clonedOption: Boolean = false,
    val hasNewComment: Boolean = false,
    val hasNewLike: Boolean = false,
    val hasNewClone : Boolean = false,
    val cloningAccess: CloningAccess = CloningAccess.PUBLIC
) {
    fun toPostFirebaseModel(jsonUtils: JsonUtils): PostFirebaseModel {
        return PostFirebaseModel(
            id = id,
            userID = userID,
            userImagePathURL = userImagePathURL,
            title = title,
            description = description,
            name = name,
            lasName = lasName,
            hashtags = jsonUtils.encodeToJson(hashtags),
            idProject = idProject,
            urlCompleteAudio = urlCompleteAudio,
            urlAudioTracks = jsonUtils.encodeToJson(urlAudioTracks),
            imageUrl = imageUrl,
            createdAt = createdAt,
            likes = likes,
            comments = comments.map { it.toCommentFirebaseModel() },
            totalShared = totalShared,
            clonedOption = clonedOption,
            cloningAccess = cloningAccess
        )
    }

    fun toDataBase(
        jsonUtils: JsonUtils,
        hasNewComment: Boolean = false,
        hasNewLike: Boolean = false,
        hasNewClone: Boolean = false
    ) = MyPostEntity(
        id = id,
        userID = userID,
        userImagePathURL = userImagePathURL,
        title = title,
        description = description,
        name = name,
        lasName = lasName,
        hashtags = jsonUtils.encodeToJson(hashtags),
        idProject = idProject,
        urlCompleteAudio = urlCompleteAudio,
        urlAudioTracks = jsonUtils.encodeToJson(urlAudioTracks),
        imageUrl = imageUrl,
        createdAt = createdAt,
        likes = likes,
        comments = jsonUtils.encodeToJson(comments),
        totalShared = totalShared,
        clonedOption = clonedOption,
        hasNewComment = hasNewComment,
        hasNewLike = hasNewLike,
        hasNewClone = hasNewClone,
        cloningAccess = cloningAccess
    )
}
