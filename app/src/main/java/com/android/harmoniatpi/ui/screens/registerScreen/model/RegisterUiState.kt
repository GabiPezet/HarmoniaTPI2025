package com.android.harmoniatpi.ui.screens.registerScreen.model

data class RegisterUiState(
    val name: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isNameValid: Boolean = false,
    val isLastNameValid: Boolean = false,
    val isEmailValid: Boolean = false,
    val isPasswordValid: Boolean = false,
    val doPasswordsMatch: Boolean = false,
    val isFormValid: Boolean = false
)
