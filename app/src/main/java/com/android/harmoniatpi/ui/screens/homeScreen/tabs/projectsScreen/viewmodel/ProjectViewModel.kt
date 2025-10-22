package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.cache.HoloJamCache
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.domain.model.userPreferences.Post

import com.android.harmoniatpi.domain.usecases.GetProjectByIdUseCase
import com.android.harmoniatpi.domain.usecases.GetProjectsByUserUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.InsertNewPostFirebaseDataBaseUseCase
import com.android.harmoniatpi.domain.usecases.roomUseCases.DeleteProjectByIdFromDBUseCase
import com.android.harmoniatpi.domain.usecases.roomUseCases.GetAllProjectsFromDBUseCase
import com.android.harmoniatpi.domain.usecases.roomUseCases.UpdateOrInsertProjectInDBUseCase

import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.model.ProjectTab
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.model.ProjectUiState
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.SharedMenuUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val getAllProjectsFromDBUseCase: GetAllProjectsFromDBUseCase,
    private val getProjectsByUserUseCase: GetProjectsByUserUseCase,
    private val getProjectByIdUseCase: GetProjectByIdUseCase,
    private val insertProjectInDBUseCase: UpdateOrInsertProjectInDBUseCase,
    private val deleteProjectByIdFromDBUseCase: DeleteProjectByIdFromDBUseCase,
    private val insertNewPostFirebaseDataBaseUseCase: InsertNewPostFirebaseDataBaseUseCase, //para crear el post
    internal val sharedMenuUiState: SharedMenuUiState,
    private val holoJamCache: HoloJamCache
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadMyProjects()
        loadCollabProjects()
    }

    // --- Cargar todos los proyectos de la base de datos
    fun loadAllProjects() {
        viewModelScope.launch {
            getAllProjectsFromDBUseCase()
                .collect { projects ->
                    _uiState.update {
                        it.copy(allProjects = projects)
                    }
                }
        }
    }
    // Filtrando por usuario
    fun loadMyProjects() {
        val currentUserId = sharedMenuUiState.uiState.value.userID
        if (currentUserId.isBlank()) {
            _uiState.update { it.copy(myProjects = emptyList()) }
            return
        }

        viewModelScope.launch {
            getProjectsByUserUseCase(currentUserId)
                .collect { projects ->
                    // Actualiza solo la lista de "mis proyectos"
                    _uiState.update { it.copy(myProjects = projects) }
                }
        }
    }

    fun saveProjectEdits(
        projectToSave: Project,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUserId = sharedMenuUiState.uiState.value.userID
        if (currentUserId.isBlank()) {
            onError("Usuario no válido")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // 1. Guarda el proyecto editado (ej. "Cancion dos")
                insertProjectInDBUseCase(projectToSave)

                _uiState.update { it.copy(isLoading = false) }
                onSuccess()

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                onError(e.message ?: "Error desconocido")
            }
        }
    }
    // Esta función ahora filtra la lista para Colaboraciones
    fun loadCollabProjects() {
        val currentUserId = sharedMenuUiState.uiState.value.userID
        if (currentUserId.isBlank()) return

        viewModelScope.launch {
            getAllProjectsFromDBUseCase()
                .collect { allProjectsList ->

                    //Filtramos la lista para mostrar ÚNICAMENTE mis clones
                    val myClones = allProjectsList.filter { project ->
                        // Condición: Es un clon (tiene un ID original) Y yo soy el dueño.
                        project.originalProjectId != null && project.ownerId == currentUserId
                    }

                    _uiState.update {
                        it.copy(
                            // 'allProjects' ahora solo contiene mis clones
                            allProjects = myClones
                        )
                    }
                }
        }
    }

    // --- Borrar un proyecto por ID
    fun deleteProject(id: String) {
        val currentUserId = sharedMenuUiState.uiState.value.userID
        if (currentUserId.isBlank()) return

        viewModelScope.launch {
            try {
                // 1. Obtenemos el proyecto ANTES de borrarlo
                val projectToDelete = getProjectByIdUseCase(id)

                // 2. Lo borramos
                deleteProjectByIdFromDBUseCase(id)

                // 3. Verificamos si era un clon nuestro
                if (projectToDelete.originalProjectId != null && projectToDelete.ownerId == currentUserId) {

                    // 4. Si era un clon, buscamos el original
                    val originalProject = try {
                        getProjectByIdUseCase(projectToDelete.originalProjectId)
                    } catch (e: Exception) { null } // El original ya no existe

                    // 5. Si el original existe y nos tiene en su lista, nos quitamos
                    if (originalProject != null && originalProject.forkedByUserIds.contains(currentUserId)) {
                        val updatedForkedIds = originalProject.forkedByUserIds.filter { it != currentUserId }
                        val updatedOriginal = originalProject.copy(
                            forkedByUserIds = updatedForkedIds
                        )
                        insertProjectInDBUseCase(updatedOriginal)
                    }
                }
            } catch (e: Exception) {
                // Manejar error si no se pudo encontrar el proyecto a borrar
            }
        }
    }

    // --- Handlers de campos
    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
        validateForm()
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
        validateForm()
    }

    fun onHashtagsChange(hashtags: String) {
        _uiState.update { it.copy(hashtags = hashtags) }
        validateForm()
    }

    // --- Guardar un proyecto nuevo
    fun saveProject(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!_uiState.value.isFormValid) return

        val currentUserId = sharedMenuUiState.uiState.value.userID
        if (currentUserId.isBlank()) {
            onError("Error: Usuario no identificado.")
            return
        }
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val current = _uiState.value
                val project = Project(
                    id = UUID.randomUUID().toString(),
                    ownerId = currentUserId,
                    name = sharedMenuUiState.uiState.value.userName,
                    lastName = sharedMenuUiState.uiState.value.userLastName,
                    title = current.title,
                    description = current.description,
                    duration = 0L,
                    createdAt = LocalDateTime.now().toString(),
                    status = true,
                    likes = 0,
                    totalShared = 0,
                    comments = emptyList(),
                    urlCompleteAudio = null,
                    urlAudioTracks = emptyList(),
                    hashtags = current.hashtags.split(",").map { it.trim() },
                    forkedByUserIds = emptyList(),
                    isPublished = false
                )

                insertProjectInDBUseCase(project)


                _uiState.update {
                    it.copy(
                        title = "",
                        description = "",
                        hashtags = "",
                        isLoading = false
                    )
                }

                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                onError(e.message ?: "Error desconocido")
            }
        }
    }

    fun publishProject(project: Project) {
        viewModelScope.launch {
            try {
                // 1. Marca el proyecto local como "publicado"
                val publishedProject = project.copy(isPublished = true)
                insertProjectInDBUseCase(publishedProject)

                // (Simulación futura) Aquí es donde subirías el audio a Storage
                // val fullAudioUrl = uploadAudioToStorage(project.urlCompleteAudio)
                // val tracksUrls = uploadTracksToStorage(project.urlAudioTracks)
                val simulatedAudioUrl = "simulated_audio_url_for_${project.id}.mp3"

                // 2. Crea el 'Post' para la comunidad
                val post = Post(
                    id = System.currentTimeMillis().toString(),
                    userID = project.ownerId,
                    userImagePathURL = sharedMenuUiState.uiState.value.userPhotoPathRemote,
                    title = project.title,
                    description = project.description,
                    name = project.name,
                    lasName = project.lastName,
                    hashtags = project.hashtags,
                    idProject = project.id, // ENLACE CLAVE
                    urlCompleteAudio = simulatedAudioUrl, //Audio para el reproductor
                    urlAudioTracks = emptyList(), // (O las URLs reales si ya las tuvieras)
                    imageUrl = "", // Podemos añadir una imagen del proyecto si queremos.
                    createdAt = LocalDateTime.now().toString(),
                    clonedOption = true // Indica que este post se puede clonar
                )

                // 3. Inserta el Post en la base de datos remota (Firebase)
                insertNewPostFirebaseDataBaseUseCase(post)

            } catch (e: Exception) {
                // Manejar error de publicación
            }
        }
    }


    // --- Validación del formulario
    private fun validateForm() {
        val currentState = _uiState.value
        val isTitleValid = currentState.title.isNotBlank()
        val isFormValid = isTitleValid

        _uiState.update {
            it.copy(
                isTitleValid = isTitleValid,
                isFormValid = isFormValid
            )
        }
    }

    fun onTabSelected(tab: ProjectTab) {
        _uiState.update { it.copy(tabSelected = tab) }
        if (tab == ProjectTab.MY_PROJECTS) {
            loadMyProjects()
        } else {
            loadCollabProjects() // Llamamos a la nueva función filtrada
        }
    }

    fun setCurrentProject(project: Project) {
        holoJamCache.currentProjectSelected = project
    }

    fun togglePlayPause(project: Project) {
        _uiState.update { currentState ->
            // Si ya se estaba reproduciendo este proyecto, lo detenemos (null)
            if (currentState.currentlyPlayingProject?.id == project.id) {
                currentState.copy(currentlyPlayingProject = null)
            }
            // Si no, empezamos a reproducir este
            else {
                currentState.copy(currentlyPlayingProject = project)
            }
        }
        // TODO: Aquí interactuaríamos con nuestro servicio/clase de reproducción real
    }

    // Para detener la reproducción (ej. desde el mini-player)
    fun stopPlayback() {
        _uiState.update { it.copy(currentlyPlayingProject = null) }
        // TODO: Detener la reproducción real
    }
}