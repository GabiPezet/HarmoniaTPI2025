package com.android.harmoniatpi.domain.interfaces

import com.android.harmoniatpi.data.database.entities.MyPostEntity
import com.android.harmoniatpi.data.local.model.ProjectFirebaseModel
import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.payment.PaymentPreference
import com.android.harmoniatpi.domain.model.payment.PaymentResult
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.domain.model.user.User
import com.android.harmoniatpi.domain.model.userPreferences.FriendRequestReceived
import com.android.harmoniatpi.domain.model.userPreferences.Post
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface Repository {
    fun getFirebaseCurrentUser(): FirebaseUser?

    suspend fun getUserById(userId: String): UserPreferences?

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

    fun getAllUser (): Flow<List<UserPreferences>>

    fun getAllPostsFlowRealTimeDB(): Flow<List<Post>>

    suspend fun insertPostRealTimeDB(post: Post)

    suspend fun updatePostRealTimeDB(post: Post)

    suspend fun deletePostByIdRealTimeDB(id: String)

    suspend fun uploadLocalFileToFirebaseStorage(
        localFilePath: String,
        remotePath: String
    ): Result<String>

    suspend fun deleteFileFromStorage(remotePath: String): Result<Unit>

    suspend fun fetchAndSyncUsersFromFirestore(userIds: List<String>): Result<Unit>

    fun getMyPosts(): Flow<List<Post>>

    suspend fun insertMyPost(post: MyPostEntity)

    suspend fun updateMyPost(post: MyPostEntity)

    suspend fun deleteMyPostById(id: String)

    suspend fun sendFriendRequest(
        currentUser: UserPreferences,
        targetUser: UserPreferences
    ): Result<UserPreferences>

    suspend fun acceptFriendRequest(
        currentUser: UserPreferences,
        request: FriendRequestReceived
    ): Result<UserPreferences>

    suspend fun declineFriendRequest(
        currentUser: UserPreferences,
        request: FriendRequestReceived
    ): Result<UserPreferences>

    fun observeCurrentUserFromFirestore(): Flow<UserPreferences?>

    //--------------------Proyectos------------------------TODO(PROYECTOS)
    fun getAllProjects ():Flow<List<Project>>

    suspend fun getAllProjectsByUser(ownerId: String): Flow<List<Project>>

    suspend fun deleteProject(projectId: String)

    suspend fun insertOrUpdateProject(project: Project)

    suspend fun getProjectById(projectId: String): Project

    suspend fun getDerivedProjectsFromFirestore(originalProjectId: String): List<Project>

    suspend fun getProjectByIdFromFirestore(projectId: String): Project?

    suspend fun getFirestoreProjectsByUser(userId: String): Flow<List<ProjectFirebaseModel>>

    suspend fun deleteProjectFromFirestore(projectId: String): Result<Unit>

    suspend fun upsertProjectInFirestore(projectModel: ProjectFirebaseModel): Result<Unit>

    suspend fun createPaymentPreference(amount: Double, description: String): PaymentPreference

    suspend fun sendPayment(preferenceId: String): PaymentResult

}