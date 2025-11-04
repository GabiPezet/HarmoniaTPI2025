package com.android.harmoniatpi.domain.usecases.firebaseUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.UserPreferences
import javax.inject.Inject

class SendFriendRequestUseCase @Inject constructor(
    private val repository: Repository
) {
    /**
     * Envía una solicitud de amistad/seguimiento llamando al repositorio,
     * que maneja la transacción de Firestore y la actualización de Room.
     *
     * @return Result que contiene el 'UserPreferences' actualizado del usuario actual.
     */
    suspend operator fun invoke(
        currentUser: UserPreferences,
        targetUser: UserPreferences
    ): Result<UserPreferences> {
        // Llama a la nueva función del repositorio
        return repository.sendFriendRequest(currentUser, targetUser)
    }
}