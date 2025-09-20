package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.UserPreferences
import javax.inject.Inject

class GetUserPreferencesUseCase @Inject constructor(
    private val repository: Repository
) {
    suspend operator fun invoke(): UserPreferences? {
        return repository.getUserPreferences()
    }
}