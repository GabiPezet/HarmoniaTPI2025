package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.Repository
import javax.inject.Inject

class GetProjectsByUserUseCase @Inject constructor(private val repository: Repository) {
    operator fun invoke(ownerId: String) = repository.getAllProjectsByUser(ownerId)
}
