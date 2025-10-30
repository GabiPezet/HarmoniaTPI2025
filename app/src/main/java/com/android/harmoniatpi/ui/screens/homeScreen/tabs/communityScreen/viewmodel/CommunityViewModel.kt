package com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.model.userPreferences.Comment
import com.android.harmoniatpi.domain.model.userPreferences.Post
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.DeletePostFirebaseDataBaseUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.GetAllPostFromFirebaseDataBaseUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.InsertNewPostFirebaseDataBaseUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.UpdatePostFirebaseDataBaseUseCase
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.model.CommunityUiState
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.SharedMenuUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject
import java.util.UUID
import com.android.harmoniatpi.domain.usecases.GetProjectByIdUseCase
import com.android.harmoniatpi.domain.usecases.roomUseCases.GetAllProjectsFromDBUseCase
import com.android.harmoniatpi.domain.usecases.roomUseCases.UpdateOrInsertProjectInDBUseCase
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val sharedMenuUiState: SharedMenuUiState,
    private val insertNewPostFirebaseDataBaseUseCase: InsertNewPostFirebaseDataBaseUseCase,
    private val getAllPostFromFirebaseDataBaseUseCase: GetAllPostFromFirebaseDataBaseUseCase,
    private val updatePostFirebaseDataBaseUseCase: UpdatePostFirebaseDataBaseUseCase,
    private val deletePostFirebaseDataBaseUseCase: DeletePostFirebaseDataBaseUseCase,

    private val getAllProjectsFromDBUseCase: GetAllProjectsFromDBUseCase,
    private val getProjectByIdUseCase: GetProjectByIdUseCase,
    private val insertProjectInDBUseCase: UpdateOrInsertProjectInDBUseCase
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
            // Combinamos los posts de Firebase con los proyectos locales
            combine(
                getAllPostFromFirebaseDataBaseUseCase(),
                localProjectsFlow
            ) { posts, localProjects ->
                _uiState.update {
                    it.copy(
                        posts = posts,
                        localProjects = localProjects // Guarda los proyectos locales
                    )
                }
            }.collect{}
        }
    }

    fun cloneProject(post: Post) {
        val currentUserId = _uiState.value.userID
        // Si el post no es un proyecto, o si yo soy el dueño, no hago nada.
        if (post.idProject.isBlank() || post.userID == currentUserId) return

        viewModelScope.launch {
            try {
                // 1. Obtiene el proyecto original (asumiendo que está en la DB local por ahora)
                // (En el futuro, esto sería una llamada a Firebase: getRemoteProjectByIdUseCase(post.idProject))
                val originalProject = getProjectByIdUseCase(post.idProject)

                // 2. Crea el clon
                val clonedProject = originalProject.copy(
                    id = UUID.randomUUID().toString(),
                    ownerId = currentUserId,
                    name = _uiState.value.userName,
                    lastName = _uiState.value.userLastName,
                    originalProjectId = originalProject.id,
                    forkedByUserIds = emptyList()
                )
                insertProjectInDBUseCase(clonedProject)

                // 3. Actualiza el original (local)
                val updatedForkedIds = originalProject.forkedByUserIds + (currentUserId)
                val updatedOriginal = originalProject.copy(
                    forkedByUserIds = updatedForkedIds
                )
                insertProjectInDBUseCase(updatedOriginal)

                // 4. Actualiza el Post (remoto) para sumar un "clon"
                val updatedPost = post.copy(totalShared = post.totalShared + 1)
                updatePostFirebaseDataBaseUseCase(updatedPost)

                _toastEvents.emit("Proyecto clonado en colaboraciones.")

                // (Opcional: puedes añadir un callback 'onSuccess' para navegar)

            } catch (e: Exception) {
                // Manejar error (ej. el proyecto original no se encontró localmente)
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
            } catch (e: Exception) {
                // Manejar error (ej. el post no se pudo borrar, o el proyecto local no se encontró)
            }
        }
   }
}
