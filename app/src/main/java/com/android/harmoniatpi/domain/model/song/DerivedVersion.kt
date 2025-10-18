package com.android.harmoniatpi.domain.model.song

import com.android.harmoniatpi.domain.model.user.User

/**
 * DerivedVersion representa una versión derivada de una canción.
 */
data class DerivedVersion(
    val id: String,
    val creator: User,
    val projectId: String?,
    val audioUrl: String?,
    val durationMillis: Long?
)