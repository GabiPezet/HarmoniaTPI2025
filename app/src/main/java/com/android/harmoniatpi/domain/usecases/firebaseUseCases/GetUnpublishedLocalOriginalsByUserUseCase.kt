package com.android.harmoniatpi.domain.usecases.firebaseUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.project.Project
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetUnpublishedLocalOriginalsByUserUseCase @Inject constructor(
    private val repository: Repository
) {
    /**
     * Obtiene un Flow con la lista de proyectos originales locales
     * que NO han sido publicados (`isPublished = false`) para un usuario específico.
     * Lee directamente de la base de datos local (Room).
     */
    suspend operator fun invoke(userId: String): Flow<List<Project>> {
        // Llama a la función correspondiente en el repositorio
        return repository.getUnpublishedLocalOriginalsByUser(userId)
    }
}