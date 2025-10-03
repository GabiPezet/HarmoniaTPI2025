package com.android.harmoniatpi.ui.screens.menuPrincipal.content.model

import com.android.harmoniatpi.domain.model.userPreferences.AppTheme
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
    val isLoading: Boolean = false,
    val logOutSuccess: Boolean = false,
    val appTheme: AppTheme = AppTheme.LIGHT,
    val optionsMenu: OptionsMenu = OptionsMenu.MAIN_CONTENT_SCREEN,
    val notificationsList: List<NotificationHarmonia> = emptyList(),
    val newNotification: Boolean = false,
    val showNewNotification : Boolean = false,
    val internetAvailable : Boolean = true
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
    USER_PROFILE,
    USER_PREFERENCES_SCREEN,
    MAIN_CONTENT_SCREEN
}

