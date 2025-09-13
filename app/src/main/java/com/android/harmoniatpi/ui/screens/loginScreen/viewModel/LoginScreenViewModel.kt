package com.android.harmoniatpi.ui.screens.loginScreen.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.usecases.CheckIsInternetAvailableUseCase
import com.android.harmoniatpi.ui.screens.loginScreen.model.LoginUiState
import com.android.harmoniatpi.ui.screens.loginScreen.util.checkUserNick
import com.android.harmoniatpi.ui.screens.loginScreen.util.checkValidPassword
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    private val isInternetAvailable: CheckIsInternetAvailableUseCase,
    @ApplicationContext private val context: Context
) :
    ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val appVersion: String by lazy {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: "N/A"
    }

    init {
        checkInternetAvailable()
        tryAutoLogin()
    }

    fun checkInternetAvailable() {
        viewModelScope.launch {
            val isInternetAvailable = isInternetAvailable()
            Log.i("KlyxDevs", "Internet Activado: $isInternetAvailable")
            _uiState.update {
                it.copy(
                    isInternetAvailable = isInternetAvailable,
                    showLoginScreen = isInternetAvailable,
                    versionName = appVersion
                )
            }
        }
    }

    fun tryAutoLogin() {
        viewModelScope.launch {
            val result = _uiState.value.autoLogin
            _uiState.update {
                when (result) {
                    true -> {
                        it.copy(
                            loginSuccess = true,
                            isLoading = false,
                            isInitialized = true
                        )
                    }

                    false -> {
                        it.copy(
                            isLoading = false,
                            showLoginScreen = true,
                            isInitialized = true
                        )
                    }
                }
            }
        }
    }

    fun onLogin(username: String, password: String) {
        val isValid = checkUserNick(username) && checkValidPassword(password)
        if (username.isNotBlank() && password.isNotBlank() && isValid) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                delay(4000)
                _uiState.update { it.copy(loginSuccess = true, isLoading = false) }

            }
        }

    }


    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null, helpDeskContact = true) }
    }

    fun onLoginNavigated() {
        _uiState.update { it.copy(loginSuccess = false, helpDeskContact = false) }
    }
}