package com.android.harmoniatpi.data

import android.net.Uri
import android.util.Log
import com.android.harmoniatpi.data.database.dao.MyPostDao
import com.android.harmoniatpi.data.database.dao.ProjectDao
import com.android.harmoniatpi.data.database.dao.UserPreferencesDao
import com.android.harmoniatpi.data.database.entities.MyPostEntity
import com.android.harmoniatpi.data.database.entities.UserPreferencesEntity
import com.android.harmoniatpi.data.local.model.PostFirebaseModel
import com.android.harmoniatpi.data.local.model.ProjectFirebaseModel
import com.android.harmoniatpi.data.local.model.UserFirebaseModel
import com.android.harmoniatpi.data.remote.MercadoPagoApi
import com.android.harmoniatpi.data.remote.model.AutoRecurring
import com.android.harmoniatpi.data.remote.model.SubscriptionRequest
import com.android.harmoniatpi.data.remote.model.SubscriptionStatusUpdateRequest
import com.android.harmoniatpi.di.util.JsonUtils
import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.payment.PaymentPreference
import com.android.harmoniatpi.domain.model.payment.PaymentResult
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.domain.model.userPreferences.Friend
import com.android.harmoniatpi.domain.model.userPreferences.FriendRequestReceived
import com.android.harmoniatpi.domain.model.userPreferences.FriendRequestSending
import com.android.harmoniatpi.domain.model.userPreferences.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
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
    private val storage: FirebaseStorage,
    private val mercadoPagoApi: MercadoPagoApi
) : Repository {

    override suspend fun updatePremiumStatus(statusString: String, subscriptionId: String?): Result<UserPreferences> =
        withContext(Dispatchers.IO) {
            val successKeyword = "approved"

            if (statusString.equals(successKeyword, ignoreCase = true)) {
                try {
                    val currentUser = getUserPreferences()
                    if (currentUser == null) {
                        return@withContext Result.failure(Exception("Usuario no autenticado."))
                    }

                    // ✨ AQUÍ ESTÁ LA MAGIA: Guardamos el ID de suscripción
                    // Si viene un ID nuevo, lo usamos. Si no (ej. botón de prueba), mantenemos el que tenía o null.
                    val updatedUser = currentUser.copy(
                        isPremium = true,
                        subscriptionId = subscriptionId ?: currentUser.subscriptionId
                    )

                    updateUserPreferences(updatedUser)

                    Log.i("RepositoryImpl", "Usuario ${currentUser.userID} actualizado a Premium. ID: ${updatedUser.subscriptionId}")
                    Result.success(updatedUser)

                } catch (e: Exception) {
                    Log.e("RepositoryImpl", "Error al actualizar estado Premium.", e)
                    Result.failure(e)
                }
            } else {
                Result.failure(Exception("Estado no aprobado."))
            }
        }

    override fun getFirebaseCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    override suspend fun getUserById(userId: String): UserPreferences? {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val userFirebaseModel = document.toObject(UserFirebaseModel::class.java)
                userFirebaseModel?.toEntity()!!.toDomain(jsonUtils)
            } else {
                null
            }
        } catch (e: Exception) {
            // Manejar el error según tu necesidad
            Log.e("FirestoreRepository", "Error getting user by ID: $userId", e)
            null
        }
    }

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
        lastName: String,
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

    override fun getAllUser(): Flow<List<UserPreferences>> {
        return userPreferencesDao.getAllUser().map { list -> list.map { it.toDomain(jsonUtils) } }
    }

    override suspend fun deleteProject(projectId: String) {
        projectDao.deleteById(projectId)
    }

    override suspend fun insertOrUpdateProject(project: Project) {
        projectDao.insertOrUpdate(project.toDataBase(jsonUtils))
    }

    override fun getAllPostsFlowRealTimeDB(): Flow<List<Post>> = callbackFlow {
        val user = firebaseAuth.currentUser
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = snapshot.children.mapNotNull { child ->
                    val firebasePost = child.getValue(PostFirebaseModel::class.java)
                    firebasePost?.toDomain(jsonUtils)
                }

                trySend(posts.reversed())

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
                                val hasNewClone = remotePost.totalShared != localPost.totalShared
                                if (hasNewLike || hasNewComment || hasNewClone) {
                                    Log.i("KlyxDevs", "Actualizando post ${remotePost.id}")
                                    val updatedEntity = remotePost.toDataBase(
                                        jsonUtils = jsonUtils,
                                        hasNewComment = hasNewComment,
                                        hasNewLike = hasNewLike,
                                        hasNewClone = hasNewClone
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
        val query: Query = postsRef.orderByChild("createdAt")
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
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
        remotePath: String,
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

    override suspend fun sendFriendRequest(
        currentUser: UserPreferences,
        targetUser: UserPreferences
    ): Result<UserPreferences> = withContext(Dispatchers.IO) {
        try {
            var updatedCurrentUser: UserPreferences? = null
            var updatedTargetUser: UserPreferences? = null

            // Usamos una transacción de Firestore para los 5 pasos
            firestore.runTransaction { transaction ->
                val requestId = System.currentTimeMillis().toInt()

                // Obtener 'targetUser' de Firestore
                val targetUserRef = firestore.collection("users").document(targetUser.userID)
                val targetSnapshot = transaction.get(targetUserRef)
                val targetFirebaseModel = targetSnapshot.toObject(UserFirebaseModel::class.java)
                    ?: throw IllegalStateException("Usuario objetivo no encontrado en Firestore.")

                // Convertir a modelo de dominio para modificar las listas
                val targetDomainUser = targetFirebaseModel.toEntity().toDomain(jsonUtils)

                //Añadir 'currentUser' a 'targetUser.friendRequestReceived'
                val newRequestReceived = FriendRequestReceived(
                    idRequest = requestId,
                    fromUserID = currentUser.userID,
                    status = false
                )

                // Añade la solicitud solo si no existe ya
                val updatedTargetRequests =
                    if (targetDomainUser.friendRequestReceived.any { it.fromUserID == currentUser.userID }) {
                        targetDomainUser.friendRequestReceived
                    } else {
                        targetDomainUser.friendRequestReceived + newRequestReceived
                    }

                updatedTargetUser =
                    targetDomainUser.copy(friendRequestReceived = updatedTargetRequests)

                // Obtener 'currentUser' de Firestore
                val currentUserRef = firestore.collection("users").document(currentUser.userID)
                val currentSnapshot = transaction.get(currentUserRef)
                val currentFirebaseModel = currentSnapshot.toObject(UserFirebaseModel::class.java)
                    ?: throw IllegalStateException("Usuario actual no encontrado en Firestore.")

                val currentDomainUser = currentFirebaseModel.toEntity().toDomain(jsonUtils)

                //Añadir 'targetUser' a 'currentUser.friendRequestSent'
                val newRequestSent = FriendRequestSending(
                    idRequest = requestId,
                    toUserID = targetUser.userID,
                    status = false
                )

                // Añade la solicitud solo si no existe ya
                val updatedCurrentRequests =
                    if (currentDomainUser.friendRequestSent.any { it.toUserID == targetUser.userID }) {
                        currentDomainUser.friendRequestSent
                    } else {
                        currentDomainUser.friendRequestSent + newRequestSent
                    }

                updatedCurrentUser =
                    currentDomainUser.copy(friendRequestSent = updatedCurrentRequests)

                //Subir 'targetUser' y 'currentUser' actualizados ---
                transaction.set(
                    targetUserRef,
                    updatedTargetUser.toDataBase(jsonUtils).toFirebaseModel()
                )
                transaction.set(
                    currentUserRef,
                    updatedCurrentUser.toDataBase(jsonUtils).toFirebaseModel()
                )

                // Requerido por la lambda de la transacción
                null
            }.await() // Espera a que la transacción termine

            //  Actualización Adicional: Sincronizar Room ---
            if (updatedTargetUser != null) {
                userPreferencesDao.insertUserPreferences(updatedTargetUser.toDataBase(jsonUtils))
            }
            if (updatedCurrentUser != null) {
                userPreferencesDao.insertUserPreferences(updatedCurrentUser.toDataBase(jsonUtils))
                Result.success(updatedCurrentUser) // Devuelve el usuario actual actualizado
            } else {
                Result.failure(Exception("La transacción falló y 'updatedCurrentUser' es nulo."))
            }

        } catch (e: Exception) {
            Log.e("RepositoryImpl", "Error en sendFriendRequest", e)
            Result.failure(e)
        }
    }

    override suspend fun acceptFriendRequest(
        currentUser: UserPreferences,
        request: FriendRequestReceived
    ): Result<UserPreferences> = withContext(Dispatchers.IO) {
        try {
            var updatedCurrentUser: UserPreferences? = null

            firestore.runTransaction { transaction ->
                val targetUserId = request.fromUserID

                //Obtener y actualizar Target User (el que envió la solicitud) ---
                val targetUserRef = firestore.collection("users").document(targetUserId)
                val targetSnapshot = transaction.get(targetUserRef)
                val targetFirebaseModel = targetSnapshot.toObject(UserFirebaseModel::class.java)
                    ?: throw IllegalStateException("Usuario objetivo no encontrado.")

                val targetDomainUser = targetFirebaseModel.toEntity().toDomain(jsonUtils)

                //Eliminar de 'friendRequestSent'
                val updatedTargetSent = targetDomainUser.friendRequestSent.filterNot {
                    it.toUserID == currentUser.userID
                }
                //Añadir a 'friendsList'
                val newFriendForTarget = Friend(
                    id = currentUser.userID,
                    name = currentUser.userName,
                    lastName = currentUser.userLastName,
                    urlPhoto = currentUser.userPhotoPathRemote
                )
                val updatedTargetFriends =
                    if (targetDomainUser.friendsList.any { it.id == currentUser.userID }) {
                        targetDomainUser.friendsList
                    } else {
                        targetDomainUser.friendsList + newFriendForTarget
                    }

                val finalTargetUser = targetDomainUser.copy(
                    friendRequestSent = updatedTargetSent,
                    friendsList = updatedTargetFriends
                )


                //Eliminar de 'friendRequestReceived'
                val updatedCurrentReceived = currentUser.friendRequestReceived.filterNot {
                    it.fromUserID == targetUserId
                }
                //Añadir a 'friendsList'
                val newFriendForCurrent = Friend(
                    id = targetDomainUser.userID,
                    name = targetDomainUser.userName,
                    lastName = targetDomainUser.userLastName,
                    urlPhoto = targetDomainUser.userPhotoPathRemote
                )
                val updatedCurrentFriends =
                    if (currentUser.friendsList.any { it.id == targetUserId }) {
                        currentUser.friendsList
                    } else {
                        currentUser.friendsList + newFriendForCurrent
                    }

                updatedCurrentUser = currentUser.copy(
                    friendRequestReceived = updatedCurrentReceived,
                    friendsList = updatedCurrentFriends
                )

                //Ejecutar la transacción ---
                transaction.set(
                    targetUserRef,
                    finalTargetUser.toDataBase(jsonUtils).toFirebaseModel()
                )
                transaction.set(
                    firestore.collection("users").document(currentUser.userID),
                    updatedCurrentUser.toDataBase(jsonUtils).toFirebaseModel()
                )

            }.await()

            //Sincronizar Room
            if (updatedCurrentUser != null) {
                userPreferencesDao.insertUserPreferences(updatedCurrentUser.toDataBase(jsonUtils))
                Result.success(updatedCurrentUser)
            } else {
                Result.failure(Exception("Error en la transacción al aceptar solicitud."))
            }

        } catch (e: Exception) {
            Log.e("RepositoryImpl", "Error en acceptFriendRequest", e)
            Result.failure(e)
        }
    }

    override suspend fun declineFriendRequest(
        currentUser: UserPreferences,
        request: FriendRequestReceived
    ): Result<UserPreferences> = withContext(Dispatchers.IO) {
        try {
            var updatedCurrentUser: UserPreferences? = null

            firestore.runTransaction { transaction ->
                val targetUserId = request.fromUserID

                // Obtener y actualizar Target User (el que envió la solicitud) ---
                val targetUserRef = firestore.collection("users").document(targetUserId)
                val targetSnapshot = transaction.get(targetUserRef)
                val targetFirebaseModel = targetSnapshot.toObject(UserFirebaseModel::class.java)
                    ?: throw IllegalStateException("Usuario objetivo no encontrado.")

                val targetDomainUser = targetFirebaseModel.toEntity().toDomain(jsonUtils)

                // Eliminar de 'friendRequestSent'
                val updatedTargetSent = targetDomainUser.friendRequestSent.filterNot {
                    it.toUserID == currentUser.userID
                }
                val finalTargetUser = targetDomainUser.copy(friendRequestSent = updatedTargetSent)

                //Obtener y actualizar Current User (el que rechaza) ---
                //Eliminar de 'friendRequestReceived'
                val updatedCurrentReceived = currentUser.friendRequestReceived.filterNot {
                    it.fromUserID == targetUserId
                }
                updatedCurrentUser =
                    currentUser.copy(friendRequestReceived = updatedCurrentReceived)

                //Ejecutar la transacción ---
                transaction.set(
                    targetUserRef,
                    finalTargetUser.toDataBase(jsonUtils).toFirebaseModel()
                )
                transaction.set(
                    firestore.collection("users").document(currentUser.userID),
                    updatedCurrentUser.toDataBase(jsonUtils).toFirebaseModel()
                )

            }.await()

            //Sincronizar Room
            if (updatedCurrentUser != null) {
                userPreferencesDao.insertUserPreferences(updatedCurrentUser.toDataBase(jsonUtils))
                Result.success(updatedCurrentUser)
            } else {
                Result.failure(Exception("Error en la transacción al rechazar solicitud."))
            }

        } catch (e: Exception) {
            Log.e("RepositoryImpl", "Error en declineFriendRequest", e)
            Result.failure(e)
        }
    }

    override fun observeCurrentUserFromFirestore(): Flow<UserPreferences?> = callbackFlow {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val userRef = firestore.collection("users").document(userId)

        val listener = userRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("RepositoryImpl", "Error escuchando 'currentUser'", error)
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val firebaseModel = snapshot.toObject(UserFirebaseModel::class.java)
                if (firebaseModel != null) {
                    val entity = firebaseModel.toEntity()

                    launch(Dispatchers.IO) {
                        userPreferencesDao.insertUserPreferences(entity)
                    }
                    trySend(entity.toDomain(jsonUtils))
                } else {
                    trySend(null)
                }
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }
//--------------------Proyectos------------------------TODO(PROYECTOS)


    override fun getAllProjects(): Flow<List<Project>> {
        return projectDao.getAllProjects().map { list -> list.map { it.toDomain(jsonUtils) } }
    }

    override suspend fun getAllProjectsByUser(ownerId: String): Flow<List<Project>> {
        return projectDao.getAllProjectsByUser(ownerId)
            .map { list -> list.map { it.toDomain(jsonUtils) } }
    }

    override suspend fun getProjectById(projectId: String): Project {
        return projectDao.getProjectById(projectId)!!.toDomain(jsonUtils)
    }

    override suspend fun upsertProjectInFirestore(projectModel: ProjectFirebaseModel): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                Log.d("RepositoryImpl", "Guardando proyecto en Firestore: ${projectModel.id}")
                firestore.collection("projects") // Nombre de tu colección
                    .document(projectModel.id) // Usa el ID del proyecto como ID del documento
                    .set(projectModel) // 'set' crea o sobrescribe el documento
                    .await() // Espera a que termine la operación
                Log.d("RepositoryImpl", "Proyecto guardado exitosamente en Firestore.")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(
                    "RepositoryImpl",
                    "Error al guardar proyecto en Firestore (${projectModel.id})",
                    e
                )
                Result.failure(e)
            }
        }

    override suspend fun createPaymentPreference(
        amount: Double,
        description: String
    ): PaymentPreference {

        val accessToken = com.android.harmoniatpi.BuildConfig.MP_ACCESS_TOKEN
        val currentUser = firebaseAuth.currentUser
        val userEmail = currentUser?.email ?: throw Exception("Usuario no tiene email vinculado")
        val myDeepLink = "https://idyllic-fudge-447568.netlify.app/"

        val request = SubscriptionRequest(
            reason = description,
            payerEmail = userEmail,
            autoRecurring = AutoRecurring(transactionAmount = amount),
            backUrl = myDeepLink
        )
        return try {
            val response = mercadoPagoApi.createSubscription(accessToken, request)
            Log.d("MercadoPagos", "LINK DE PAGO GENERADO: ${response.initPoint}")
            PaymentPreference(
                preferenceId = response.initPoint,
                amount = amount,
                description = description
            )

        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("MercadoPagoError", "⚠️ Error ${e.code()}: $errorBody")
            throw e
        } catch (e: Exception) {
            Log.e("MercadoPago", "Error genérico", e)
            throw e
        }
    }

    override suspend fun sendPayment(preferenceId: String): PaymentResult {

        return PaymentResult.PENDING
    }

    override suspend fun cancelSubscription(preapprovalId: String): Result<Unit> {
        // Usamos el mismo token de producción que ya tienes configurado
        val accessToken = com.android.harmoniatpi.BuildConfig.MP_ACCESS_TOKEN

        return try {
            // 1. Llamada a la API de Mercado Pago para cancelar
            val request = SubscriptionStatusUpdateRequest(status = "cancelled")
            mercadoPagoApi.cancelSubscription("Bearer $accessToken", preapprovalId, request)

            // 2. Lógica para volver a FREE en la Base de Datos
            // Obtenemos el usuario actual (que ya sincroniza Firestore -> Local)
            val currentUser = getUserPreferences()

            if (currentUser != null) {
                // Creamos una copia del usuario con isPremium en FALSE
                val updatedUser = currentUser.copy(
                    isPremium = false,
                    // Opcional: Si guardas el subscriptionId en el modelo, podrías borrarlo aquí también
                    // subscriptionId = null
                )

                // Guardamos en Room y Firestore usando tu función existente
                updateUserPreferences(updatedUser)

                Log.i("RepositoryImpl", "Suscripción cancelada en MP y usuario actualizado a FREE local/remoto.")
                Result.success(Unit)
            } else {
                Result.failure(Exception("No se pudo obtener el usuario local para degradar a Free."))
            }
        } catch (e: Exception) {
            Log.e("MercadoPago", "Error cancelando suscripción: ${e.message}")
            Result.failure(e)
        }
    }


    override suspend fun getFirestoreProjectsByUser(userId: String): Flow<List<ProjectFirebaseModel>> =
        callbackFlow {
            // Referencia a la colección 'projects'
            val projectsCollection = firestore.collection("projects")

            // Crea la consulta: filtra por 'ownerId' igual al userId actual
            val query = projectsCollection.whereEqualTo("ownerId", userId)

            // Escucha cambios en los documentos que coinciden con la consulta
            val listenerRegistration = query.addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w("RepositoryImpl", "Error escuchando proyectos de Firestore", error)
                    close(error) // Cierra el flow con error si falla el listener
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    // Mapea los documentos a tu ProjectFirebaseModel
                    val firestoreProjects = snapshots.documents.mapNotNull { doc ->
                        doc.toObject(ProjectFirebaseModel::class.java)
                    }
                    Log.d(
                        "RepositoryImpl",
                        "Proyectos de Firestore recibidos para $userId: ${firestoreProjects.size}"
                    )
                    // Envía la lista actualizada al flow
                    trySend(firestoreProjects).isSuccess // Ignora si el flow ya está cerrado
                }
            }

            // Se ejecuta cuando el flow es cancelado (ej. ViewModel se destruye)
            awaitClose {
                Log.d(
                    "RepositoryImpl",
                    "Cancelando listener de proyectos de Firestore para $userId"
                )
                listenerRegistration.remove() // Detiene la escucha
            }
        }

    override suspend fun deleteProjectFromFirestore(projectId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                firestore.collection("projects").document(projectId).delete().await()
                Log.d("RepositoryImpl", "Proyecto $projectId borrado de Firestore.")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("RepositoryImpl", "Error al borrar de Firestore: $projectId", e)
                Result.failure(e)
            }
        }

    override suspend fun deleteFileFromStorage(remotePath: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                // Si el path está vacío, no hacer nada
                if (remotePath.isBlank()) {
                    return@withContext Result.success(Unit)
                }
                storage.reference.child(remotePath).delete().await()
                Log.d("RepositoryImpl", "Archivo $remotePath borrado de Storage.")
                Result.success(Unit)
            } catch (e: Exception) {
                // Manejar si el objeto no existe (quizás ya fue borrado)
                if (e is com.google.firebase.storage.StorageException && e.errorCode == com.google.firebase.storage.StorageException.ERROR_OBJECT_NOT_FOUND) {
                    Log.w(
                        "RepositoryImpl",
                        "Archivo no encontrado en Storage (quizás ya borrado): $remotePath"
                    )
                    Result.success(Unit) // Es éxito si ya no existe
                } else {
                    Log.e("RepositoryImpl", "Error al borrar de Storage: $remotePath", e)
                    Result.failure(e)
                }
            }
        }

    override suspend fun getProjectByIdFromFirestore(projectId: String): Project? =
        withContext(Dispatchers.IO) {
            try {
                // 1. Busca el documento por ID en la colección "projects"
                val document = firestore.collection("projects")
                    .document(projectId)
                    .get()
                    .await()

                if (document.exists()) {
                    // 2. Lo convierte al modelo de Firebase
                    val firebaseModel = document.toObject(ProjectFirebaseModel::class.java)

                    // 3. Lo convierte al modelo de Dominio (FirebaseModel -> Entity -> Domain)
                    // (Esta es la misma lógica que usas en tu 'sync')
                    firebaseModel?.toEntity()?.toDomain(jsonUtils)
                } else {
                    // El proyecto no existe en Firestore
                    Log.w("RepositoryImpl", "No se encontró el proyecto $projectId en Firestore.")
                    null
                }
            } catch (e: Exception) {
                Log.e("RepositoryImpl", "Error al obtener $projectId de Firestore", e)
                null
            }
        }

    override suspend fun getDerivedProjectsFromFirestore(originalProjectId: String): List<Project> =
        withContext(Dispatchers.IO) {
            try {
                // 1. Prepara la consulta a Firestore
                val querySnapshot = firestore.collection("projects")
                    .whereEqualTo("originalProjectId", originalProjectId) // Busca por ID original
                    .whereEqualTo("published", true) // Solo los que estén publicados
                    .get()
                    .await()

                // 2. Mapea los resultados (FirebaseModel -> Entity -> Domain)
                val projects = querySnapshot.documents.mapNotNull { document ->
                    val firebaseModel = document.toObject(ProjectFirebaseModel::class.java)
                    firebaseModel?.toEntity()?.toDomain(jsonUtils)
                }

                Log.d(
                    "RepositoryImpl",
                    "Encontrados ${projects.size} derivados publicados de $originalProjectId"
                )
                projects

            } catch (e: Exception) {
                Log.e("RepositoryImpl", "Error al obtener derivados de Firestore", e)
                emptyList<Project>() // Devuelve lista vacía en caso de error
            }
        }

    override suspend fun fetchAndSyncUsersFromFirestore(userIds: List<String>): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val chunks = userIds.distinct().chunked(30)
                Log.d(
                    "RepositoryImpl",
                    "Iniciando fetch de ${userIds.size} usuarios en ${chunks.size} lotes."
                )

                chunks.forEach { chunk ->
                    val querySnapshot = firestore.collection("users")
                        .whereIn("userID", chunk)
                        .get()
                        .await()

                    // 1. Mapea de FirebaseModel a Entity
                    querySnapshot.documents.mapNotNull { doc ->
                        doc.toObject(UserFirebaseModel::class.java)
                            ?.toEntity() // Convierte a UserPreferencesEntity

                        // --- ✨ 2. SANITIZA LA INFORMACIÓN ANTES DE GUARDAR ✨ ---
                    }.forEach { entity ->
                        // Creamos una copia de la entidad, pero borrando
                        // todos los datos privados/innecesarios.
                        val sanitizedEntity = entity.copy(
                            friendsList = "[]", // Borra la lista de amigos de otros
                            projectsList = "[]", // Borra sus proyectos
                            myPostsList = "[]", // Borra sus posts
                            notificationList = "[]", // Borra sus notificaciones
                            friendRequestReceived = "[]",
                            friendRequestSent = "[]"
                            // Dejamos intactos: userID, userName, userLastName,
                            // userPhotoPath y userPhotoPathRemote
                        )

                        // 3. Inserta la entidad "limpia" en Room
                        userPreferencesDao.insertUserPreferences(sanitizedEntity)
                    }
                }

                Log.d("RepositoryImpl", "Sincronización de usuarios (sanitizada) completada.")
                Result.success(Unit)

            } catch (e: Exception) {
                Log.e("RepositoryImpl", "Error en fetchAndSyncUsersFromFirestore", e)
                Result.failure(e)
            }
        }

    override suspend fun deletePostByProjectId(projectId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                // 1. Buscamos el post que tenga este idProject
                val snapshot = database.reference.child("posts")
                    .orderByChild("idProject")
                    .equalTo(projectId)
                    .get()
                    .await()

                // 2. Iteramos (por si acaso hubiera duplicados, aunque debería ser uno)
                for (child in snapshot.children) {
                    child.ref.removeValue().await()
                    // También borramos de Room local si es necesario
                    child.key?.let { postId ->
                        myPostDao.deletePostById(postId)
                    }
                }

                Log.i("RepositoryImpl", "Post asociado al proyecto $projectId eliminado.")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("RepositoryImpl", "Error borrando post asociado al proyecto $projectId", e)
                Result.failure(e)
            }
        }

    //--------------------ProyectosFin------------------------

}
