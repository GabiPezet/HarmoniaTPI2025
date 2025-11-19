package com.android.harmoniatpi.domain.usecases.paymentUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.UserPreferences
import javax.inject.Inject

class TogglePremiumStatusUseCase @Inject constructor(
    private val repository: Repository
) {
    /**
     * Alterna el estado Premium del usuario actual para propósitos de prueba.
     * Esto simula un pago exitoso o fallido basado en el estado actual.
     */
    suspend operator fun invoke(isCurrentlyPremium: Boolean): Result<UserPreferences> {
        return if (isCurrentlyPremium) {
            // Si es Premium, simulamos un 'fallo' al llamar a la función con una palabra clave que no es 'aprobado'
            repository.updatePremiumStatus("pago_cancelado")
        } else {
            // Si es Free, simulamos un 'éxito' al llamar a la función con la palabra clave correcta
            repository.updatePremiumStatus("aprobado")
        }
    }
}