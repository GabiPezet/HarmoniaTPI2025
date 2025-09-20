package com.android.harmoniatpi.ui.screens.registerScreen.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.usecases.RegisterUserFirebaseUseCase
import com.android.harmoniatpi.ui.screens.registerScreen.model.RegisterUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterScreenViewModel @Inject constructor(
    private val registerUserFirebaseUseCase: RegisterUserFirebaseUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
        validateForm()
    }

    fun onLastNameChange(lastName: String) {
        _uiState.value = _uiState.value.copy(lastName = lastName)
        validateForm()
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
        validateForm()
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
        validateForm()
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword)
        validateForm()
    }

    fun registerUser(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!_uiState.value.isFormValid) return

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            val result = registerUserFirebaseUseCase(
                email = _uiState.value.email,
                password = _uiState.value.password,
                name = _uiState.value.name,
                lastName = _uiState.value.lastName
            )

            _uiState.value = _uiState.value.copy(isLoading = false)

            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { ex -> onError(ex.message ?: "Error desconocido") }
            )
        }
    }

    private fun validateForm() {
        val currentState = _uiState.value
        val isNameValid = currentState.name.isNotBlank()
        val isLastNameValid = currentState.lastName.isNotBlank()
        val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()
        val isPasswordValid = currentState.password.length >= 6
        val doPasswordsMatch =
            currentState.password == currentState.confirmPassword && currentState.confirmPassword.isNotBlank()
        val isFormValid =
            isNameValid && isLastNameValid && isEmailValid && isPasswordValid && doPasswordsMatch

        _uiState.value = currentState.copy(
            isNameValid = isNameValid,
            isLastNameValid = isLastNameValid,
            isEmailValid = isEmailValid,
            isPasswordValid = isPasswordValid,
            doPasswordsMatch = doPasswordsMatch,
            isFormValid = isFormValid
        )
    }
}

