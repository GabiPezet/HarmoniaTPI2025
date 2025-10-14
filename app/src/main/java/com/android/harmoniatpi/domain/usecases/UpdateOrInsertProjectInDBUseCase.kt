package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.project.Project
import javax.inject.Inject

class UpdateOrInsertProjectInDBUseCase @Inject constructor(private val repository: Repository) {
    suspend operator fun invoke(project: Project) = repository.insertOrUpdateProject(project)
}