package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.data.local.model.ProjectFirebaseModel
import com.android.harmoniatpi.di.util.JsonUtils
import com.android.harmoniatpi.domain.cache.HoloJamCache
import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.domain.model.user.User
import com.android.harmoniatpi.domain.model.userPreferences.Post

import com.android.harmoniatpi.domain.usecases.GetProjectByIdUseCase
import com.android.harmoniatpi.domain.usecases.GetProjectsByUserUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.ExportProjectUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.MixTracksUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.OnPreviewCompletedUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.PlayPreviewUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.StopPreviewUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.DeleteFileFromStorageUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.DeleteProjectFromFirestoreUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.GetAllUserFromDBUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.GetFirestoreProjectsByUserUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.GetUnpublishedLocalOriginalsByUserUseCase
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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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
    private val getProjectByIdUseCase: GetProjectByIdUseCase,
    private val getFirestoreProjectsByUserUseCase: GetFirestoreProjectsByUserUseCase,
    private val getUnpublishedLocalOriginalsByUserUseCase: GetUnpublishedLocalOriginalsByUserUseCase,
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
    private val holoJamCache: HoloJamCache,
    private val uploadAudioToStorageUseCase: UploadAudioToStorageUseCase,
    private val upsertProjectInFirestoreUseCase: UpsertProjectInFirestoreUseCase,
    private val deleteProjectFromFirestoreUseCase: DeleteProjectFromFirestoreUseCase,
    private val deleteFileFromStorageUseCase: DeleteFileFromStorageUseCase,
    private val getAllUsersUseCase : GetAllUserFromDBUseCase,
    private val jsonUtils: JsonUtils,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectUiState())
    val uiState = _uiState.asStateFlow()
    private var previewMixJob: Job? = null


    init {

        // 1. Inicia la sincronización en segundo plano.
        //    Esto escucha Firestore y actualiza Room.
        syncFirestoreToRoomInBackground()
        // 2. Carga los proyectos de "Mis Proyectos" DESDE ROOM.
        //    Room es ahora la única fuente de verdad para la UI.
        loadMyProjectsFromRoom()
        // 3. Carga los clones
        loadCollabProjects()
        // 4. Escucha el reproductor
        listenForPreviewCompletion()
        loadAllUsers()

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
                    Log.d("ProjectViewModel", "[Room MyProjects Flow] Recibidos ${myProjectsFromRoom.size} proyectos desde Room.")
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

        viewModelScope.launch(Dispatchers.IO) { // Sincronización en hilo IO
            Log.d("ProjectViewModel", "Iniciando listener de Firestore en background...")

            getFirestoreProjectsByUserUseCase(currentUserId)
                .map { firestoreList ->
                    // Flujo: FirebaseModel -> Entity -> Domain
                    firestoreList.map { firebaseModel ->
                        firebaseModel.toEntity().toDomain(jsonUtils)
                    }
                }
                .catch { e -> Log.e("ProjectViewModel", "[Firestore Sync] Error fatal", e) }
                .collect { firestoreDomainProjects ->
                    // Sincroniza los datos recibidos con Room usando la lógica de FUSIÓN
                    Log.d("ProjectViewModel", "[Firestore Sync] Recibidos ${firestoreDomainProjects.size} proyectos. Sincronizando...")
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
            val allLocalOriginals = getProjectsByUserUseCase(userId).firstOrNull() ?: emptyList()
            val localProjectsMap = allLocalOriginals.associateBy { it.id }
            Log.d(
                "ProjectViewModel",
                "SYNC: Comparando ${firestoreProjects.size} Firestore con ${allLocalOriginals.size} locales."
            )

            // 1. Asegurar que los de Firestore estén en Room y publicados
            firestoreProjects.forEach { firestoreProject ->
                val localMatch = localProjectsMap[firestoreProject.id]

                if (localMatch == null) {
                    // CASO A: No existe localmente.
                    // Lo insertamos tal cual viene de Firestore (sin pistas locales).
                    Log.i("ProjectViewModel", "SYNC: Insertando ${firestoreProject.id} (de Firestore) en Room.")
                    insertProjectInDBUseCase(firestoreProject)
                } else {
                    // CASO B: Existe localmente. ¡FUSIONAMOS!
                    // Mantenemos las pistas locales (urlAudioTracks) de 'localMatch',
                    // pero actualizamos los metadatos desde 'firestoreProject'.
                    Log.i("ProjectViewModel", "SYNC: Fusionando metadatos de ${localMatch.id} desde Firestore.")

                    val updatedProject = localMatch.copy(
                        // --- Metadatos de Firestore ---
                        isPublished = firestoreProject.isPublished,
                        urlCompleteAudio = firestoreProject.urlCompleteAudio,
                        likes = firestoreProject.likes,
                        totalShared = firestoreProject.totalShared,
                        // (Añade cualquier otro campo que Firebase deba controlar)

                        // --- Datos Locales (implícitos en .copy) ---
                        // urlAudioTracks = localMatch.urlAudioTracks (¡SE PRESERVAN!)
                    )
                    insertProjectInDBUseCase(updatedProject)
                }
            }

            // --- 2. Desmarcar locales que ya no están en Firestore ---
            allLocalOriginals.forEach { localProject ->
                if (localProject.isPublished) {
                    val firestoreMatch = firestoreProjects.find { it.id == localProject.id }
                    if (firestoreMatch == null) {
                        // Fue borrado en otro dispositivo
                        Log.i("ProjectViewModel", "SYNC: Desmarcando ${localProject.id} en Room (ya no está en Firestore).")
                        insertProjectInDBUseCase(localProject.copy(isPublished = false, urlCompleteAudio = null))
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
            var projectToDelete: Project? = null
            try {
                // 1. Obtenemos el proyecto ANTES de borrarlo (de Room)
                projectToDelete = getProjectByIdUseCase(id)

                // 2. Lo borramos de Room
                deleteProjectByIdFromDBUseCase(id)
                Log.d("ProjectViewModel", "Proyecto $id borrado de Room.")

                // 3. Si estaba publicado, borrar de Firebase
                if (projectToDelete.isPublished) {
                    Log.d("ProjectViewModel", "Borrando $id de Firebase...")

                    // Borrar de Firestore
                    deleteProjectFromFirestoreUseCase(id).onFailure {
                        Log.e("ProjectViewModel", "Error al borrar $id de Firestore", it)
                        // Opcional: ¿Mostrar error al usuario?
                    }

                    // Borrar MP3 de Storage
                    // (Asumimos que la ruta es "projectId/mix.mp3" basado en tu `publishProject`)
                    val remotePath = "project_audio/${projectToDelete.id}/mix.mp3"
                    deleteFileFromStorageUseCase(remotePath).onFailure {
                        Log.e("ProjectViewModel", "Error al borrar $remotePath de Storage", it)
                    }
                    // TODO: Borrar el Post de Realtime DB
                    // Esto es más complejo porque el Post tiene su propio ID.
                    // Necesitarías un caso de uso que "busque el post por projectId y lo borre".
                    // Por ahora, esto borra el Proyecto y el Audio.
                    Log.i("ProjectViewModel", "Borrado de Firebase para $id completado (excepto Post RTDB).")
                }
                // 3. Verificamos si era un clon nuestro
                if (projectToDelete.originalProjectId != null && projectToDelete.ownerId == currentUserId) {

                    // 4. Si era un clon, buscamos el original
                    val originalProject = try {
                        getProjectByIdUseCase(projectToDelete.originalProjectId)
                    } catch (e: Exception) {
                        null
                    } // El original ya no existe

                    // 5. Si el original existe y nos tiene en su lista, nos quitamos
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
        // TODO: Considerar añadir estado isPublishing a UiState para feedback visual
        // _uiState.update { it.copy(isPublishing = true, currentlyPublishingId = project.id) }

        viewModelScope.launch {
            var mixedMp3File: File? = null
            var individualMp3Files: List<File> = emptyList()
            var finalAudioUrl: String? = null
            val finalTrackUrls = mutableListOf<String>()
            // Usaremos 'projectDataToPublish' para asegurar que usamos datos consistentes
            var projectDataToPublish: Project? = null

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Preparando publicación...",
                    Toast.LENGTH_SHORT
                ).show()
            }

            try {
                // Exportar MP3 localmente ---
                Log.d("ProjectViewModel", "Iniciando generación de MP3 para ${project.id}...")
                // Carga los datos más frescos del proyecto desde la DB local ANTES de hacer nada más
                projectDataToPublish = getProjectByIdFromDBUseCase(project.id)
                val trackPaths = projectDataToPublish.urlAudioTracks.map { it.path }
                if (trackPaths.isEmpty()) throw IOException("El proyecto no tiene audio para publicar.")

                val exportResult = exportProjectUseCase(project.id, trackPaths).getOrThrow()
                mixedMp3File = exportResult.mixedMp3
                individualMp3Files = exportResult.individualMp3s
                Log.i("ProjectViewModel", "MP3 generados localmente.")

                val mainMp3ToUse = mixedMp3File ?: individualMp3Files.firstOrNull()
                if (mainMp3ToUse == null) throw IOException("No se pudo generar el archivo MP3 principal.")

                // Subir MP3 a Firebase Storage ---
                Log.d("ProjectViewModel", "Subiendo MP3 principal a Storage...")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Subiendo audio...",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                finalAudioUrl =
                    uploadAudioToStorageUseCase(project.id, mainMp3ToUse, "mix.mp3").getOrThrow()
                Log.i("ProjectViewModel", "Audio principal subido. URL: $finalAudioUrl")

                // Subir tracks individuales
                coroutineScope { /* ... (tu código para subir tracks individuales) */ }
                Log.d("ProjectViewModel", "Subida de tracks individuales completada (si aplica).")

                // Guardar datos del Proyecto en Firestore ---
                Log.d("ProjectViewModel", "Guardando datos del proyecto en Firestore...")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Guardando información...",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // Convierte Project (Dominio) -> ProjectEntity (Room)
                val projectEntity = projectDataToPublish.toDataBase(jsonUtils)

                // Convierte ProjectEntity (Room) -> ProjectFirebaseModel (Firebase)
                var projectFirebaseModel = projectEntity.toFirebaseModel()

                // Sobrescribe las URLs en el FirebaseModel con las URLs finales de Storage
                //    (Porque las de 'projectEntity' son probablemente paths locales)
                projectFirebaseModel = projectFirebaseModel.copy(
                    publishedAudioUrl = finalAudioUrl, // URL principal de Storage
                    // Codifica la lista de URLs de tracks a JSON String
                    publishedTrackUrls = jsonUtils.encodeToJson(finalTrackUrls)
                )
                upsertProjectInFirestoreUseCase(projectFirebaseModel).getOrThrow() // Guarda en Firestore
                Log.i("ProjectViewModel", "Datos del proyecto guardados en Firestore.")
                // Marcar Proyecto como publicado LOCALMENTE (¡Y guardar URL!) ---
                // Usa 'projectDataToPublish' como base para marcarlo como publicado
                Log.d("ProjectViewModel", "Actualizando Room localmente con URL: $finalAudioUrl")
                val finalPublishedProjectLocal = projectDataToPublish.copy(
                    isPublished = true,
                    urlCompleteAudio = finalAudioUrl
                )
                // Guarda la versión local fusionada (con pistas Y la URL de la nube)
                withContext(Dispatchers.IO){ insertProjectInDBUseCase(finalPublishedProjectLocal)}
                Log.d("ProjectViewModel", "Proyecto ${project.id} marcado como publicado localmente.")

                // Crear Post en Realtime Database ---
                Log.d("ProjectViewModel", "Creando Post en Realtime DB...")
                val post = Post(
                    id = System.currentTimeMillis()
                        .toString(), // Genera ID aquí o usa uno basado en el proyecto
                    userID = projectDataToPublish.ownerId,
                    userImagePathURL = sharedMenuUiState.uiState.value.userPhotoPathRemote,
                    title = projectDataToPublish.title,
                    description = projectDataToPublish.description,
                    name = projectDataToPublish.name,
                    lasName = projectDataToPublish.lastName,
                    hashtags = projectDataToPublish.hashtags,
                    idProject = projectDataToPublish.id, // ID del proyecto original
                    urlCompleteAudio = finalAudioUrl ?: "", // URL principal de Storage
                    urlAudioTracks = finalTrackUrls.toList(), // Lista de URLs de tracks de Storage
                    imageUrl = "", // Añade si tienes imagen de portada
                    createdAt = LocalDateTime.now().toString(), // Fecha de publicación del post
                    likes = 0,
                    totalShared = 0, // Inicia en 0 clones
                    comments = emptyList(),
                    clonedOption = true // Permite clonar
                )
                insertNewPostFirebaseDataBaseUseCase(post)
                Log.i("ProjectViewModel", "Post creado en Firebase Realtime DB para ${project.id}")

                // Éxito
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "¡Proyecto publicado!", Toast.LENGTH_LONG).show()
                    // Refrescar la lista local si es necesario (aunque el Flow debería hacerlo)
                }

            } catch (e: Exception) {
                // --- Manejo de Errores ---
                Log.e("ProjectViewModel", "Error publicando proyecto ${project.id}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Error al publicar: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                // Intenta revertir el estado local si falló
                try {
                    // Usa 'projectDataToPublish' si no es null, si no, usa el 'project' original
                    val projectToRevert = projectDataToPublish ?: project
                    if (projectToRevert.isPublished) {
                        insertProjectInDBUseCase(projectToRevert.copy(isPublished = false))
                    }
                } catch (_: Exception) {
                }

            } finally {
                // --- Limpieza ---
                withContext(Dispatchers.IO) {
                    mixedMp3File?.delete()
                    individualMp3Files.forEach { it.delete() }
                    Log.d(
                        "ProjectViewModel",
                        "Archivos MP3 locales temporales eliminados (si existían)."
                    )
                }
                // TODO: Quitar estado de carga específico para publicación
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

                if (!mixedPcmFile.exists() || trackPaths.size > 1) {
                    Log.d("ProjectViewModel", "Mezclando/Copiando para preview de ${project.id}...")
                    val sourceFile = if (trackPaths.size > 1) {
                        mixTracksUseCase(project.id, trackPaths)
                    } else {
                        File(trackPaths.first())
                    }

                    if (sourceFile == null || !sourceFile.exists()) {
                        throw IOException("Fallo al obtener archivo fuente para preview.")
                    }
                    sourceFile.copyTo(mixedPcmFile, overwrite = true)
                    Log.d(
                        "ProjectViewModel",
                        "PCM para preview listo en caché: ${mixedPcmFile.absolutePath}"
                    )

                    if (trackPaths.size > 1 && sourceFile.absolutePath != mixedPcmFile.absolutePath) sourceFile.delete()

                } else {
                    Log.d(
                        "ProjectViewModel",
                        "Usando PCM de preview existente en caché: ${mixedPcmFile.absolutePath}"
                    )
                }


                if (!isActive) return@launch

                _uiState.update { it.copy(isPreviewLoading = false) }
                playPreviewUseCase(mixedPcmFile.absolutePath)
                    .onFailure { error ->
                        Log.e("ProjectViewModel", "Error al iniciar preview desde UseCase", error)
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
                Log.e("ProjectViewModel", "Error preparando preview (mezcla/copia)", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Error al preparar preview.",
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
        // Solo actualiza si realmente hay algo que resetear, evita updates innecesarios
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

    private fun loadAllUsers() {
        viewModelScope.launch {
            getAllUsersUseCase().collect { usersList ->
                _uiState.update { it.copy(allUsers = usersList) }
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
}