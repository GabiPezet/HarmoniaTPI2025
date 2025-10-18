package com.android.harmoniatpi.domain.interfaces

import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.project.Project
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface Repository {
    fun getFirebaseCurrentUser(): FirebaseUser?

    suspend fun updateUserPreferences(userPreferences: UserPreferences)

    suspend fun getUserPreferences(): UserPreferences?

    suspend fun logOutUser()

    suspend fun logInUser(email: String, password: String): Result<FirebaseUser>

    suspend fun registerUserFirebase(
        email: String,
        password: String,
        name: String,
        lastName: String
    ): Result<FirebaseUser>

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser>

    fun getAllProjects ():Flow<List<Project>>

    suspend fun deleteProject(projectId: String)

    suspend fun insertOrUpdateProject(project: Project)

    suspend fun getProjectById(projectId: String): Project
}