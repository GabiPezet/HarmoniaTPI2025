package com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.model

import com.android.harmoniatpi.domain.model.userPreferences.Post

data class CommunityUiState(
    val isLoading: Boolean = false,
    val showCreateDialog: Boolean = false,
    val userName: String = "",
    val userLastName: String = "",
    val posts: List<Post> = emptyList()
)
