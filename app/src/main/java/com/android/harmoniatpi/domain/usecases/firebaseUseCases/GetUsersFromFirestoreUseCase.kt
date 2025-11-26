package com.android.harmoniatpi.domain.usecases.firebaseUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.UserPreferences
import javax.inject.Inject

class GetUsersFromFirestoreUseCase @Inject constructor(
    private val repository: Repository
) {
    /**
     * Obtiene una lista de usuarios directamente de Firestore (datos frescos).
     */
    suspend operator fun invoke(userIds: List<String>): Result<List<UserPreferences>> {
        return repository.getUsersFromFirestore(userIds)
    }
}