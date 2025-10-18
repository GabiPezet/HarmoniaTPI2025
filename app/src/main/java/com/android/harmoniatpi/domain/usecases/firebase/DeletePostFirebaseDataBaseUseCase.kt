package com.android.harmoniatpi.domain.usecases.firebase

import com.android.harmoniatpi.domain.interfaces.Repository
import javax.inject.Inject

class DeletePostFirebaseDataBaseUseCase @Inject constructor(private val repository: Repository) {
    suspend operator fun invoke(id: String) = repository.deletePostByIdRealTimeDB(id)
}