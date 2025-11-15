package com.android.harmoniatpi.ui.screens.paymentMarketScreen.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.model.payment.PaymentResult
import com.android.harmoniatpi.domain.usecases.paymentUseCases.CreatePaymentPreferenceUseCase
import com.android.harmoniatpi.domain.usecases.paymentUseCases.SendPaymentUseCase
import com.android.harmoniatpi.ui.screens.paymentMarketScreen.model.PaymentUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentMarketViewModel @Inject constructor(
    private val createPreferenceUseCase: CreatePaymentPreferenceUseCase,
    private val sendPaymentUseCase: SendPaymentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState

    fun createPreference(amount: Double, description: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }

            try {
                val pref = createPreferenceUseCase(amount, description)
                _uiState.update { it.copy(loading = false, preference = pref) }
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, errorMessage = e.message) }
            }
        }
    }

    fun onPaymentApproved() {
        val current = _uiState.value
        _uiState.value = current.copy(
            paymentResult = PaymentResult.APPROVED,
            errorMessage = null
        )
    }

    fun sendPayment() {
        val pref = _uiState.value.preference ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }

            try {
                val result = sendPaymentUseCase(pref.preferenceId)
                _uiState.update { it.copy(loading = false, paymentResult = result) }
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, errorMessage = e.message) }
            }
        }
    }
}
