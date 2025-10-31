package com.android.harmoniatpi.domain.usecases.firebaseUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import javax.inject.Inject

class GetAllUserFromDBUseCase @Inject constructor(private val repository: Repository) {
    operator fun invoke() = repository.getAllUser()
}