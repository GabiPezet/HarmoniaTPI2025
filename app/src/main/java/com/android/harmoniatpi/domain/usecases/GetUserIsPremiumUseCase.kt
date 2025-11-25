package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetUserIsPremiumUseCase @Inject constructor(
    private val repository: Repository
) {
    /**
     * Devuelve un Flow que emite el estado Premium del usuario en tiempo real.
     */
    operator fun invoke(): Flow<Boolean> {
        // Usa la función de observación en tiempo real del repositorio (Repository.kt)
        return repository.observeCurrentUserFromFirestore().map { userPreferences ->
            // Mapea el UserPreferences? a Boolean (false si es nulo o no premium)
            userPreferences?.isPremium ?: false
        }
    }

    // Mantener la suspend fun para lecturas únicas, si es necesaria.
    suspend fun oneTimeRead(): Boolean {
        return repository.getUserPreferences()?.isPremium ?: false
    }
}