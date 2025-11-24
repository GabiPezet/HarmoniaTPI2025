package com.android.harmoniatpi.domain.usecases.firebaseUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import javax.inject.Inject

class DeletePostByProjectIdUseCase @Inject constructor(
    private val repository: Repository
) {
    suspend operator fun invoke(projectId: String): Result<Unit> {
        return repository.deletePostByProjectId(projectId)
    }
}