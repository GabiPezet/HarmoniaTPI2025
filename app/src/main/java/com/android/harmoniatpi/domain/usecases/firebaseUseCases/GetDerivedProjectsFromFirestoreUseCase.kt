package com.android.harmoniatpi.domain.usecases.firebaseUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.project.Project
import javax.inject.Inject

class GetDerivedProjectsFromFirestoreUseCase @Inject constructor(
    private val repository: Repository
) {
    /**
     * Llama al repositorio para obtener una lista de proyectos desde Firestore
     * que estén publicados y apunten a un ID de proyecto original.
     */
    suspend operator fun invoke(originalProjectId: String): List<Project> {
        return repository.getDerivedProjectsFromFirestore(originalProjectId)
    }
}