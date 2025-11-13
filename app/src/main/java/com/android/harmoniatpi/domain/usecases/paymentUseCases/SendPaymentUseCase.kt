package com.android.harmoniatpi.domain.usecases.paymentUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import javax.inject.Inject

class SendPaymentUseCase @Inject constructor(
    private val repository: Repository
) {
    suspend operator fun invoke(preferenceId: String) =
        repository.sendPayment(preferenceId)
}
