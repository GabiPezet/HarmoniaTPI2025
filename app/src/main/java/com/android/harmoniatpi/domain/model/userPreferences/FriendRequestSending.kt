package com.android.harmoniatpi.domain.model.userPreferences

data class FriendRequestSending(
    val idRequest : Int,
    val toUserID : String,
    val status : Boolean = false
)