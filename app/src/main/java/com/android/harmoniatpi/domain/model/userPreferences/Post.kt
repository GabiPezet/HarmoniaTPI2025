package com.android.harmoniatpi.domain.model.userPreferences

data class Post(
    val id: String,
    val title: String,
    val description: String,
    val name: String,
    val lasName: String,
    val hashtags: List<String>,
    val urlCompleteAudio: String,
    val urlAudioTracks: List<String> = emptyList(),
    val createdAt: String,
    val likes: Int,
    val comments: List<Comment> = emptyList(),
    val totalShared: Int,
    val clonedOption: Boolean = false
)
