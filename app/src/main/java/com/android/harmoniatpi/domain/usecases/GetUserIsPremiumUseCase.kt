package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.Repository
import javax.inject.Inject

class GetUserIsPremiumUseCase @Inject constructor(
    private val repository: Repository
) {
    /**
     * Devuelve el estado Premium del usuario actual.
     * Si no hay usuario o el estado no está disponible, se asume 'false' (Free).
     */
    suspend operator fun invoke(): Boolean {
        // La función getUserPreferences() en Repository ya maneja la lógica de obtener
        // los datos del usuario (sincronizando de Firestore a Room si es necesario).
        return repository.getUserPreferences()?.isPremium ?: false
    }
}