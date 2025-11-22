package com.android.harmoniatpi.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.harmoniatpi.di.util.JsonUtils
import com.android.harmoniatpi.domain.model.project.CloningAccess
import com.android.harmoniatpi.domain.model.userPreferences.Comment
import com.android.harmoniatpi.domain.model.userPreferences.Post

@Entity(tableName = "MyPostEntity")
data class MyPostEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "userID")
    val userID: String,
    @ColumnInfo(name = "userImagePathURL")
    val userImagePathURL: String = "",
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "lasName")
    val lasName: String,
    @ColumnInfo(name = "hashtags")
    val hashtags: String = "",
    @ColumnInfo(name = "idProject")
    val idProject: String = "",
    @ColumnInfo(name = "urlCompleteAudio")
    val urlCompleteAudio: String = "",
    @ColumnInfo(name = "urlAudioTracks")
    val urlAudioTracks: String = "",
    @ColumnInfo(name = "imageUrl")
    val imageUrl: String = "",
    @ColumnInfo(name = "createdAt")
    val createdAt: String,
    @ColumnInfo(name = "likes")
    val likes: Int = 0,
    @ColumnInfo(name = "comments")
    val comments: String = "",
    @ColumnInfo(name = "totalShared")
    val totalShared: Int = 0,
    @ColumnInfo(name = "clonedOption")
    val clonedOption: Boolean = false,
    @ColumnInfo(name = "hasNewComment", defaultValue = "0")
    val hasNewComment: Boolean = false,
    @ColumnInfo(name = "hasNewLike", defaultValue = "0")
    val hasNewLike: Boolean = false,
    @ColumnInfo(name = "hasNewClone", defaultValue = "0")
    val hasNewClone: Boolean = false,
    @ColumnInfo(name = "cloningAccess", defaultValue = "0")
    val cloningAccess: CloningAccess = CloningAccess.PUBLIC
) {
    fun toDomain(jsonUtils: JsonUtils) = Post(
        id = id,
        userID = userID,
        userImagePathURL = userImagePathURL,
        title = title,
        description = description,
        name = name,
        lasName = lasName,
        hashtags = jsonUtils.decodeJsonToListObject<String>(hashtags),
        idProject = idProject,
        urlCompleteAudio = urlCompleteAudio,
        urlAudioTracks = jsonUtils.decodeJsonToListObject<String>(urlAudioTracks),
        imageUrl = imageUrl,
        createdAt = createdAt,
        likes = likes,
        comments = jsonUtils.decodeJsonToListObject<Comment>(comments),
        totalShared = totalShared,
        clonedOption = clonedOption,
        hasNewComment = hasNewComment,
        hasNewLike = hasNewLike,
        hasNewClone = hasNewClone,
        cloningAccess = cloningAccess
    )
}
