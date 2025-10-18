package com.android.harmoniatpi.domain.model.userPreferences

data class Comment(
    val id : String,
    val name : String,
    val lastName : String,
    val comment : String,
    val photoUrlUser : String,
    val likes : Int
)