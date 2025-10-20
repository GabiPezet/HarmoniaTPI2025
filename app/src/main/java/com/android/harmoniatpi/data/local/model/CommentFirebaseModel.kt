package com.android.harmoniatpi.data.local.model

import com.android.harmoniatpi.domain.model.userPreferences.Comment

data class CommentFirebaseModel(
    val id: String = "",
    val name: String = "",
    val lastName: String = "",
    val comment: String = "",
    val photoUrlUser: String = ""
) {
    fun toDomain() = Comment(
        id = id,
        name = name,
        lastName = lastName,
        comment = comment,
        photoUrlUser = photoUrlUser
    )
}
