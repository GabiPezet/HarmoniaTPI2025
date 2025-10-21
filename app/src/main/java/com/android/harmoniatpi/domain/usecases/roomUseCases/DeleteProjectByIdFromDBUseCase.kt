package com.android.harmoniatpi.domain.usecases.roomUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import javax.inject.Inject

class DeleteProjectByIdFromDBUseCase @Inject constructor(private val repository: Repository) {
    suspend operator fun invoke(projectId: String) = repository.deleteProject(projectId)
}