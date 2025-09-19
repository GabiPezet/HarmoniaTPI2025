package com.android.harmoniatpi.data

import com.android.harmoniatpi.domain.interfaces.Repository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class RepositoryImpl @Inject constructor(private val firebaseAuth: FirebaseAuth) : Repository {
    override fun getFirebaseCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
}