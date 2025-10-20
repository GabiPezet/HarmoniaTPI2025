package com.android.harmoniatpi.domain.interfaces


import com.android.harmoniatpi.data.database.entities.ProjectEntity
import com.android.harmoniatpi.data.database.entities.MyPostEntity
import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.domain.model.userPreferences.Post
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

    fun getAllProjectsByUser(ownerId: String): Flow<List<Project>>

    suspend fun deleteProject(projectId: String)

    suspend fun insertOrUpdateProject(project: Project)

    suspend fun getProjectById(projectId: String): Project

    fun getAllPostsFlowRealTimeDB(): Flow<List<Post>>

    suspend fun insertPostRealTimeDB(post: Post)

    suspend fun updatePostRealTimeDB(post: Post)

    suspend fun deletePostByIdRealTimeDB(id: String)

    suspend fun uploadLocalFileToFirebaseStorage(
        localFilePath: String,
        remotePath: String
    ): Result<String>

    fun getMyPosts(): Flow<List<Post>>

    suspend fun insertMyPost(post: MyPostEntity)

    suspend fun updateMyPost(post: MyPostEntity)

    suspend fun deleteMyPostById(id: String)
}