package com.android.harmoniatpi.ui.screens.menuPrincipal.content.model

import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.domain.model.userPreferences.AppTheme
import com.android.harmoniatpi.domain.model.userPreferences.Friend
import com.android.harmoniatpi.domain.model.userPreferences.FriendRequestReceived
import com.android.harmoniatpi.domain.model.userPreferences.FriendRequestSending
import com.android.harmoniatpi.domain.model.userPreferences.Post
import com.android.harmoniatpi.ui.screens.notificationScreen.model.NotificationHarmonia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

data class MenuUiState(
    val userEmail: String = "",
    val userID: String = "",
    val userName : String = "",
    val userLastName : String = "",
    val userPhotoPath: String = "",
    val userPhotoPathRemote: String = "",
    val isLoading: Boolean = false,
    val logOutSuccess: Boolean = false,
    val appTheme: AppTheme = AppTheme.LIGHT,
    val optionsMenu: OptionsMenu = OptionsMenu.MAIN_CONTENT_SCREEN,
    val notificationsList: List<NotificationHarmonia> = emptyList(),
    val friendsList: List<Friend> = emptyList(),
    val projectsList: List<Project> = emptyList(),
    val myPostsList: List<Post> = emptyList(),
    val friendRequestReceived: List<FriendRequestReceived> = emptyList(),
    val friendRequestSent: List<FriendRequestSending> = emptyList(),
    val newNotification: Boolean = false,
    val showNewNotification : Boolean = false,
    val internetAvailable : Boolean = true,
    val instrument: String = "",
    val genres: String = "",
    val location : String = "",
    val rating : Float =  0.0f,
    val listProjects : List<Project> = emptyList(),
    val cloningPostId: String? = null,
    val currentUserData: UserPreferences? = null,
    val isSendingFollowRequest: Boolean = false
)

@Singleton
class SharedMenuUiState @Inject constructor() {
    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState = _uiState.asStateFlow()

    fun updateState(transform: (MenuUiState) -> MenuUiState) {
        _uiState.update { currentState -> transform(currentState) }
    }
}

enum class OptionsMenu {
    USER_PREFERENCES_SCREEN,
    MAIN_CONTENT_SCREEN,
    USER_PROFILE,
    MY_POSTS_SCREEN
}

