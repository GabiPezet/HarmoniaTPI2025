package com.android.harmoniatpi.domain.usecases.paymentUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import javax.inject.Inject

class CreatePaymentPreferenceUseCase @Inject constructor(
    private val repository: Repository
) {
    suspend operator fun invoke(amount: Double, description: String) =
        repository.createPaymentPreference(amount, description)
}
