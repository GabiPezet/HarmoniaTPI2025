package com.android.harmoniatpi.domain.usecases.firebaseUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import javax.inject.Inject

class FetchAndSyncUsersUseCase @Inject constructor(
    private val repository: Repository
) {
    /**
     * Busca una lista de usuarios por sus IDs en Firestore y
     * los actualiza/inserta en la base de datos local (Room).
     */
    suspend operator fun invoke(userIds: List<String>): Result<Unit> {
        if (userIds.isEmpty()) {
            return Result.success(Unit)
        }
        // Llama a la nueva función del repositorio
        return repository.fetchAndSyncUsersFromFirestore(userIds)
    }
}