package com.android.harmoniatpi.domain.model.userPreferences

data class Project(
    val id: String,
    val name: String,
    val lastName : String,
    val description: String,
    val duration : String,
    val createdAt : String,
    val status : Boolean,
    val urlCompleteAudio : String,
    val urlAudioTracks : List<String> = emptyList(),
    val likes : Int,
    val comments : List<Comment> = emptyList(),
    val totalShared : Int
)