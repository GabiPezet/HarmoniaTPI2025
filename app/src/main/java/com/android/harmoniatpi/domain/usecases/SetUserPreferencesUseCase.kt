package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.UserPreferences
import javax.inject.Inject

class SetUserPreferencesUseCase @Inject constructor(private val repository: Repository) {
    suspend operator fun invoke(userPreferences: UserPreferences) =
        repository.updateUserPreferences(userPreferences)
}