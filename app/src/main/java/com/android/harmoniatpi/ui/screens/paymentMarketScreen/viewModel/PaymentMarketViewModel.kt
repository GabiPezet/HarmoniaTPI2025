package com.android.harmoniatpi.ui.screens.paymentMarketScreen.viewModel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.model.payment.PaymentResult
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.ObserveCurrentUserUseCase
import com.android.harmoniatpi.domain.usecases.paymentUseCases.CancelSubscriptionUseCase
import com.android.harmoniatpi.domain.usecases.paymentUseCases.CreatePaymentPreferenceUseCase
import com.android.harmoniatpi.domain.usecases.paymentUseCases.SendPaymentUseCase
import com.android.harmoniatpi.domain.usecases.paymentUseCases.UpdatePremiumStatusUseCase
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
    private val sendPaymentUseCase: SendPaymentUseCase,
    private val cancelSubscriptionUseCase: CancelSubscriptionUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val updatePremiumStatusUseCase: UpdatePremiumStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState


    init {
        viewModelScope.launch {
            observeCurrentUserUseCase().collect { user ->
                _uiState.update {
                    it.copy(subscriptionId = user?.subscriptionId)
                }
            }
        }
    }


    fun handlePaymentResult(uri: Uri) {
        val status = uri.getQueryParameter("collection_status")

        // MercadoPago devuelve "approved", pero tu Repository espera "aprobado"
        if (status == "approved") {
            viewModelScope.launch {
                _uiState.update { it.copy(loading = true) }
                try {
                    // Enviamos "aprobado" para que coincida con la lógica de tu RepositoryImpl
                    val result = updatePremiumStatusUseCase(status)

                    result.onSuccess {
                        _uiState.update { state ->
                            state.copy(loading = false, paymentResult = PaymentResult.APPROVED)
                        }
                    }.onFailure { e ->
                        _uiState.update { state ->
                            state.copy(loading = false, errorMessage = "Error actualizando premium: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(loading = false, errorMessage = e.message) }
                }
            }
        } else if (status == "rejected" || status == "null") {
            // Manejar rechazo si es necesario
        }
    }

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

    fun openSubscriptionLink(context: Context) {
        val url = _uiState.value.preference?.preferenceId
        if (!url.isNullOrBlank()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
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

    fun cancelMySubscription(subscriptionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }

            val result = cancelSubscriptionUseCase(subscriptionId)

            result.onSuccess {
                _uiState.update {
                    it.copy(
                        loading = false,
                        subscriptionId = null, // Forzamos la vista de "No Suscrito"
                        paymentResult = null
                    )
                }

            }.onFailure { error ->
                _uiState.update { it.copy(loading = false, errorMessage = "Error al cancelar: ${error.message}") }

            }
        }
    }
}
