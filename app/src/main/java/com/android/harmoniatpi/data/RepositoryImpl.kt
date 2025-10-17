package com.android.harmoniatpi.data

import com.android.harmoniatpi.data.database.dao.ProjectDao
import com.android.harmoniatpi.data.database.dao.UserPreferencesDao
import com.android.harmoniatpi.data.database.entities.UserPreferencesEntity
import com.android.harmoniatpi.data.local.model.UserFirebaseModel
import com.android.harmoniatpi.di.util.JsonUtils
import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.project.Project
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userPreferencesDao: UserPreferencesDao,
    private val jsonUtils: JsonUtils,
    private val firestore: FirebaseFirestore,
    private val projectDao: ProjectDao
) : Repository {

    override fun getFirebaseCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    override suspend fun updateUserPreferences(userPreferences: UserPreferences) {
        val entity = userPreferences.toDataBase(jsonUtils)
        userPreferencesDao.updateUserPreferences(entity)

        val userFirebaseModel = entity.toFirebaseModel()
        firestore.collection("users")
            .document(entity.userID)
            .set(userFirebaseModel)
            .await()
    }

    override suspend fun getUserPreferences(): UserPreferences? {
        val user = firebaseAuth.currentUser ?: return null
        syncFireStoreToLocal(user.uid)
        val entity = userPreferencesDao.getUserPreferences(user.uid) ?: return null
        return entity.toDomain(jsonUtils)
    }

    override suspend fun logOutUser() {
        withContext(Dispatchers.IO) { firebaseAuth.signOut() }
    }

    override suspend fun logInUser(email: String, password: String): Result<FirebaseUser> =
        withContext(Dispatchers.IO) {
            try {
                val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val user = authResult.user ?: return@withContext Result.failure(Exception("User is null"))

//                syncFireStoreToLocal(user.uid)

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
                val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user ?: return@withContext Result.failure(Exception("User is null"))

                // Crear local
                val userPrefs = UserPreferencesEntity(
                    userID = user.uid,
                    userEmail = email,
                    userName = name,
                    userLastName = lastName
                )
                userPreferencesDao.insertUserPreferences(userPrefs)

                // Crear remoto
                val userFirebaseModel = userPrefs.toFirebaseModel()
                firestore.collection("users").document(user.uid).set(userFirebaseModel).await()

                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> =
        withContext(Dispatchers.IO) {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                val user = authResult.user ?: return@withContext Result.failure(Exception("User is null"))

                // Si no existe documento en Firestore -> crear
                val userDoc = firestore.collection("users").document(user.uid).get().await()
                if (!userDoc.exists()) {
                    val userFirebaseModel = UserFirebaseModel(
                        userID = user.uid,
                        userEmail = user.email ?: "",
                        userName = user.displayName ?: "",
                        userLastName = ""
                    )
                    firestore.collection("users").document(user.uid).set(userFirebaseModel).await()
                }

                //sincronizar Firestore -> DB local
                syncFireStoreToLocal(user.uid)

                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override fun getAllProjects(): Flow<List<Project>> {
        return projectDao.getAllProjects().map { list -> list.map { it.toDomain(jsonUtils) } }
    }

    override fun getAllProjectsByUser(ownerId: String): Flow<List<Project>> {
        return projectDao.getAllProjectsByUser(ownerId).map { list -> list.map { it.toDomain(jsonUtils) } }
    }

    override suspend fun deleteProject(projectId: String) {
        projectDao.deleteById(projectId)
    }

    override suspend fun insertOrUpdateProject(project: Project) {
        projectDao.insertOrUpdate(project.toDataBase(jsonUtils))
    }

    override suspend fun getProjectById(projectId: String): Project {
        return projectDao.getProjectById(projectId)!!.toDomain(jsonUtils)
    }

    private suspend fun syncFireStoreToLocal(userId: String) {
        val snapshot = firestore.collection("users").document(userId).get().await()
        if (snapshot.exists()) {
            val remoteUser = snapshot.toObject(UserFirebaseModel::class.java)
            remoteUser?.let {
                val entity = it.toEntity()
                userPreferencesDao.insertUserPreferences(entity)
            }
        }
    }
}
