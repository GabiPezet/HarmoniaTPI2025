package com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.model

import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.domain.model.userPreferences.Post

data class CommunityUiState(
    val isLoading: Boolean = false,
    val showCreateDialog: Boolean = false,
    val userName: String = "",
    val userLastName: String = "",
    val userID : String = "",
    val userPhotoPathRemote : String = "",
    val posts: List<Post> = emptyList(),
    val localProjects: List<Project> = emptyList(),
    val userSelected : UserPreferences? = null,
    val showUserProfile : Boolean = false,
    val cloningPostId: String? = null,
    val currentUserData: UserPreferences? = null,
    val isSendingFollowRequest: Boolean = false,
    val isShowSearchContentCommunity: Boolean = false,
    val searchQueryCommunity: String = ""
)
