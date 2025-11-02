package com.android.harmoniatpi.domain.usecases.firebaseUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import jakarta.inject.Inject

class DeleteFileFromStorageUseCase @Inject constructor(
    private val repository: Repository
) {
    suspend operator fun invoke(remotePath: String) = repository.deleteFileFromStorage(remotePath)
}