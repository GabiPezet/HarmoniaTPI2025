package com.android.harmoniatpi.domain.usecases.paymentUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.UserPreferences
import javax.inject.Inject

class TogglePremiumStatusUseCase @Inject constructor(
    private val repository: Repository
) {
    suspend operator fun invoke(isCurrentlyPremium: Boolean): Result<UserPreferences> {
        return if (isCurrentlyPremium) {
            repository.updatePremiumStatus("pago_cancelado", null) // Pasamos null
        } else {
            repository.updatePremiumStatus("approved", null) // Pasamos null
        }
    }
}