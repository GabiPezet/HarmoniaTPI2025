package com.android.harmoniatpi.domain.model.userPreferences

import com.android.harmoniatpi.data.local.model.CommentFirebaseModel

data class Comment(
    val id : String,
    val name : String,
    val lastName : String,
    val comment : String,
    val photoUrlUser : String
){
    fun toCommentFirebaseModel() = CommentFirebaseModel(
        id = id,
        name = name,
        lastName = lastName,
        comment = comment,
        photoUrlUser = photoUrlUser
    )
}