package com.android.harmoniatpi.domain.usecases.roomUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import javax.inject.Inject

class GetAllProjectsFromDBUseCase @Inject constructor(private val repository: Repository) {
     operator fun invoke() = repository.getAllProjects()
}