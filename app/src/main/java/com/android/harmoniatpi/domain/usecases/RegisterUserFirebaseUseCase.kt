package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.Repository
import javax.inject.Inject

class RegisterUserFirebaseUseCase @Inject constructor(private val repository: Repository) {

    suspend operator fun invoke(email: String, password: String, name: String, lastName: String) =
        repository.registerUserFirebase(email, password, name, lastName)
}