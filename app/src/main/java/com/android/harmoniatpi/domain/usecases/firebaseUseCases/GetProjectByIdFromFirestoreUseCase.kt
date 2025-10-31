package com.android.harmoniatpi.domain.usecases.firebaseUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.project.Project
import javax.inject.Inject

class GetProjectByIdFromFirestoreUseCase @Inject constructor(private val repository: Repository) {
    /**
     * Llama al repositorio para obtener un solo proyecto por su ID desde Firestore.
     */
    suspend operator fun invoke(projectId: String): Project? {
        return repository.getProjectByIdFromFirestore(projectId)
    }
}