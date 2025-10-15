package com.android.harmoniatpi.domain.model.song

import com.android.harmoniatpi.domain.model.user.User

data class Song(
    val id: String,
    val title: String,
    val creator: User,
    val imageUrl: String?,
    val audioUrl: String,
    val durationMillis: Long,
    val projectId: String? = null,
    val versionType: VersionType = VersionType.ORIGINAL
)
