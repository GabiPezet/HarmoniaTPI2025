package com.android.harmoniatpi.data.remote

import com.android.harmoniatpi.domain.model.payment.PaymentPreference
import com.android.harmoniatpi.domain.model.payment.PaymentResult
import kotlinx.coroutines.delay
import javax.inject.Inject

class MockMercadoPagoApi @Inject constructor() {

    suspend fun createPreference(amount: Double, description: String): PaymentPreference {
        delay(1500)
        return PaymentPreference(
            preferenceId = "pref_${System.currentTimeMillis()}",
            amount = amount,
            description = description
        )
    }

    suspend fun processPayment(preferenceId: String): PaymentResult {
        delay(2000)

        return listOf(
            PaymentResult.APPROVED,
            PaymentResult.REJECTED,
            PaymentResult.PENDING
        ).random()
    }
}
