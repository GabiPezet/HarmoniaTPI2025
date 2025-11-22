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
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectUiState())
    val uiState = _uiState.asStateFlow()
    private var previewMixJob: Job? = null


    init {

        // 1. Inicia la sincronización en segundo plano.
        //    Esto escucha Firestore y actualiza Room.
        syncFirestoreToRoomInBackground()
        //OBSERVADOR DE USUARIOS ---
        observeProjectsAndFetchUsers()
        // 2. Carga los proyectos de "Mis Proyectos" DESDE ROOM.
        //    Room es ahora la única fuente de verdad para la UI.
        loadMyProjectsFromRoom()
        // 3. Carga los clones
        loadCollabProjects()
        // 4. Escucha el reproductor
        listenForPreviewCompletion()
        observeAllUsersFromRoom()

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

        viewModelScope.launch(Dispatchers.IO) { // Sincronización en hilo IO
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
                    }

                    // Borrar MP3 de Storage
                    val remotePath = "project_audio/${projectToDelete.id}/mix.mp3"
                    deleteFileFromStorageUseCase(remotePath).onFailure {
                        Log.e("ProjectViewModel", "Error al borrar $remotePath de Storage", it)
                    }
                    // TODO: Borrar el Post de Realtime DB
                    // Esto es más complejo porque el Post tiene su propio ID.
                    // Necesitarías un caso de uso que "busque el post por projectId y lo borre".
                    // Por ahora, esto borra el Proyecto y el Audio.
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

        _uiState.update { it.copy(isPublishing = true) }
        // TODO: Considerar añadir estado isPublishing a UiState para feedback visual
        // _uiState.update { it.copy(isPublishing = true, currentlyPublishingId = project.id) }

        viewModelScope.launch {
            // --- 1. PREPARAR EL PROYECTO ACTUALIZADO ---
            // Creamos el objeto 'updatedProject' con los datos del diálogo
            val updatedProject = project.copy(
                // OJO: El 'postTitle' es solo para el post.
                // El 'project.title' no lo cambiamos aquí.
                description = postDescription,
                hashtags = postHashtags.split(",").map { it.trim() },
                imageUrl = postImageUrl,
            )
            // --- 2. GUARDAR CAMBIOS EN ROOM ---
            try {
                // Guardamos el proyecto actualizado en la base de datos local
                insertProjectInDBUseCase(updatedProject)
                Log.d("ProjectViewModel", "Proyecto ${project.id} actualizado en Room antes de publicar.")
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error al guardar cambios antes de publicar", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al guardar cambios: ${e.message}", Toast.LENGTH_LONG).show()
                }
                _uiState.update { it.copy(isPublishing = false) }
                onComplete()
                return@launch
            }

            // --- 3. CONTINUAR CON LA LÓGICA DE PUBLICACIÓN ---
            var mixedMp3File: File? = null
            var individualMp3Files: List<File> = emptyList()
            var finalAudioUrl: String? = null
            val finalTrackUrls = mutableListOf<String>()
            var finalImageUrl: String?

            // 'projectDataToPublish' ahora leerá el proyecto actualizado que acabamos de guardar
            var projectDataToPublish: Project? = null

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Preparando publicación...",
                    Toast.LENGTH_SHORT
                ).show()
            }

            try {
                // --- 4. EXPORTAR AUDIO Y SUBIR IMAGEN ---
                //Exportar  (Mix + Pistas) ---
                Log.d("ProjectViewModel", "Iniciando exportación de MP3 para ${project.id}...")
                projectDataToPublish = getProjectByIdFromDBUseCase(project.id)

                // Lógica de subida de imagen usará la URL que acabamos de guardar
                finalImageUrl = projectDataToPublish.imageUrl

                val localImageUriString = projectDataToPublish.imageUrl

                if (localImageUriString != null &&
                    (localImageUriString.startsWith("content://") || localImageUriString.startsWith(
                        "android.resource://"
                    ))
                ) {
                    Log.d("ProjectViewModel", "Imagen local detectada. Subiendo a Storage...")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Subiendo imagen...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    // Copia la URI a un archivo temporal
                    val tempImageFile = File(context.cacheDir, "temp_cover_${project.id}.jpg")
                    context.contentResolver.openInputStream(Uri.parse(localImageUriString))
                        ?.use { input ->
                            tempImageFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    // Sube el archivo temporal (Reutilizo UseCase de audio, ¡aunque el nombre no sea ideal!)
                    val remotePath = "project_images/${project.id}/cover.jpg"
                    finalImageUrl = uploadAudioToStorageUseCase(
                        project.id,
                        tempImageFile,
                        remotePath
                    ).getOrThrow()

                    Log.i("ProjectViewModel", "Imagen subida. URL: $finalImageUrl")

                    // Borra el archivo temporal
                    tempImageFile.delete()
                }

                //Lógica de exportación de audio
                Log.d("ProjectViewModel", "Iniciando exportación de pistas para ${project.id}...")
                val tracksToExport = projectDataToPublish.urlAudioTracks
                if (tracksToExport.isEmpty()) throw IOException("El proyecto no tiene audio para publicar.")

                // **Pasa la lista de AudioTrack a exportProjectUseCase**
                val exportResult = exportProjectUseCase(project.id, tracksToExport).getOrThrow()

                val mainMp3ToUse = exportResult.mixedMp3
                if (mainMp3ToUse == null) throw IOException("No se pudo generar el archivo MP3 principal.")

                // Subir Mix Principal ---
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
                Log.d("ProjectViewModel", "Subiendo ${exportResult.individualMp3s.size} pistas individuales...")
                val uploadedTracks = mutableListOf<AudioTrack>()

                // Itera sobre los MP3 individuales que generó el UseCase
                exportResult.individualMp3s.forEach { mp3File ->
                    // Extrae el ID del track desde el nombre del archivo
                    // (Asumiendo formato: "${projectId}_track_${trackId}.mp3")
                    val trackId = mp3File.name.substringAfterLast("_").substringBefore(".mp3").toLongOrNull()
                    val originalAudioTrack = tracksToExport.find { it.id == trackId }

                    if (originalAudioTrack != null) {
                        val remotePath = "project_audio/${project.id}/track_${originalAudioTrack.id}.mp3"
                        // Sube el archivo MP3 ya convertido
                        val downloadUrl = uploadAudioToStorageUseCase(
                            project.id,
                            mp3File,
                            remotePath
                        ).getOrThrow()

                        // Añade a la lista con la URL remota
                        uploadedTracks.add(
                            originalAudioTrack.copy(remoteUrl = downloadUrl)
                        )
                    } else {
                        Log.w(
                            "ProjectViewModel",
                            "No se pudo encontrar el AudioTrack original para ${mp3File.name}"
                        )
                    }
                }

                // Guardar datos del Proyecto en Firestore ---
                Log.d("ProjectViewModel", "Guardando datos del proyecto en Firestore...")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Guardando información...",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                val finalProjectData = projectDataToPublish.copy(urlAudioTracks = uploadedTracks)
                val projectEntity = finalProjectData.toDataBase(jsonUtils)
                var projectFirebaseModel = projectEntity.toFirebaseModel()

                projectFirebaseModel = projectFirebaseModel.copy(
                    publishedAudioUrl = finalAudioUrl,
                    publishedTrackUrls = jsonUtils.encodeToJson(uploadedTracks), // <-- Guarda el JSON de AudioTrack[]
                    imageUrl = finalImageUrl,
                    isPublished = true
                )
                upsertProjectInFirestoreUseCase(projectFirebaseModel).getOrThrow()
                Log.i("ProjectViewModel", "Datos del proyecto guardados en Firestore.")
                // Marcar Proyecto como publicado LOCALMENTE (¡Y guardar URL!) ---
                // Usa 'projectDataToPublish' como base para marcarlo como publicado
                Log.d("ProjectViewModel", "Actualizando Room localmente con URL: $finalAudioUrl")
                Log.d("ProjectViewModel", "Actualizando Room localmente...")
                val finalPublishedProjectLocal = projectDataToPublish.copy(
                    isPublished = true,
                    urlCompleteAudio = finalAudioUrl,
                    urlAudioTracks = uploadedTracks,
                    imageUrl = finalImageUrl
                )
                withContext(Dispatchers.IO){ insertProjectInDBUseCase(finalPublishedProjectLocal)}
                Log.d("ProjectViewModel", "Proyecto ${project.id} marcado como publicado localmente.")

                // Crear Post en Realtime Database ---
                Log.d("ProjectViewModel", "Creando Post en Realtime DB...")
                val post = Post(
                    id = System.currentTimeMillis()
                        .toString(),
                    userID = projectDataToPublish.ownerId,
                    userImagePathURL = sharedMenuUiState.uiState.value.userPhotoPathRemote,
                    title = postTitle,
                    description = projectDataToPublish.description,
                    name = projectDataToPublish.name,
                    lasName = projectDataToPublish.lastName,
                    hashtags = projectDataToPublish.hashtags,//hashtags = postHashtags.split(",").map { it.trim() },
                    idProject = projectDataToPublish.id,
                    urlCompleteAudio = finalAudioUrl,
                    urlAudioTracks = finalTrackUrls.toList(),
                    imageUrl = finalImageUrl ?: "",
                    createdAt = LocalDateTime.now().toString(),
                    likes = 0,
                    totalShared = 0,
                    comments = emptyList(),
                    clonedOption = true,
                    cloningAccess = cloningAccess
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
                _uiState.update { it.copy(isPublishing = false) }
                onComplete()
                Log.d("ProjectViewModel", "Lógica de publicación finalizada.")
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
        viewModelScope.launch(Dispatchers.IO) { // Hilo IO para red/base de datos

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
                    //    y guardarlos en Room.
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
        projectToPublish: Project,
        postTitle: String,
        postDescription: String, // Este será el mensaje completo (atribución + personal)
        postHashtags: String,
        postImageUrl: String?,
        onComplete: () -> Unit
    ) {
        // Esta función es casi idéntica a `publishProject`,
        // pero está pensada para CLONES.

        viewModelScope.launch {
            // --- 1. GUARDAR CAMBIOS EN ROOM PRIMERO ---
            // Creamos un 'updatedProject' con los datos del diálogo
            val updatedProject = projectToPublish.copy(
                // El título del post ('postTitle') no cambia el título del proyecto ('project.title')
                // La descripción del post ('postDescription') no cambia la descripción del proyecto
                hashtags = postHashtags.split(",").map { it.trim() },
                imageUrl = postImageUrl
            )

            try {
                // Guardamos el clon actualizado en la base de datos local
                insertProjectInDBUseCase(updatedProject)
                Log.d("ProjectViewModel", "Clon ${projectToPublish.id} actualizado en Room antes de publicar.")
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error al guardar clon antes de publicar", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al guardar cambios: ${e.message}", Toast.LENGTH_LONG).show()
                }
                onComplete()
                return@launch
            }

            // --- 2. LÓGICA DE PUBLICACIÓN ---
            var mixedMp3File: File? = null
            var finalAudioUrl: String? = null
            var finalImageUrl: String?

            // Usamos el 'updatedProject' que acabamos de guardar
            var projectDataToPublish: Project? = updatedProject

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Publicando versión...", Toast.LENGTH_SHORT).show()
            }

            try {
                // Carga Datos Actualizados y sube imagen
                Log.d("ProjectViewModel", "Exportando clon ${projectDataToPublish!!.id}...")

                // Busca la versión más fresca del proyecto en la DB (la que acabamos de guardar)
                val refreshedProject = getProjectByIdFromDBUseCase(projectDataToPublish.id)
                val tracksToExport = refreshedProject.urlAudioTracks

                //Inicio de subida de imagen
                val localImageUriString = refreshedProject.imageUrl
                finalImageUrl = localImageUriString

                if (localImageUriString != null &&
                    (localImageUriString.startsWith("content://") || localImageUriString.startsWith("android.resource://"))
                ) {
                    Log.d("ProjectViewModel", "Imagen local (clon) detectada. Subiendo a Storage...")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Subiendo imagen...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // Copia la URI a un archivo temporal
                    val tempImageFile = File(context.cacheDir, "temp_cover_${updatedProject.id}.jpg")
                    context.contentResolver.openInputStream(Uri.parse(localImageUriString))?.use { input ->
                        tempImageFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    // Sube el archivo temporal
                    val remotePath = "project_images/${updatedProject.id}/cover.jpg"
                    finalImageUrl =
                        uploadAudioToStorageUseCase(updatedProject.id, tempImageFile, remotePath)
                            .getOrThrow() // Re-asigna la variable

                    Log.i("ProjectViewModel", "Imagen (clon) subida. URL: $finalImageUrl")

                    //  Borra el archivo temporal
                    tempImageFile.delete()
                }

                //Exportación de MP3
                if (tracksToExport.isEmpty()) throw IOException("El proyecto no tiene audio para publicar.")

                // Llama al UseCase que ya hace la conversión PCM -> MP3
                val exportResult = exportProjectUseCase(projectDataToPublish.id, tracksToExport).getOrThrow()
                mixedMp3File = exportResult.mixedMp3
                val mainMp3ToUse = mixedMp3File ?: exportResult.individualMp3s.firstOrNull()
                if (mainMp3ToUse == null) throw IOException("No se pudo generar el archivo MP3 principal.")

                // Subir Mix Principal a Firebase Storage ---
                withContext(Dispatchers.Main) { Toast.makeText(context, "Subiendo audio...", Toast.LENGTH_SHORT).show() }
                finalAudioUrl = uploadAudioToStorageUseCase(projectDataToPublish.id, mainMp3ToUse, "mix.mp3").getOrThrow()

                // Subir Pistas Individuales a Firebase Storage ---
                Log.d("ProjectViewModel", "Subiendo ${exportResult.individualMp3s.size} pistas individuales (clon)...")
                val uploadedTracks = mutableListOf<AudioTrack>()

                exportResult.individualMp3s.forEach { mp3File ->
                    // Extrae el ID del track desde el nombre del archivo
                    val trackId =
                        mp3File.name.substringAfterLast("_").substringBefore(".mp3").toLongOrNull()
                    val originalAudioTrack = tracksToExport.find { it.id == trackId }

                    if (originalAudioTrack != null) {
                        val remotePath =
                            "project_audio/${projectDataToPublish.id}/track_${originalAudioTrack.id}.mp3"
                        val downloadUrl = uploadAudioToStorageUseCase(
                            projectDataToPublish.id,
                            mp3File,
                            remotePath
                        ).getOrThrow()

                        // Añade a la lista el AudioTrack actualizado con la URL remota
                        uploadedTracks.add(
                            originalAudioTrack.copy(remoteUrl = downloadUrl)
                        )
                    } else {
                        Log.w(
                            "ProjectViewModel",
                            "No se pudo encontrar el AudioTrack original para ${mp3File.name}"
                        )
                    }
                }

                // Prepara los datos finales (con la lista de pistas actualizada)
                val finalProjectData = refreshedProject.copy(urlAudioTracks = uploadedTracks)

                // Guardar datos del Proyecto en Firestore ---
                val projectEntity = finalProjectData.toDataBase(jsonUtils)
                var projectFirebaseModel = projectEntity.toFirebaseModel()

                projectFirebaseModel = projectFirebaseModel.copy(
                    publishedAudioUrl = finalAudioUrl,
                    imageUrl = finalImageUrl,
                    publishedTrackUrls = jsonUtils.encodeToJson(uploadedTracks), // <-- Guarda el JSON de AudioTrack[]
                    originalProjectId = projectDataToPublish.originalProjectId,
                    isPublished = true
                )
                upsertProjectInFirestoreUseCase(projectFirebaseModel).getOrThrow()

                // Marcar Proyecto como publicado LOCALMENTE ---
                val finalPublishedProjectLocal = finalProjectData.copy(
                    isPublished = true,
                    urlCompleteAudio = finalAudioUrl,
                    imageUrl = finalImageUrl
                )
                withContext(Dispatchers.IO) { insertProjectInDBUseCase(finalPublishedProjectLocal) }

                // Crear Post en Realtime Database (LA PARTE CLAVE) ---
                Log.d("ProjectViewModel", "Creando Post para el CLON...")

                // Crea la lista de URLs de pistas remotas para el Post
                val remoteTrackUrls = uploadedTracks.mapNotNull { it.remoteUrl }

                val post = Post(
                    id = System.currentTimeMillis().toString(),
                    userID = projectDataToPublish.ownerId,
                    userImagePathURL = sharedMenuUiState.uiState.value.userPhotoPathRemote,

                    // --- DATOS PERSONALIZADOS DEL DIÁLOGO ---
                    title = postTitle,
                    description = postDescription,

                    name = projectDataToPublish.name,
                    lasName = projectDataToPublish.lastName,
                    hashtags = projectDataToPublish.hashtags,

                    // --- ASOCIACIÓN ---
                    idProject = projectDataToPublish.id,

                    urlCompleteAudio = finalAudioUrl,
                    urlAudioTracks = remoteTrackUrls,
                    imageUrl = finalImageUrl ?: "",
                    createdAt = LocalDateTime.now().toString(),
                    likes = 0,
                    totalShared = 0,
                    comments = emptyList(),
                    clonedOption = false
                )
                insertNewPostFirebaseDataBaseUseCase(post)
                Log.i(
                    "ProjectViewModel",
                    "Post creado en Firebase Realtime DB para el clon ${projectDataToPublish.id}"
                )

                // Actualizar el Proyecto Original (local y remoto) ---
                Log.d("ProjectViewModel", "Actualizando el proyecto original localmente...")
                try {
                    val originalId = projectDataToPublish.originalProjectId
                    if (originalId != null) {
                        val originalProject = getProjectByIdUseCase(originalId)
                        val clonerUserId = projectDataToPublish.ownerId

                        if (!originalProject.forkedByUserIds.contains(clonerUserId)) {
                            val updatedForkedIds = originalProject.forkedByUserIds + clonerUserId
                            val updatedOriginal = originalProject.copy(
                                forkedByUserIds = updatedForkedIds
                            )
                            // Actualiza el original en Room
                            insertProjectInDBUseCase(updatedOriginal)
                            Log.i(
                                "ProjectViewModel",
                                "Proyecto original ${originalProject.id} actualizado con el fork de $clonerUserId"
                            )

                            // Actualiza el original en Firestore
                            try {
                                val originalFirebaseModel =
                                    updatedOriginal.toDataBase(jsonUtils).toFirebaseModel()
                                        .copy(isPublished = true) // Asegura que siga publicado

                                upsertProjectInFirestoreUseCase(originalFirebaseModel)
                                Log.i(
                                    "ProjectViewModel",
                                    "Proyecto original ${originalProject.id} actualizado en Firestore."
                                )
                            } catch (e: Exception) {
                                Log.e(
                                    "ProjectViewModel",
                                    "Fallo al actualizar el original en Firestore",
                                    e
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(
                        "ProjectViewModel",
                        "Fallo al actualizar el proyecto original, pero la publicación del clon fue exitosa.",
                        e
                    )
                }

                // ---  Éxito ---
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "¡Versión publicada!", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                // --- Manejo de Errores ---
                Log.e("ProjectViewModel", "Error publicando clon ${projectToPublish.id}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Error al publicar: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                // --- Limpieza ---
                // No borramos los archivos MP3, ya que ExportProjectUseCase los guarda
                // en una carpeta pública ("exported_music")
                onComplete()
            }
        }
    }

    fun onImageSelected(uriString: String?) {
        _uiState.update { it.copy(selectedImageUri = uriString) }
    }
}