package com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.model

import com.android.harmoniatpi.domain.model.userPreferences.Post

data class CommunityUiState(
    val isLoading: Boolean = false,
    val showCreateDialog: Boolean = false,
    val userName: String = "Cristian",
    val userLastName: String = "Barzabal",
    val posts: List<Post> = emptyList()
)
