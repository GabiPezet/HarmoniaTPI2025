package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.cache.HoloJamCache
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.domain.usecases.DeleteProjectByIdFromDBUseCase
import com.android.harmoniatpi.domain.usecases.GetAllProjectsFromDBUseCase
import com.android.harmoniatpi.domain.usecases.GetProjectsByUserUseCase
import com.android.harmoniatpi.domain.usecases.UpdateOrInsertProjectInDBUseCase
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
    private val insertProjectInDBUseCase: UpdateOrInsertProjectInDBUseCase,
    private val deleteProjectByIdFromDBUseCase: DeleteProjectByIdFromDBUseCase,
    internal val sharedMenuUiState: SharedMenuUiState,
    private val holoJamCache: HoloJamCache
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadMyProjects()
    }



    // --- Cargar todos los proyectos de la base de datos
    fun loadProjects() {
        viewModelScope.launch {
            getAllProjectsFromDBUseCase()
                .collect { projects ->
                    _uiState.update {
                        it.copy(listProjects = projects)
                    }
                    sharedMenuUiState.updateState {
                        it.copy(listProjects = projects)
                    }
                }
        }
    }
    // Filtrando por usuario
    fun loadMyProjects() {
        val currentUserId = sharedMenuUiState.uiState.value.userID
        if (currentUserId.isBlank()) {
            _uiState.update { it.copy(listProjects = emptyList()) }
            return
        }

        viewModelScope.launch {
            getProjectsByUserUseCase(currentUserId)
                .collect { projects ->
                    _uiState.update { it.copy(listProjects = projects) }
                    sharedMenuUiState.updateState { it.copy(projectsList = projects) }
                }
        }
    }

    fun forkProject(project: Project) {
        val currentUserId = sharedMenuUiState.uiState.value.userID
        if (currentUserId.isBlank()) return

        if (project.ownerId == currentUserId) {
            return
        }
        // Evita añadir duplicados si el usuario ya le dio a guardar
        if (project.forkedByUserIds.contains(currentUserId)) return

        viewModelScope.launch {
            // Crea una nueva lista de IDs añadiendo el actual
            val updatedForkedIds = project.forkedByUserIds + currentUserId

            // Crea una copia del proyecto con la lista actualizada
            val updatedProject = project.copy(forkedByUserIds = updatedForkedIds)

            // Guarda el proyecto actualizado en la base de datos
            insertProjectInDBUseCase(updatedProject)
        }
    }

    // --- Borrar un proyecto por ID
    fun deleteProject(id: String) {
        viewModelScope.launch {
            deleteProjectByIdFromDBUseCase(id)
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
                    forkedByUserIds = emptyList()
                )

                insertProjectInDBUseCase(project)
                loadMyProjects()

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
            loadProjects()
        }
    }

    fun setCurrentProject(project: Project) {
        holoJamCache.currentProjectSelected = project
    }
}
