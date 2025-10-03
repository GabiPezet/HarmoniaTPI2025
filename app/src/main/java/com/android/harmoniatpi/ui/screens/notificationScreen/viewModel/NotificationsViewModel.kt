package com.android.harmoniatpi.ui.screens.notificationScreen.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.usecases.SetUserPreferencesUseCase
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.SharedMenuUiState
import com.android.harmoniatpi.ui.screens.notificationScreen.model.NotificationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val sharedMenuUiState: SharedMenuUiState,
    private val setUserPreferencesUseCase: SetUserPreferencesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sharedMenuUiState.uiState.collect { menuState ->
                _uiState.update {
                    it.copy(
                        notificationsList = menuState.notificationsList,
                        newNotifications = menuState.notificationsList.any { it.isNew },
                        internetAvailable = menuState.internetAvailable
                    )
                }
            }
        }
    }

    fun markNotificationsAsRead() {
        val updatedList = _uiState.value.notificationsList.map { it.copy(isNew = false) }

        // Actualiza el NotificationState local
        _uiState.update {
            it.copy(
                notificationsList = updatedList,
                newNotifications = false
            )
        }

        // Actualiza el estado global compartido
        sharedMenuUiState.updateState {
            it.copy(
                notificationsList = updatedList,
                newNotification = false
            )
        }
    }

    fun deleteNotification(notificationId: Int) {
        val updatedList = _uiState.value.notificationsList.filter { it.id != notificationId }

        // Actualiza el NotificationState local
        _uiState.update {
            it.copy(
                notificationsList = updatedList
            )
        }

        // Actualiza el estado global compartido
        sharedMenuUiState.updateState {
            it.copy(
                notificationsList = updatedList
            )
        }

        val preferences = UserPreferences(
            userName = sharedMenuUiState.uiState.value.userName,
            userEmail = sharedMenuUiState.uiState.value.userEmail,
            userID = sharedMenuUiState.uiState.value.userID,
            userPhotoPath = sharedMenuUiState.uiState.value.userPhotoPath,
            userLastName = sharedMenuUiState.uiState.value.userLastName,
            appTheme = sharedMenuUiState.uiState.value.appTheme,
            notificationList = sharedMenuUiState.uiState.value.notificationsList,
            newNotification = sharedMenuUiState.uiState.value.newNotification
        )

        viewModelScope.launch {
            setUserPreferencesUseCase(preferences)
        }
    }
}