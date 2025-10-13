package com.android.harmoniatpi.ui.screens.menuPrincipal.content.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.userPreferences.AppTheme
import com.android.harmoniatpi.domain.usecases.GetUserPreferencesUseCase
import com.android.harmoniatpi.domain.usecases.LogOutFirebaseUseCase
import com.android.harmoniatpi.domain.usecases.SetUserPreferencesUseCase
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.OptionsMenu
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.ProfileImageUser
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.SharedMenuUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrawerContentViewModel @Inject constructor(
    private val logOutUseCase: LogOutFirebaseUseCase,
    private val sharedMenuUiState: SharedMenuUiState,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val setUserPreferencesUseCase: SetUserPreferencesUseCase,
) : ViewModel() {
    val uiState = sharedMenuUiState.uiState
    private val _userPhotoPath = MutableStateFlow(ProfileImageUser())
    val userPhotoPath = _userPhotoPath.asStateFlow()
    fun initUserPreferences() {
        viewModelScope.launch {
            val currentUser: UserPreferences? = getUserPreferencesUseCase()
            if (currentUser != null) {
                _userPhotoPath.update {
                    it.copy(
                        path = currentUser.userPhotoPath,
                        version = it.version + 1
                    )
                }
                sharedMenuUiState.updateState {
                    it.copy(
                        userEmail = currentUser.userEmail,
                        userName = currentUser.userName,
                        userLastName = currentUser.userLastName,
                        userPhotoPath = currentUser.userPhotoPath,
                        userPhotoPathRemote = currentUser.userPhotoPathRemote,
                        userID = currentUser.userID,
                        appTheme = currentUser.appTheme,
                        notificationsList = currentUser.notificationList,
                        newNotification = currentUser.newNotification,
                        instrument = currentUser.instrument,
                        genres = currentUser.genres,
                        location = currentUser.location,
                        rating = currentUser.rating,
                        friendsList = currentUser.friendsList,
                        projectsList = currentUser.projectsList,
                        myPostsList = currentUser.myPostsList,
                        friendRequestReceived = currentUser.friendRequestReceived,
                        friendRequestSent = currentUser.friendRequestSent
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


    fun toggleTheme(theme: AppTheme) {
        sharedMenuUiState.updateState {
            it.copy(
                appTheme = theme
            )
        }
    }


    fun updateUserPreferences() {

        val preferences = UserPreferences(
            userID = uiState.value.userID,
            userEmail = uiState.value.userEmail,
            userPhotoPath = uiState.value.userPhotoPath,
            userPhotoPathRemote = uiState.value.userPhotoPathRemote,
            userName = uiState.value.userName,
            userLastName = uiState.value.userLastName,
            appTheme = uiState.value.appTheme,
            notificationList = uiState.value.notificationsList,
            newNotification = uiState.value.newNotification,
            instrument = uiState.value.instrument,
            genres = uiState.value.genres,
            location = uiState.value.location,
            rating = uiState.value.rating,
            friendsList = uiState.value.friendsList,
            projectsList = uiState.value.projectsList,
            myPostsList = uiState.value.myPostsList,
            friendRequestReceived = uiState.value.friendRequestReceived,
            friendRequestSent = uiState.value.friendRequestSent
        )
        viewModelScope.launch(Dispatchers.IO) {
            setUserPreferencesUseCase(preferences)
        }
    }

    fun updateUserName(newName: String) {
        sharedMenuUiState.updateState { it.copy(userName = newName) }
    }

    fun updateWorkProfile(instrument: String, genres: String, location: String) {
        sharedMenuUiState.updateState {
            it.copy(
                instrument = instrument,
                genres = genres,
                location = location
            )
        }
    }

    fun updateRating(newRating: Float) {
        // Aseguramos que el valor siempre esté entre 0 y 5
        val clampedRating = newRating.coerceIn(0f, 5f)
        sharedMenuUiState.updateState {
            it.copy(rating = clampedRating)
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

    fun saveUserPhoto(path: String) {
        _userPhotoPath.update {
            it.copy(
                path = path,
                version = it.version + 1
            )
        }
        sharedMenuUiState.updateState {
            it.copy(userPhotoPath = path)
        }
    }

    fun sendNotification() {
        viewModelScope.launch {
            sharedMenuUiState.updateState { it.copy(showNewNotification = true) }
        }
    }

}