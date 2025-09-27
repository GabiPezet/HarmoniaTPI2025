package com.android.harmoniatpi.data

import com.android.harmoniatpi.data.database.dao.UserPreferencesDao
import com.android.harmoniatpi.data.database.entities.UserPreferencesEntity
import com.android.harmoniatpi.di.util.JsonUtils
import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userPreferencesDao: UserPreferencesDao,
    private val jsonUtils: JsonUtils,
) : Repository {
    override fun getFirebaseCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override suspend fun updateUserPreferences(userPreferences: UserPreferences) {
        userPreferencesDao.updateUserPreferences(userPreferences.toDataBase(jsonUtils))
    }

    override suspend fun getUserPreferences(): UserPreferences? {
        val user = firebaseAuth.currentUser ?: return null
        val entity = userPreferencesDao.getUserPreferences(user.uid) ?: return null
        return entity.toDomain(jsonUtils)
    }

    override suspend fun logOutUser() {
        withContext(Dispatchers.IO) {
            firebaseAuth.signOut()
        }
    }

    override suspend fun logInUser(email: String, password: String): Result<FirebaseUser> =
        withContext(Dispatchers.IO) {
            try {
                val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val user =
                    authResult.user ?: return@withContext Result.failure(Exception("User is null"))

                ensureUserPreferencesExist(user.uid, email)

                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun registerUserFirebase(
        email: String,
        password: String,
        name: String,
        lastName: String
    ): Result<FirebaseUser> =
        withContext(Dispatchers.IO) {
            try {
                val authResult =
                    firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val user =
                    authResult.user ?: return@withContext Result.failure(Exception("User is null"))
                val existingPrefs = userPreferencesDao.getUserPreferences(user.uid)
                if (existingPrefs == null) {
                    userPreferencesDao.insertUserPreferences(
                        UserPreferencesEntity(
                            userID = user.uid,
                            userEmail = email,
                            userName = name,
                            userLastName = lastName
                        )
                    )
                }
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return withContext(Dispatchers.IO) {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                val user = authResult.user ?: return@withContext Result.failure(Exception("User is null"))

                ensureUserPreferencesExist(
                    userId = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName
                )

                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }


    private suspend fun ensureUserPreferencesExist(
        userId: String,
        email: String,
        displayName: String? = null
    ) {
        val existingPrefs = userPreferencesDao.getUserPreferences(userId)
        if (existingPrefs == null) {
            userPreferencesDao.insertUserPreferences(
                UserPreferencesEntity(
                    userID = userId,
                    userEmail = email,
                    userName = displayName ?: "",
                    userLastName = "",
                )
            )
        } else {
            val updatedPrefs = existingPrefs.copy(
                userEmail = email.ifBlank { existingPrefs.userEmail },
                userName = displayName ?: existingPrefs.userName
            )
            userPreferencesDao.updateUserPreferences(updatedPrefs)
        }
    }
}