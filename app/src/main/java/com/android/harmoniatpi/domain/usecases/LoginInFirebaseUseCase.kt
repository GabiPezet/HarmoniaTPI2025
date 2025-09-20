package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.Repository
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class LoginInFirebaseUseCase @Inject constructor(
    private val repository: Repository
) {
    suspend operator fun invoke(email: String, password: String): Result<FirebaseUser> =
        repository.logInUser(email = email, password = password)
}