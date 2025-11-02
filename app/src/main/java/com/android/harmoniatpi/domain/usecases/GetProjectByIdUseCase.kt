package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.project.Project
import javax.inject.Inject

class GetProjectByIdUseCase @Inject constructor(private val repository: Repository) {
    suspend operator fun invoke(projectId: String): Project {
        return repository.getProjectById(projectId)
    }
}