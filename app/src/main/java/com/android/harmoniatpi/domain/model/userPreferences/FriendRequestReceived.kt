package com.android.harmoniatpi.domain.model.userPreferences

data class FriendRequestReceived(
    val idRequest : Int,
    val fromUserID : String,
    val status : Boolean = false
)