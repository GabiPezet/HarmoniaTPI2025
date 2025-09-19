package com.android.harmoniatpi.domain.usecases

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseLoginUseCase @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    suspend operator fun invoke(email: String, password: String): Result<FirebaseUser> = try {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        Result.success(result.user ?: throw Exception("User is null"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}