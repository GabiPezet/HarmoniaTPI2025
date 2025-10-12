package com.android.harmoniatpi.ui.screens.menuPrincipal.content.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.userPreferences.AppTheme
import com.android.harmoniatpi.domain.usecases.GetUserPreferencesUseCase
import com.android.harmoniatpi.domain.usecases.LogOutFirebaseUseCase
import com.android.harmoniatpi.domain.usecases.SetUserPreferencesUseCase
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.OptionsMenu
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.SharedMenuUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrawerContentViewModel @Inject constructor(
    private val logOutUseCase: LogOutFirebaseUseCase,
    private val sharedMenuUiState: SharedMenuUiState,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val setUserPreferencesUseCase: SetUserPreferencesUseCase
) : ViewModel() {
    val uiState = sharedMenuUiState.uiState

    fun initUserPreferences() {
        viewModelScope.launch {
            val currentUser: UserPreferences? = getUserPreferencesUseCase()
            if (currentUser != null) {
                sharedMenuUiState.updateState {
                    it.copy(
                        userEmail = currentUser.userName,
                        userName = uiState.value.userName,
                        userLastName = uiState.value.userLastName,
                        userID = currentUser.userID,
                        appTheme = currentUser.appTheme,
                        notificationsList = currentUser.notificationList,
                        newNotification = currentUser.newNotification
                    )
                }
            }
        }
    }

    fun start() {
        initUserPreferences()
    }

    fun resetLogOutSuccess() {
        sharedMenuUiState.updateState { it.copy(logOutSuccess = false) }
    }


    fun toggleTheme() {
        sharedMenuUiState.updateState {
            it.copy(
                appTheme = when (it.appTheme) {
                    AppTheme.LIGHT -> AppTheme.DARK
                    AppTheme.DARK -> AppTheme.LIGHT
                }
            )
        }
    }


    fun updateUserPreferences() {

        val preferences = UserPreferences(
            userEmail = uiState.value.userEmail,
            userName = uiState.value.userName,
            userLastName = uiState.value.userLastName,
            userID = uiState.value.userID,
            appTheme = uiState.value.appTheme,
            notificationList = uiState.value.notificationsList,
            newNotification = uiState.value.newNotification
        )
        viewModelScope.launch(Dispatchers.IO) {
            setUserPreferencesUseCase(preferences)
        }
    }

    fun logOutUser() {
        viewModelScope.launch(Dispatchers.IO) {
            logOutUseCase()
            sharedMenuUiState.updateState {
                it.copy(
                    logOutSuccess = true
                )
            }
        }
    }

    fun changeOptionsMenu(option: OptionsMenu) {
        sharedMenuUiState.updateState { it.copy(optionsMenu = option) }
    }

}