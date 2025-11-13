package com.android.harmoniatpi.ui.screens.paymentMarketScreen.model

import com.android.harmoniatpi.domain.model.payment.PaymentPreference
import com.android.harmoniatpi.domain.model.payment.PaymentResult

data class PaymentUiState(
    val loading: Boolean = false,
    val preference: PaymentPreference? = null,
    val paymentResult: PaymentResult? = null,
    val errorMessage: String? = null
)

