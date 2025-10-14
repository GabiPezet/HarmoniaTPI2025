package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.Repository
import javax.inject.Inject

class GetAllProjectsFromDBUseCase @Inject constructor(private val repository: Repository) {
    suspend operator fun invoke() = repository.getAllProjects()
}