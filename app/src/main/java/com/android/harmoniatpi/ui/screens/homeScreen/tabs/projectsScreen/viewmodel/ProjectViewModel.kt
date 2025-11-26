package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.di.util.JsonUtils
import com.android.harmoniatpi.domain.cache.HoloJamCache
import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.project.AudioTrack
import com.android.harmoniatpi.domain.model.project.CloningAccess
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.domain.model.userPreferences.Post
import com.android.harmoniatpi.domain.usecases.GetProjectByIdUseCase
import com.android.harmoniatpi.domain.usecases.GetProjectsByUserUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.ExportProjectUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.MixTracksUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.OnPreviewCompletedUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.PlayPreviewUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.StopPreviewUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.DeleteFileFromStorageUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.DeletePostByProjectIdUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.DeleteProjectFromFirestoreUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.FetchAndSyncUsersUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.GetAllUserFromDBUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.GetFirestoreProjectsByUserUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.GetProjectByIdFromFirestoreUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.GetUserOnFirebaseByIDUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.InsertNewPostFirebaseDataBaseUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.UploadAudioToStorageUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.UpsertProjectInFirestoreUseCase
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getAllProjectsFromDBUseCase: GetAllProjectsFromDBUseCase,
    private val getProjectsByUserUseCase: GetProjectsByUserUseCase,
    internal val getProjectByIdUseCase: GetProjectByIdUseCase,
    private val getFirestoreProjectsByUserUseCase: GetFirestoreProjectsByUserUseCase,
    private val insertProjectInDBUseCase: UpdateOrInsertProjectInDBUseCase,
    private val deleteProjectByIdFromDBUseCase: DeleteProjectByIdFromDBUseCase,
    private val insertNewPostFirebaseDataBaseUseCase: InsertNewPostFirebaseDataBaseUseCase,
    private val exportProjectUseCase: ExportProjectUseCase,
    private val getProjectByIdFromDBUseCase: GetProjectByIdFromDBUseCase,
    private val playPreviewUseCase: PlayPreviewUseCase,
    private val stopPreviewUseCase: StopPreviewUseCase,
    private val onPreviewCompletedUseCase: OnPreviewCompletedUseCase,
    private val mixTracksUseCase: MixTracksUseCase,
    internal val sharedMenuUiState: SharedMenuUiState,
    private val holoJamCache: HoloJamCache,
    private val uploadAudioToStorageUseCase: UploadAudioToStorageUseCase,
    private val upsertProjectInFirestoreUseCase: UpsertProjectInFirestoreUseCase,
    private val deleteProjectFromFirestoreUseCase: DeleteProjectFromFirestoreUseCase,
    private val deleteFileFromStorageUseCase: DeleteFileFromStorageUseCase,
    private val getAllUsersUseCase: GetAllUserFromDBUseCase,
    private val fetchAndSyncUsersUseCase: FetchAndSyncUsersUseCase,
    private val jsonUtils: JsonUtils,
    internal val getProjectByIdFromFirestoreUseCase: GetProjectByIdFromFirestoreUseCase,
    internal val getUserOnFirebaseByIDUseCase: GetUserOnFirebaseByIDUseCase,
    private val deletePostByProjectIdUseCase: DeletePostByProjectIdUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectUiState())
    val uiState = _uiState.asStateFlow()
    private var previewMixJob: Job? = null


    init {
        syncFirestoreToRoomInBackground()
        //OBSERVADOR DE USUARIOS
        observeProjectsAndFetchUsers()
        //Carga los proyectos de "Mis Proyectos" DESDE ROOM.
        loadMyProjectsFromRoom()
        //Carga los clones
        loadCollabProjects()

        listenForPreviewCompletion()
        observeAllUsersFromRoom()

    }

    //Cargar todos los proyectos de la base de datos
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


    // Carga "Mis Proyectos" a la UI, leyendo ÚNICAMENTE desde Room.
    private fun loadMyProjectsFromRoom() {
        val currentUserId = sharedMenuUiState.uiState.value.userID
        if (currentUserId.isBlank()) {
            _uiState.update { it.copy(myProjects = emptyList()) }
            return
        }

        viewModelScope.launch {
            // getProjectsByUserUseCase -> DAO.getAllProjectsByUser (solo originales)
            // Esto mostrará publicados y no publicados, todos desde Room.
            getProjectsByUserUseCase(currentUserId)
                .catch { e ->
                    Log.e("ProjectViewModel", "[Room MyProjects Flow] Error", e)
                    emit(emptyList())
                }
                .collect { myProjectsFromRoom ->
                    Log.d(
                        "ProjectViewModel",
                        "[Room MyProjects Flow] Recibidos ${myProjectsFromRoom.size} proyectos desde Room."
                    )
                    _uiState.update {
                        it.copy(myProjects = myProjectsFromRoom.sortedByDescending { p -> p.createdAt })
                    }
                }
        }
    }

    // Trae Los proyectos de firestore y los sincroniza con room
    private fun syncFirestoreToRoomInBackground() {
        val currentUserId = sharedMenuUiState.uiState.value.userID
        if (currentUserId.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            Log.d("ProjectViewModel", "Iniciando listener de Firestore en background...")

            getFirestoreProjectsByUserUseCase(currentUserId)
                .map { firestoreList ->
                    firestoreList.map { firebaseModel ->
                        firebaseModel.toEntity().toDomain(jsonUtils)
                    }
                }
                .catch { e -> Log.e("ProjectViewModel", "[Firestore Sync] Error fatal", e) }
                .collect { firestoreDomainProjects ->
                    // Sincroniza los datos recibidos con Room usando la lógica de FUSIÓN
                    Log.d(
                        "ProjectViewModel",
                        "[Firestore Sync] Recibidos ${firestoreDomainProjects.size} proyectos. Sincronizando..."
                    )
                    synchronizeFirestoreToRoom(currentUserId, firestoreDomainProjects)
                }
        }
    }

    private suspend fun synchronizeFirestoreToRoom(
        userId: String,
        firestoreProjects: List<Project>,
    ) {
        Log.d("ProjectViewModel", "SYNC: Iniciando Firestore -> Room...")
        try {
            // Obtiene TODOS los originales locales (pub y no pub) para comparar
            val allLocalProjects = getAllProjectsFromDBUseCase().firstOrNull() ?: emptyList()
            val localProjectsMap = allLocalProjects.associateBy { it.id }
            Log.d(
                "ProjectViewModel",
                "SYNC: Comparando ${firestoreProjects.size} Firestore con ${allLocalProjects.size} locales."
            )

            //Asegurar que los de Firestore estén en Room y publicados
            firestoreProjects.forEach { firestoreProject ->
                val localMatch = localProjectsMap[firestoreProject.id]

                if (localMatch == null) {
                    // Lo insertamos tal cual viene de Firestore (sin pistas locales).
                    Log.i(
                        "ProjectViewModel",
                        "SYNC: Insertando ${firestoreProject.id} (de Firestore) en Room."
                    )
                    insertProjectInDBUseCase(firestoreProject)
                } else {
                    // Mantenemos las pistas locales (urlAudioTracks) de 'localMatch',
                    // pero actualizamos los metadatos desde 'firestoreProject'.
                    Log.i(
                        "ProjectViewModel",
                        "SYNC: Fusionando metadatos de ${localMatch.id} desde Firestore."
                    )

                    val updatedProject = localMatch.copy(
                        isPublished = firestoreProject.isPublished,
                        urlCompleteAudio = firestoreProject.urlCompleteAudio,
                        likes = firestoreProject.likes,
                        totalShared = firestoreProject.totalShared,
                        forkedByUserIds = firestoreProject.forkedByUserIds
                    )
                    insertProjectInDBUseCase(updatedProject)
                }
            }

            //Desmarcar locales que ya no están en Firestore ---
            val allLocalOriginals = getProjectsByUserUseCase(userId).firstOrNull() ?: emptyList()
            allLocalOriginals.forEach { localProject ->
                if (localProject.isPublished) {
                    val firestoreMatch = firestoreProjects.find { it.id == localProject.id }
                    if (firestoreMatch == null) {
                        // Fue borrado en otro dispositivo
                        Log.i(
                            "ProjectViewModel",
                            "SYNC: Desmarcando ${localProject.id} en Room (ya no está en Firestore)."
                        )
                        insertProjectInDBUseCase(
                            localProject.copy(
                                isPublished = false,
                                urlCompleteAudio = null
                            )
                        )
                    }
                }
            }
            Log.d("ProjectViewModel", "SYNC: Sincronización Firestore -> Room completada.")
        } catch (e: Exception) {
            Log.e("ProjectViewModel", "SYNC: Error fatal durante sincronización", e)
        }
    }

    fun saveProjectEdits(
        projectToSave: Project,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val currentUserId = sharedMenuUiState.uiState.value.userID
        if (currentUserId.isBlank()) {
            onError("Usuario no válido")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

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

    //Borrar un proyecto por ID
    fun deleteProject(id: String) {
        val currentUserId = sharedMenuUiState.uiState.value.userID
        if (currentUserId.isBlank()) return

        viewModelScope.launch {
            var projectToDelete: Project? = null
            try {
                //Obtenemos el proyecto ANTES de borrarlo (de Room)
                projectToDelete = getProjectByIdUseCase(id)

                //Lo borramos de Room
                deleteProjectByIdFromDBUseCase(id)
                Log.d("ProjectViewModel", "Proyecto $id borrado de Room.")

                //Si estaba publicado, borrar de Firebase
                if (projectToDelete.isPublished) {
                    Log.d("ProjectViewModel", "Borrando $id de Firebase...")

                    // Borrar de Firestore
                    deleteProjectFromFirestoreUseCase(id).onFailure {
                        Log.e("ProjectViewModel", "Error al borrar $id de Firestore", it)
                    }

                    // Borrar MP3 de Storage
                    val remotePath = "project_audio/${projectToDelete.id}/mix.mp3"
                    deleteFileFromStorageUseCase(remotePath).onFailure {
                        Log.e("ProjectViewModel", "Error al borrar $remotePath de Storage", it)
                    }
                    // TODO: Borrar el Post de Realtime DB
                    deletePostByProjectIdUseCase(id)
                        .onSuccess { Log.i("ProjectViewModel", "Post de comunidad eliminado.") }
                        .onFailure { Log.e("ProjectViewModel", "Fallo al borrar post de comunidad", it) }

                    Log.i("ProjectViewModel", "Borrado completo de Firebase.")
                    Log.i(
                        "ProjectViewModel",
                        "Borrado de Firebase para $id completado (excepto Post RTDB)."
                    )
                }
                // Verificamos si era un clon nuestro
                if (projectToDelete.originalProjectId != null && projectToDelete.ownerId == currentUserId) {


                    val originalProject = try {
                        getProjectByIdUseCase(projectToDelete.originalProjectId)
                    } catch (e: Exception) {
                        null
                    } // El original ya no existe

                    //Si el original existe y nos tiene en su lista, nos quitamos
                    if (originalProject != null && originalProject.forkedByUserIds.contains(
                            currentUserId
                        )
                    ) {
                        val updatedForkedIds =
                            originalProject.forkedByUserIds.filter { it != currentUserId }
                        val updatedOriginal = originalProject.copy(
                            forkedByUserIds = updatedForkedIds
                        )
                        insertProjectInDBUseCase(updatedOriginal)
                    }
                }
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error al borrar proyecto $id", e)
                if (projectToDelete == null) {
                    Log.e("ProjectViewModel", "El proyecto $id no se encontró en Room para borrar.")
                }
            }
        }
    }


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


    fun saveProject(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
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
                    imageUrl = current.selectedImageUri,
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

                setCurrentProject(project)

                _uiState.update {
                    it.copy(
                        title = "",
                        description = "",
                        hashtags = "",
                        selectedImageUri = null,
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

    fun publishProject(
        project: Project,
        postTitle: String,
        postDescription: String,
        postHashtags: String,
        postImageUrl: String?,
        cloningAccess: CloningAccess,
        onComplete: () -> Unit
    ) {
        if (project.isPublished) {
            Toast.makeText(context, "Este proyecto ya está publicado.", Toast.LENGTH_SHORT).show()
            onComplete()
            return
        }

        // Lanzamos el flujo central configurado para un proyecto ORIGINAL
        executePublishingWorkflow(
            project = project,
            postTitle = postTitle,
            postDescription = postDescription,
            postHashtags = postHashtags,
            postImageUrl = postImageUrl,
            cloningAccess = cloningAccess,
            isClone = false, // <--- Flag importante
            onComplete = onComplete
        )
    }

    //Validación del formulario
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
    }

    fun setCurrentProject(project: Project) {
        stopPlayback()
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

            val tracksToMix = project.urlAudioTracks
            if (tracksToMix.isEmpty()) {
                Log.w("ProjectViewModel", "Project ${project.id} has no tracks for preview.")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "El proyecto no tiene pistas.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                resetPlaybackState()
                return@launch
            }

            val previewMixFileName = "preview_${project.id}_mix.pcm"
            val mixedPcmFile = File(context.cacheDir, previewMixFileName)

            try {


                var needsMixing = true

                if (needsMixing || !mixedPcmFile.exists()) {
                    Log.d("ProjectViewModel", "Mixing tracks for preview of ${project.id}...")

                    val mixedFileResult = mixTracksUseCase(project.id, tracksToMix)

                    val finalMixedFile = mixedFileResult ?: mixedPcmFile
                    if (!finalMixedFile.exists() || finalMixedFile.length() == 0L) {

                        throw IOException("Fallo CRÍTICO al generar o encontrar el archivo PCM mezclado: ${finalMixedFile.absolutePath}")
                    }

                    if (finalMixedFile.absolutePath != mixedPcmFile.absolutePath) {
                        finalMixedFile.copyTo(mixedPcmFile, overwrite = true)

                    }
                    Log.d(
                        "ProjectViewModel",
                        "Archivo de mezcla verificado OK: ${mixedPcmFile.absolutePath}, Tamaño: ${mixedPcmFile.length()}"
                    )


                    if (mixedFileResult == null || !mixedFileResult.exists()) {
                        throw IOException("Failed to generate mixed PCM file for preview.")
                    }





                    Log.d(
                        "ProjectViewModel",
                        "PCM for preview ready in cache: ${mixedPcmFile.absolutePath}"
                    )
                } else {
                    Log.d(
                        "ProjectViewModel",
                        "Using existing preview PCM from cache: ${mixedPcmFile.absolutePath}"
                    )
                }


                if (!isActive) return@launch

                _uiState.update { it.copy(isPreviewLoading = false) }

                playPreviewUseCase(mixedPcmFile.absolutePath)
                    .onFailure { error ->
                        Log.e(
                            "ProjectViewModel",
                            "Error starting preview playback from UseCase",
                            error
                        )
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Error al reproducir preview.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        resetPlaybackState()
                    }

            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error preparing preview (mixing/copying)", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Error al preparar preview: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
        //Solo actualiza si realmente hay algo que resetear, evita updates innecesarios
        if (_uiState.value.currentlyPlayingProject != null || _uiState.value.isPreviewLoading) {
            Log.d("ProjectViewModel", "Reseteando estado de playback.")
            _uiState.update { it.copy(currentlyPlayingProject = null, isPreviewLoading = false) }
        }
    }

    private fun listenForPreviewCompletion() {
        onPreviewCompletedUseCase()
            .onEach {
                Log.d("ProjectViewModel", "Preview completion event received.")

                withContext(Dispatchers.Main.immediate) {
                    if (_uiState.value.currentlyPlayingProject != null) {
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

    private fun observeAllUsersFromRoom() {
        viewModelScope.launch {
            // getAllUsersUseCase() ya devuelve un Flow<List<UserPreferences>>
            // (gracias a cómo arreglamos el DAO de Room antes)
            getAllUsersUseCase().collect { usersListFromRoom ->
                _uiState.update { it.copy(allUsers = usersListFromRoom) }
            }
        }
    }

    private fun observeProjectsAndFetchUsers() {
        viewModelScope.launch(Dispatchers.IO) {

            // Este flow se re-ejecuta cada vez que 'myProjects' cambia (ej. por un sync)
            uiState.map { it.myProjects }.collect { myProjectsList ->

                // Obtiene TODOS los IDs únicos de la lista de 'forkedByUserIds'
                val allForkedUserIds = myProjectsList
                    .flatMap { it.forkedByUserIds }
                    .distinct()

                if (allForkedUserIds.isNotEmpty()) {
                    Log.d(
                        "ProjectViewModel",
                        "IDs de usuarios detectados: $allForkedUserIds. Sincronizando..."
                    )
                    // Llama al UseCase para buscar esos IDs en Firestore
                    fetchAndSyncUsersUseCase(allForkedUserIds)
                        .onFailure { e ->
                            Log.e("ProjectViewModel", "[Users Sync] Error fatal", e)
                        }
                }
            }
        }
    }

    fun buscarporID(
        userID: String
    ): UserPreferences? {
        return uiState.value.allUsers.find {
            it.userID == userID
        }
    }

    fun publishClonedProject(
        project: Project,
        postTitle: String,
        postDescription: String,
        postHashtags: String,
        postImageUrl: String?,
        onComplete: () -> Unit
    ) {
        // Lanzamos el flujo central configurado para un CLON
        executePublishingWorkflow(
            project = project,
            postTitle = postTitle,
            postDescription = postDescription,
            postHashtags = postHashtags,
            postImageUrl = postImageUrl,
            cloningAccess = CloningAccess.PUBLIC,
            isClone = true,
            onComplete = onComplete
        )
    }

    private fun executePublishingWorkflow(
        project: Project,
        postTitle: String,
        postDescription: String,
        postHashtags: String,
        postImageUrl: String?,
        cloningAccess: CloningAccess,
        isClone: Boolean,
        onComplete: () -> Unit
    ) {
        _uiState.update { it.copy(isPublishing = true) }

        viewModelScope.launch {
            //Guardar cambios locales previos (Título, descripción, imagen nueva)
            val updatedProject = project.copy(
                description = if (!isClone) postDescription else project.description,
                hashtags = postHashtags.split(",").map { it.trim() },
                imageUrl = postImageUrl
            )

            try {
                insertProjectInDBUseCase(updatedProject)
            } catch (e: Exception) {
                handlePublishError("Error al guardar cambios locales", e)
                onComplete()
                return@launch
            }

            // Variables para limpieza final
            var mixedMp3File: File? = null
            var individualMp3Files: List<File> = emptyList()

            try {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Preparando publicación...", Toast.LENGTH_SHORT).show()
                }

                //Subir Imagen (si es local)
                val finalImageUrl = uploadImageIfNeeded(updatedProject.id, postImageUrl)

                //Exportar y Subir Audio
                val (finalAudioUrl, uploadedTracks) = exportAndUploadAudio(updatedProject)

                //Guardar Proyecto en Firestore y Local (Actualizar URLs remotas)
                val finalProjectData = updatedProject.copy(
                    isPublished = true,
                    urlCompleteAudio = finalAudioUrl,
                    urlAudioTracks = uploadedTracks,
                    imageUrl = finalImageUrl
                )

                //Firestore
                val projectFirebaseModel = finalProjectData.toDataBase(jsonUtils).toFirebaseModel().copy(
                    publishedAudioUrl = finalAudioUrl,
                    publishedTrackUrls = jsonUtils.encodeToJson(uploadedTracks),
                    imageUrl = finalImageUrl
                )
                upsertProjectInFirestoreUseCase(projectFirebaseModel).getOrThrow()

                //Room (Local)
                insertProjectInDBUseCase(finalProjectData)

                //Crear Post en Realtime Database
                createPostInRealtimeDB(
                    project = finalProjectData,
                    postTitle = postTitle,
                    postDescription = postDescription,
                    postHashtags = finalProjectData.hashtags,
                    finalAudioUrl = finalAudioUrl!!,
                    finalImageUrl = finalImageUrl,
                    isClone = isClone,
                    cloningAccess = cloningAccess
                )

                //Lógica específica si es CLON (Actualizar al original)
                if (isClone && finalProjectData.originalProjectId != null) {
                    updateOriginalProjectAfterFork(
                        originalProjectId = finalProjectData.originalProjectId!!,
                        clonerUserId = finalProjectData.ownerId
                    )
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, if(isClone) "¡Versión publicada!" else "¡Proyecto publicado!", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                handlePublishError("Error al publicar", e)
                //Revertir estado local si falló
                try {
                    insertProjectInDBUseCase(project.copy(isPublished = false))
                } catch (_: Exception) { }
            } finally {
                //Limpieza de archivos temporales (Implementación simplificada)
                //Idealmente exportProjectUseCase debería darte handles a los archivos para borrarlos aquí.
                withContext(Dispatchers.IO) {
                    mixedMp3File?.delete()
                }
                _uiState.update { it.copy(isPublishing = false) }
                onComplete()
            }
        }
    }

    private suspend fun uploadImageIfNeeded(projectId: String, imageUrl: String?): String? {
        if (imageUrl != null && (imageUrl.startsWith("content://") || imageUrl.startsWith("android.resource://"))) {
            withContext(Dispatchers.Main) { Toast.makeText(context, "Subiendo imagen...", Toast.LENGTH_SHORT).show() }

            val tempImageFile = File(context.cacheDir, "temp_cover_${projectId}.jpg")
            return try {
                context.contentResolver.openInputStream(Uri.parse(imageUrl))?.use { input ->
                    tempImageFile.outputStream().use { output -> input.copyTo(output) }
                }
                val remotePath = "project_images/$projectId/cover.jpg"
                uploadAudioToStorageUseCase(projectId, tempImageFile, remotePath).getOrThrow()
            } finally {
                tempImageFile.delete()
            }
        }
        return imageUrl
    }

    private suspend fun exportAndUploadAudio(project: Project): Pair<String?, List<AudioTrack>> {
        withContext(Dispatchers.Main) { Toast.makeText(context, "Procesando audio...", Toast.LENGTH_SHORT).show() }

        val tracksToExport = project.urlAudioTracks
        if (tracksToExport.isEmpty()) throw IOException("El proyecto no tiene audio.")

        //Exportar (Mezcla + Individuales)
        val exportResult = exportProjectUseCase(project.id, tracksToExport).getOrThrow()
        val mainMp3 = exportResult.mixedMp3 ?: exportResult.individualMp3s.firstOrNull()
        ?: throw IOException("Error generando MP3.")

        //Subir Mix
        withContext(Dispatchers.Main) { Toast.makeText(context, "Subiendo audio...", Toast.LENGTH_SHORT).show() }
        val mixUrl = uploadAudioToStorageUseCase(project.id, mainMp3, "mix.mp3").getOrThrow()

        //Subir Pistas Individuales
        val uploadedTracks = mutableListOf<AudioTrack>()
        exportResult.individualMp3s.forEach { mp3File ->
            val trackId = mp3File.name.substringAfterLast("_").substringBefore(".mp3").toLongOrNull()
            val originalTrack = tracksToExport.find { it.id == trackId }

            if (originalTrack != null) {
                val trackUrl = uploadAudioToStorageUseCase(
                    project.id, mp3File, "project_audio/${project.id}/track_${originalTrack.id}.mp3"
                ).getOrThrow()
                uploadedTracks.add(originalTrack.copy(remoteUrl = trackUrl))
            }
            //Borramos el archivo individual temporal inmediatamente tras subirlo para ahorrar espacio
            mp3File.delete()
        }
        //Borramos el mix temporal
        mainMp3.delete()

        return Pair(mixUrl, uploadedTracks)
    }

    private suspend fun createPostInRealtimeDB(
        project: Project,
        postTitle: String,
        postDescription: String,
        postHashtags: List<String>,
        finalAudioUrl: String,
        finalImageUrl: String?,
        isClone: Boolean,
        cloningAccess: CloningAccess
    ) {
        Log.d("ProjectViewModel", "Creando Post en Realtime DB...")
        val post = Post(
            id = System.currentTimeMillis().toString(),
            userID = project.ownerId,
            userImagePathURL = sharedMenuUiState.uiState.value.userPhotoPathRemote,
            title = postTitle,
            description = postDescription,
            name = project.name,
            lasName = project.lastName,
            hashtags = postHashtags,
            idProject = project.id,
            urlCompleteAudio = finalAudioUrl,
            urlAudioTracks = project.urlAudioTracks.mapNotNull { it.remoteUrl },
            imageUrl = finalImageUrl ?: "",
            createdAt = LocalDateTime.now().toString(),
            likes = 0,
            totalShared = 0,
            comments = emptyList(),
            clonedOption = !isClone,
            cloningAccess = cloningAccess
        )
        insertNewPostFirebaseDataBaseUseCase(post)
    }

    private suspend fun updateOriginalProjectAfterFork(originalProjectId: String, clonerUserId: String) {
        try {
            Log.d("ProjectViewModel", "Actualizando proyecto original $originalProjectId...")
            val originalProject = getProjectByIdUseCase(originalProjectId) // De Room o Repositorio

            if (!originalProject.forkedByUserIds.contains(clonerUserId)) {
                val updatedOriginal = originalProject.copy(
                    forkedByUserIds = originalProject.forkedByUserIds + clonerUserId
                )
                //Actualizar Room
                insertProjectInDBUseCase(updatedOriginal)

                //Actualizar Firestore
                val firebaseModel = updatedOriginal.toDataBase(jsonUtils).toFirebaseModel().copy(isPublished = true)
                upsertProjectInFirestoreUseCase(firebaseModel)
                Log.i("ProjectViewModel", "Original actualizado con nuevo fork.")
            }
        } catch (e: Exception) {
            Log.e("ProjectViewModel", "Error actualizando el proyecto original (no crítico)", e)
        }
    }

    private suspend fun handlePublishError(msg: String, e: Exception) {
        Log.e("ProjectViewModel", msg, e)
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "$msg: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun onImageSelected(uriString: String?) {
        _uiState.update { it.copy(selectedImageUri = uriString) }
    }
}