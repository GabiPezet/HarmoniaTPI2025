package com.android.harmoniatpi.data.local.model

import com.android.harmoniatpi.domain.model.userPreferences.Comment

data class CommentFirebaseModel(
    val id: String = "",
    val name: String = "",
    val lastName: String = "",
    val comment: String = "",
    val photoUrlUser: String = "",
    val likes: Int = 0
) {
    fun toDomain() = Comment(id, name, lastName, comment, photoUrlUser, likes)

    companion object {
        fun fromDomain(comment: Comment) = CommentFirebaseModel(
            id = comment.id,
            name = comment.name,
            lastName = comment.lastName,
            comment = comment.comment,
            photoUrlUser = comment.photoUrlUser,
            likes = comment.likes
        )
    }
}
