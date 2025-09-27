package com.android.harmoniatpi.ui.screens.loginScreen.model

data class LoginUiState(
    val isInternetAvailable: Boolean = false,
    val showLoginScreen: Boolean = false,
    val versionName: String = "",
    val loginSuccess: Boolean = false,
    val errorMessage: String? = null,
    val isLoading : Boolean = false,
    val helpDeskContact: Boolean = false,
    val isInitialized: Boolean = false,
    val previewScreen : Boolean = true
)