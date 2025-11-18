package com.android.harmoniatpi.domain.usecases.paymentUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import javax.inject.Inject

class CancelSubscriptionUseCase @Inject constructor(
    private val repository: Repository
) {
    suspend operator fun invoke(subscriptionId: String): Result<Unit> {
        return repository.cancelSubscription(subscriptionId)
    }
}