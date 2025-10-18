package com.android.harmoniatpi.domain.usecases.firebase

import com.android.harmoniatpi.domain.interfaces.Repository
import javax.inject.Inject

class GetFirebaseCurrentUserUseCase @Inject constructor(
    private val repository: Repository
) {
    operator fun invoke() = repository.getFirebaseCurrentUser()
}