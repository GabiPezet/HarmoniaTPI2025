package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.Repository
import javax.inject.Inject

class GetProjectByIdFromDBUseCase @Inject constructor(private val repository: Repository) {
    suspend operator fun invoke(projectId: String) = repository.getProjectById(projectId)
}