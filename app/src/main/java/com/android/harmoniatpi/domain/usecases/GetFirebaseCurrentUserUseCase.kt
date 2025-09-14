package com.android.harmoniatpi.domain.usecases

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class GetFirebaseCurrentUserUseCase @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    operator fun invoke() = firebaseAuth.currentUser
}