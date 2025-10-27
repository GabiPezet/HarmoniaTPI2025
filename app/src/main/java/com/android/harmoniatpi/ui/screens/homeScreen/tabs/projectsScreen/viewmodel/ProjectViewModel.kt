package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.viewmodel

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.cache.HoloJamCache
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.domain.model.userPreferences.Post

import com.android.harmoniatpi.domain.usecases.GetProjectByIdUseCase
import com.android.harmoniatpi.domain.usecases.GetProjectsByUserUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.ExportProjectUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.MixTracksUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.OnPreviewCompletedUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.PlayPreviewUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.StopPreviewUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.InsertNewPostFirebaseDataBaseUseCase
import com.android.harmoniatpi.domain.usecases.roomUseCases.DeleteProjectByIdFromDBUseCase
import com.android.harmoniatpi.domain.usecases.roomUseCases.GetAllProjectsFromDBUseCase
import com.android.harmoniatpi.domain.usecases.roomUseCases.GetProjectByIdFromDBUseCase
import com.android.harmoniatpi.domain.usecases.roomUseCases.UpdateOrInsertProjectInDBUseCase

import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.model.ProjectTab
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.model.ProjectUiState
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.SharedMenuUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getAllProjectsFromDBUseCase: GetAllProjectsFromDBUseCase,
    private val getProjectsByUserUseCase: GetProjectsByUserUseCase,
    private val getProjectByIdUseCase: GetProjectByIdUseCase,
    private val insertProjectInDBUseCase: UpdateOrInsertProjectInDBUseCase,
    private val deleteProjectByIdFromDBUseCase: DeleteProjectByIdFromDBUseCase,
    private val insertNewPostFirebaseDataBaseUseCase: InsertNewPostFirebaseDataBaseUseCase, //para crear el post
    private val exportProjectUseCase: ExportProjectUseCase,
    private val getProjectByIdFromDBUseCase: GetProjectByIdFromDBUseCase,
    private val playPreviewUseCase: PlayPreviewUseCase,
    private val stopPreviewUseCase: StopPreviewUseCase,
    private val onPreviewCompletedUseCase: OnPreviewCompletedUseCase,
    private val mixTracksUseCase: MixTracksUseCase,
    internal val sharedMenuUiState: SharedMenuUiState,
    private val holoJamCache: HoloJamCache
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectUiState())
    val uiState = _uiState.asStateFlow()
    private var previewMixJob: Job? = null


    init {
        loadMyProjects()
        loadCollabProjects()
        listenForPreviewCompletion()
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
        if (project.isPublished) {
            Toast.makeText(context, "Este proyecto ya está publicado.", Toast.LENGTH_SHORT).show()
            return
        }
        // Considerar isPublishing para UiState
        // _uiState.update { it.copy(isPublishing = true, currentlyPublishingId = project.id) }

        viewModelScope.launch {
            withContext(Dispatchers.Main) { Toast.makeText(context, "Preparando publicación...", Toast.LENGTH_SHORT).show() }
            var mixedMp3File: File? = null
            var individualMp3Files: List<File> = emptyList()

            try {
                Log.d("ProjectViewModel", "Iniciando generación de MP3 para publicación de ${project.id}...")
                val updatedProject = getProjectByIdFromDBUseCase(project.id)
                val trackPaths = updatedProject.urlAudioTracks.map { it.path }

                if (trackPaths.isEmpty()) throw IOException("El proyecto no tiene audio para publicar.")

                val exportResult = exportProjectUseCase(project.id, trackPaths).getOrThrow()
                mixedMp3File = exportResult.mixedMp3
                individualMp3Files = exportResult.individualMp3s // Guarda las individuales
                Log.i("ProjectViewModel", "MP3 generados localmente en: ${mixedMp3File?.parent ?: "N/A"}")

                val mainMp3ToUse = mixedMp3File ?: exportResult.individualMp3s.firstOrNull()
                if (mainMp3ToUse == null) throw IOException("No se pudo generar el archivo MP3 principal.")

                var finalAudioUrl = ""

                Log.d("ProjectViewModel", "-> PASO PENDIENTE: Subir ${mainMp3ToUse.name} a Firebase Storage.")
                // aca iria el codigo real para la subida a storage
                withContext(Dispatchers.Main){ Toast.makeText(context, "Subiendo audio...", Toast.LENGTH_SHORT).show() } // Feedback
                delay(2000)
                finalAudioUrl = "https://firebasestorage.googleapis.com/v0/b/your-bucket/o/project_audio%2F${project.id}%2F${mainMp3ToUse.name}?alt=media" // URL simulada
                Log.d("ProjectViewModel", "URL (simulada) obtenida: $finalAudioUrl")
                // Manejo errores aca


                val publishedProject = updatedProject.copy(
                    isPublished = true,
                    // publishedAudioUrl = finalAudioUrl // Guardar URL
                )
                insertProjectInDBUseCase(publishedProject)
                Log.d("ProjectViewModel", "Proyecto ${project.id} marcado como publicado localmente.")

                // Crear Post en Firebase Realtime DB
                val post = Post(
                    id = System.currentTimeMillis().toString(), userID = project.ownerId,
                    userImagePathURL = sharedMenuUiState.uiState.value.userPhotoPathRemote,
                    title = project.title, description = project.description, name = project.name,
                    lasName = project.lastName, hashtags = project.hashtags, idProject = project.id,
                    urlCompleteAudio = finalAudioUrl,
                    urlAudioTracks = emptyList(),
                    imageUrl = "", createdAt = LocalDateTime.now().toString(), clonedOption = true
                )
                insertNewPostFirebaseDataBaseUseCase(post)
                Log.i("ProjectViewModel", "Post creado en Firebase Realtime DB para ${project.id}")
                if (_uiState.value.tabSelected == ProjectTab.MY_PROJECTS) loadMyProjects() else loadCollabProjects()

                withContext(Dispatchers.Main) { Toast.makeText(context, "¡Proyecto publicado!", Toast.LENGTH_LONG).show() }

            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error publicando proyecto ${project.id}", e)
                withContext(Dispatchers.Main) { Toast.makeText(context, "Error al publicar: ${e.localizedMessage}", Toast.LENGTH_LONG).show() }
            } finally {
                // Quitar estado de carga
                // _uiState.update { it.copy(isPublishing = false, currentlyPublishingId = null) }
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
        stopPlayback()
        if (tab == ProjectTab.MY_PROJECTS) loadMyProjects() else loadCollabProjects()
    }

    fun setCurrentProject(project: Project) {
        holoJamCache.currentProjectSelected = project
    }

    fun togglePlayPause(project: Project) {
        val currentlyPlayingId = _uiState.value.currentlyPlayingProject?.id
        val requestedId = project.id

        stopPlayback() // Detiene y limpia siempre
        previewMixJob?.cancel()

        if (currentlyPlayingId != requestedId) {
            _uiState.update { it.copy(currentlyPlayingProject = project, isPreviewLoading = true) }
            startPreviewPlayback(project)
        }
    }

    private fun startPreviewPlayback(project: Project) {
        previewMixJob = viewModelScope.launch(Dispatchers.IO) {
            val trackPaths = project.urlAudioTracks.map { it.path }
            if (trackPaths.isEmpty()) {
                Log.w("ProjectViewModel", "Proyecto ${project.id} sin pistas para preview.")
                withContext(Dispatchers.Main){ Toast.makeText(context, "El proyecto no tiene pistas.", Toast.LENGTH_SHORT).show()}
                resetPlaybackState()
                return@launch
            }

            val previewMixFileName = "preview_${project.id}_mix.pcm"
            val mixedPcmFile = File(context.cacheDir, previewMixFileName)

            try {

                if (!mixedPcmFile.exists() || trackPaths.size > 1) {
                    Log.d("ProjectViewModel", "Mezclando/Copiando para preview de ${project.id}...")
                    val sourceFile = if(trackPaths.size > 1) {
                        mixTracksUseCase(project.id, trackPaths)
                    } else {
                        File(trackPaths.first())
                    }

                    if (sourceFile == null || !sourceFile.exists()) {
                        throw IOException("Fallo al obtener archivo fuente para preview.")
                    }
                    sourceFile.copyTo(mixedPcmFile, overwrite = true)
                    Log.d("ProjectViewModel", "PCM para preview listo en caché: ${mixedPcmFile.absolutePath}")

                    if(trackPaths.size > 1 && sourceFile.absolutePath != mixedPcmFile.absolutePath) sourceFile.delete()

                } else {
                    Log.d("ProjectViewModel", "Usando PCM de preview existente en caché: ${mixedPcmFile.absolutePath}")
                }


                if (!isActive) return@launch

                _uiState.update { it.copy(isPreviewLoading = false) }
                playPreviewUseCase(mixedPcmFile.absolutePath)
                    .onFailure { error ->
                        Log.e("ProjectViewModel", "Error al iniciar preview desde UseCase", error)
                        withContext(Dispatchers.Main){Toast.makeText(context, "Error al reproducir preview.", Toast.LENGTH_SHORT).show()}
                        resetPlaybackState()
                    }

            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error preparando preview (mezcla/copia)", e)
                withContext(Dispatchers.Main){ Toast.makeText(context, "Error al preparar preview.", Toast.LENGTH_SHORT).show()}
                resetPlaybackState()
            }
        }
    }

    fun stopPlayback() {
        previewMixJob?.cancel()
        previewMixJob = null
        stopPreviewUseCase()
        if (_uiState.value.currentlyPlayingProject != null || _uiState.value.isPreviewLoading) {
            resetPlaybackState()
        }
    }

    private fun resetPlaybackState() {
        // Solo actualiza si realmente hay algo que resetear, evita updates innecesarios
        if(_uiState.value.currentlyPlayingProject != null || _uiState.value.isPreviewLoading){
            Log.d("ProjectViewModel", "Reseteando estado de playback.")
            _uiState.update { it.copy(currentlyPlayingProject = null, isPreviewLoading = false) }
        }
    }
    private fun listenForPreviewCompletion() {
        onPreviewCompletedUseCase()
            .onEach {
                Log.d("ProjectViewModel", "Preview completion event received.")

                withContext(Dispatchers.Main.immediate) {
                    if (_uiState.value.currentlyPlayingProject != null){
                        resetPlaybackState()
                    }
                }
            }
            .launchIn(viewModelScope)
    }




    override fun onCleared() {
        super.onCleared()
        stopPlayback()
        Log.d("ProjectViewModel", "ViewModel cleared.")
    }



}