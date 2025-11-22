package com.android.harmoniatpi.domain.model.payment

data class PaymentPreference(
    val preferenceId: String,
    val amount: Double,
    val description: String
)
