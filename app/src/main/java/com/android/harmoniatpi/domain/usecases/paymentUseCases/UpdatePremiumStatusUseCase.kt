package com.android.harmoniatpi.domain.usecases.paymentUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.UserPreferences
import javax.inject.Inject

class UpdatePremiumStatusUseCase @Inject constructor(
    private val repository: Repository
) {
    suspend operator fun invoke(status: String): Result<UserPreferences> {
        return repository.updatePremiumStatus(status)
    }
}