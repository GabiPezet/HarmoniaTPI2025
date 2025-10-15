package com.android.harmoniatpi.domain.model.song

import com.android.harmoniatpi.domain.model.user.User

data class DerivedVersion(
    val id: String,
    val creator: User,
    val projectId: String?
)