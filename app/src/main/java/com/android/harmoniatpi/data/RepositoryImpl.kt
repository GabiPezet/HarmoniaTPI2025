package com.android.harmoniatpi.data

import android.net.Uri
import android.util.Log
import com.android.harmoniatpi.data.database.dao.MyPostDao
import com.android.harmoniatpi.data.database.dao.ProjectDao
import com.android.harmoniatpi.data.database.dao.UserPreferencesDao
import com.android.harmoniatpi.data.database.entities.MyPostEntity
import com.android.harmoniatpi.data.database.entities.UserPreferencesEntity
import com.android.harmoniatpi.data.local.model.PostFirebaseModel
import com.android.harmoniatpi.data.local.model.UserFirebaseModel
import com.android.harmoniatpi.di.util.JsonUtils
import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.domain.model.userPreferences.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userPreferencesDao: UserPreferencesDao,
    private val jsonUtils: JsonUtils,
    private val firestore: FirebaseFirestore,
    private val projectDao: ProjectDao,
    private val myPostDao: MyPostDao,
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage
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
                val user =
                    authResult.user ?: return@withContext Result.failure(Exception("User is null"))

                syncFireStoreToLocal(user.uid)

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
                val user =
                    authResult.user ?: return@withContext Result.failure(Exception("User is null"))

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

    override fun getAllPostsFlowRealTimeDB(): Flow<List<Post>> = callbackFlow {
        val user = firebaseAuth.currentUser
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = snapshot.children.mapNotNull { child ->
                    val firebasePost = child.getValue(PostFirebaseModel::class.java)
                    firebasePost?.toDomain(jsonUtils)
                }

                trySend(posts)

                // Sincronizar Room en segundo plano
                launch(Dispatchers.IO) {
                    try {
                        Log.i("KlyxDevs", "Sincronizando posts...")
                        Log.i("KlyxDevs", "userID: ${user!!.uid}")
                        val localPosts = getMyPosts().first()
                        val myRemotePosts = posts.filter { it.userID == user.uid }
                        myRemotePosts.forEach { remotePost ->
                            val localPost = localPosts.find { it.id == remotePost.id }

                            if (localPost == null) {
                                Log.i("KlyxDevs", "Insertando nuevo post: ${remotePost.id}")
                                insertMyPost(remotePost.toDataBase(jsonUtils))
                            } else {
                                val hasNewLike = remotePost.likes != localPost.likes
                                val hasNewComment =
                                    remotePost.comments.size != localPost.comments.size
                                if (hasNewLike || hasNewComment) {
                                    Log.i("KlyxDevs", "Actualizando post ${remotePost.id}")
                                    val updatedEntity = remotePost.toDataBase(
                                        jsonUtils = jsonUtils,
                                        hasNewComment = hasNewComment,
                                        hasNewLike = hasNewLike
                                    )
                                    updateMyPost(updatedEntity)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("KlyxDevs", "Error sincronizando posts locales", e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        val postsRef = database.reference.child("posts")
        postsRef.addValueEventListener(listener)
        awaitClose { postsRef.removeEventListener(listener) }
    }

    override suspend fun insertPostRealTimeDB(post: Post) {
        val postId =
            if (post.id.isNotEmpty()) post.id else database.reference.child("posts").push().key!!
        val postModel = post.toPostFirebaseModel(jsonUtils)
        database.reference.child("posts").child(postId).setValue(postModel)
    }

    override suspend fun updatePostRealTimeDB(post: Post) {
        val postModel = post.toPostFirebaseModel(jsonUtils)
        database.reference.child("posts").child(post.id).setValue(postModel)
    }

    override suspend fun deletePostByIdRealTimeDB(id: String) {
        database.reference.child("posts").child(id).removeValue().apply {
            myPostDao.deletePostById(id)
        }
    }

    override suspend fun uploadLocalFileToFirebaseStorage(
        localFilePath: String,
        remotePath: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val file = File(localFilePath)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("El archivo no existe en: $localFilePath"))
            }
            // Crea un File Template vacio para cargar la URI que quedará en el dispositivo local
            // hasta realizar el Upload a Storage
            val fileUri = Uri.fromFile(file)
            val ref = storage.reference.child(remotePath)

            // Realiza el Upload de la imagen del usuario al STORAGE
            ref.putFile(fileUri).await()

            // Devuelve la URL de la imagen del usuario cargada en el STORAGE
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMyPosts(): Flow<List<Post>> {
        return myPostDao.getMyPosts().map { list -> list.map { it.toDomain(jsonUtils) } }
    }

    override suspend fun insertMyPost(post: MyPostEntity) {
        myPostDao.insertPost(post)
    }

    override suspend fun updateMyPost(post: MyPostEntity) {
        myPostDao.updatePost(post)
    }

    override suspend fun deleteMyPostById(id: String) {
        myPostDao.deletePostById(id)
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
