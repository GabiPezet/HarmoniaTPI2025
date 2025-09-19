package com.android.harmoniatpi.domain.interfaces

import com.google.firebase.auth.FirebaseUser

interface Repository {
    fun getFirebaseCurrentUser(): FirebaseUser?
}