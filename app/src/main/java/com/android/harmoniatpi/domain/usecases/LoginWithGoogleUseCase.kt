package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.Repository
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class LoginWithGoogleUseCase @Inject constructor(
    private val repository: Repository
) {
    suspend operator fun invoke(idToken: String): Result<FirebaseUser> {
        return repository.signInWithGoogle(idToken)
    }
}