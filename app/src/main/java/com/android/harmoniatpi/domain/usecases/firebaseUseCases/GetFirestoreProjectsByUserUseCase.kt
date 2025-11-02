package com.android.harmoniatpi.domain.usecases.firebaseUseCases

import com.android.harmoniatpi.data.local.model.ProjectFirebaseModel
import com.android.harmoniatpi.domain.interfaces.Repository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetFirestoreProjectsByUserUseCase @Inject constructor(
    private val repository: Repository
) {
    suspend operator fun invoke(userId: String): Flow<List<ProjectFirebaseModel>> {
        return repository.getFirestoreProjectsByUser(userId)
    }
}