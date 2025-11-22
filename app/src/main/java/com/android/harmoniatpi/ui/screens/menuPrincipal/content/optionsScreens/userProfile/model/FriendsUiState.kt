package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile.model

import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.userPreferences.Friend
import com.android.harmoniatpi.domain.model.userPreferences.FriendRequestReceived

data class FriendsUiState(
    val isLoading: Boolean = true,
    val friendsList: List<Friend> = emptyList(),
    val requestList: List<UserPreferences> = emptyList(),
    val friendRequestReceived: List<FriendRequestReceived> = emptyList(),
    val loadingActionId: String? = null
)