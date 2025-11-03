package com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.model.userPreferences.Comment
import com.android.harmoniatpi.domain.model.userPreferences.Post
import com.android.harmoniatpi.domain.usecases.GetProjectByIdUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.DeletePostFirebaseDataBaseUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.GetAllPostFromFirebaseDataBaseUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.GetProjectByIdFromFirestoreUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.GetUserOnFirebaseByIDUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.InsertNewPostFirebaseDataBaseUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.UpdatePostFirebaseDataBaseUseCase
import com.android.harmoniatpi.domain.usecases.roomUseCases.GetAllProjectsFromDBUseCase
import com.android.harmoniatpi.domain.usecases.roomUseCases.UpdateOrInsertProjectInDBUseCase
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.model.CommunityUiState
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.SharedMenuUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val sharedMenuUiState: SharedMenuUiState,
    private val insertNewPostFirebaseDataBaseUseCase: InsertNewPostFirebaseDataBaseUseCase,
    private val getAllPostFromFirebaseDataBaseUseCase: GetAllPostFromFirebaseDataBaseUseCase,
    private val updatePostFirebaseDataBaseUseCase: UpdatePostFirebaseDataBaseUseCase,
    private val deletePostFirebaseDataBaseUseCase: DeletePostFirebaseDataBaseUseCase,
    private val getProjectByIdFromFirestoreUseCase: GetProjectByIdFromFirestoreUseCase,
    getAllProjectsFromDBUseCase: GetAllProjectsFromDBUseCase,
    private val getProjectByIdUseCase: GetProjectByIdUseCase,
    private val insertProjectInDBUseCase: UpdateOrInsertProjectInDBUseCase,
    private val getUserOnFirebaseByIDUseCase: GetUserOnFirebaseByIDUseCase
) : ViewModel() {

    // 1. Canal privado para enviar eventos de Toast
    private val _toastEvents = MutableSharedFlow<String>()

    // 2. Canal público para que la UI escuche
    val toastEvents = _toastEvents.asSharedFlow()

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState = _uiState.asStateFlow()
    private val localProjectsFlow = getAllProjectsFromDBUseCase()

    init {
        viewModelScope.launch {
            sharedMenuUiState.uiState.collect { state ->
                _uiState.update {
                    it.copy(
                        userName = state.userName,
                        userLastName = state.userLastName,
                        userID = state.userID,
                        userPhotoPathRemote = state.userPhotoPathRemote
                    )
                }
            }
        }

        viewModelScope.launch {
            combine(
                getAllPostFromFirebaseDataBaseUseCase(),
                localProjectsFlow
            ) { posts, localProjects ->

                val currentCloningId = _uiState.value.cloningPostId
                var newCloningId = currentCloningId

                if (currentCloningId != null) {
                    val postBeingCloned = posts.find { it.id == currentCloningId }
                    val isNowCloned = localProjects.any {
                        it.originalProjectId == postBeingCloned?.idProject && it.ownerId == _uiState.value.userID
                    }
                    if (isNowCloned) {
                        newCloningId = null
                    }
                }
                _uiState.update {
                    it.copy(
                        posts = posts,
                        localProjects = localProjects,
                        cloningPostId = newCloningId
                    )
                }
            }.collect {}
        }
    }


    private fun loadLocalProjects(getAllProjectsFromDBUseCase: GetAllProjectsFromDBUseCase) {
        viewModelScope.launch {
            // Tomamos solo el primer valor (la lista actual) y paramos de escuchar
            val initialLocalProjects = getAllProjectsFromDBUseCase().firstOrNull() ?: emptyList()
            _uiState.update {
                it.copy(localProjects = initialLocalProjects)
            }
        }
    }

    fun cloneProject(post: Post) {
        val currentUserId = _uiState.value.userID
        if (post.idProject.isBlank() || post.userID == currentUserId) return
        _uiState.update { it.copy(cloningPostId = post.id) }

        viewModelScope.launch {


            try {
                val originalProject = getProjectByIdFromFirestoreUseCase(post.idProject)

                if (originalProject == null) {
                    _toastEvents.emit("Error: No se pudo encontrar el proyecto original.")
                    return@launch
                }
                insertProjectInDBUseCase(originalProject)

                val clonedProject = originalProject.copy(
                    id = UUID.randomUUID().toString(),
                    ownerId = currentUserId,
                    name = _uiState.value.userName,
                    lastName = _uiState.value.userLastName,
                    originalProjectId = originalProject.id,
                    forkedByUserIds = emptyList(),
                    isPublished = false
                )

                insertProjectInDBUseCase(clonedProject)

                updateCloned(post)
                _toastEvents.emit("Proyecto clonado en colaboraciones.")

            } catch (e: Exception) {
                _toastEvents.emit("Error al clonar: ${e.message}")
                _uiState.update { it.copy(cloningPostId = null) }
            }
        }
    }

    fun onNewPostClicked() {
        _uiState.update { it.copy(showCreateDialog = true) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun addPost(title: String, description: String, hashtags: List<String>) {
        val newPost = Post(
            id = System.currentTimeMillis().toString(),
            userID = _uiState.value.userID,
            userImagePathURL = _uiState.value.userPhotoPathRemote,
            title = title,
            description = description,
            name = _uiState.value.userName,
            lasName = _uiState.value.userLastName,
            hashtags = hashtags,
            idProject = "",
            urlCompleteAudio = "",
            urlAudioTracks = emptyList(),
            imageUrl = "",
            createdAt = LocalDateTime.now().toString(),
            likes = 0,
            totalShared = 0,
            comments = emptyList(),
            clonedOption = false
        )
        viewModelScope.launch {
            insertNewPostFirebaseDataBaseUseCase(newPost)
        }

    }

    fun updateLikes(post: Post) {
        val newPost = post.copy(likes = post.likes + 1)
        viewModelScope.launch {
            updatePostFirebaseDataBaseUseCase(newPost)
        }
    }

    fun updateCloned(post: Post) {
        val newPost = post.copy(totalShared = post.totalShared + 1)
        viewModelScope.launch {
            updatePostFirebaseDataBaseUseCase(newPost)
        }
    }

    fun updateComments(post: Post, comment: String) {
        val newComment = Comment(
            id = System.currentTimeMillis().toString(),
            name = _uiState.value.userName,
            lastName = _uiState.value.userLastName,
            comment = comment,
            photoUrlUser = _uiState.value.userPhotoPathRemote
        )
        val newPost = post.copy(comments = post.comments + newComment)
        viewModelScope.launch {
            updatePostFirebaseDataBaseUseCase(newPost)
        }

    }

    fun deleteMyPost(post: Post) {
        viewModelScope.launch {
            try {
                // 1. Borra el Post de Firebase (Remoto)
                deletePostFirebaseDataBaseUseCase(post.id)

                // 2. Comprueba si este Post estaba vinculado a un Proyecto
                if (post.idProject.isNotBlank()) {

                    // 3. Busca el Proyecto original en la BBDD local (Room)
                    val localProject = getProjectByIdUseCase(post.idProject)

                    // 4. Lo actualiza, marcándolo como "no publicado"
                    val unpublishedProject = localProject.copy(isPublished = false)
                    insertProjectInDBUseCase(unpublishedProject)
                }
            } catch (_: Exception) {
                // Manejar error (ej. el post no se pudo borrar, o el proyecto local no se encontró)
            }
        }
    }

    fun onClickUserProfile(id: String) {
        viewModelScope.launch {
            val currentUser = getUserOnFirebaseByIDUseCase(id)
            Log.i("KlyxDevs", "UserClicked: $currentUser")
            if (currentUser != null) {
                _uiState.update { it.copy(showUserProfile = true, userSelected = currentUser) }
            }
        }

    }

    fun onDismissUserProfile() {
        _uiState.update { it.copy(showUserProfile = false) }
    }
}
