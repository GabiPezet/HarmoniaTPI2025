package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.viewmodel

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.data.local.model.ProjectFirebaseModel
import com.android.harmoniatpi.di.util.JsonUtils
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
import kotlinx.coroutines.flow.onStart
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
    private val jsonUtils: JsonUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectUiState())
    val uiState = _uiState.asStateFlow()
    private var previewMixJob: Job? = null


    init {
        loadMyProjectsCombinedAndSync()
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
    fun loadMyProjectsCombinedAndSync() {
        val currentUserId = sharedMenuUiState.uiState.value.userID
        if (currentUserId.isBlank()) {
            _uiState.update { it.copy(myProjects = emptyList()) }
            return
        }

        viewModelScope.launch {
            Log.d("ProjectViewModel", "Iniciando carga combinada para 'Mis Proyectos'...")

            // --- Flow 1: Proyectos publicados desde Firestore ---
            val firestoreFlow: Flow<List<Project>> = getFirestoreProjectsByUserUseCase(currentUserId)
                .map { firestoreList: List<ProjectFirebaseModel> -> // Tipo explícito
                    Log.d("ProjectViewModel", "[Firestore Flow] Recibidos ${firestoreList.size} proyectos.")
                    // 👇 ***** CORRECCIÓN AQUÍ ***** 👇
                    // El error estaba aquí. Seguimos tu patrón User/Post
                    // Flujo: FirebaseModel -> Entity -> Domain
                    firestoreList.map { firebaseModel ->
                        // 1. Convierte FirebaseModel a Entity (pasa Strings JSON)
                        val entity = firebaseModel.toEntity()
                        // 2. Convierte Entity a Domain (decodifica JSON)
                        entity.toDomain(jsonUtils)
                    }
                }
                .onEach { firestoreDomainProjects ->
                    // --- Sincronización Firestore -> Room ---
                    launch(Dispatchers.IO) {
                        // Pasamos los Project (Domain) a la sincronización
                        synchronizeFirestoreToRoom(currentUserId, firestoreDomainProjects)
                    }
                }
                .catch { e ->
                    Log.e("ProjectViewModel", "[Firestore Flow] Error", e)
                    emit(emptyList())
                }

            // --- Flow 2: Proyectos locales NO publicados ---
            val localUnpublishedFlow: Flow<List<Project>> = getUnpublishedLocalOriginalsByUserUseCase(currentUserId)
                .catch { e ->
                    Log.e("ProjectViewModel", "[Local Unpublished Flow] Error", e)
                    emit(emptyList())
                }

            // --- Combinación ---
            combine(firestoreFlow, localUnpublishedFlow) { firestoreProjects, localUnpublished ->
                Log.d("ProjectViewModel", "Combinando ${firestoreProjects.size} (Firestore) + ${localUnpublished.size} (Local NP)")
                (firestoreProjects + localUnpublished).sortedByDescending { it.createdAt }
            }
                .collect { combinedList ->
                    _uiState.update { it.copy(myProjects = combinedList) }
                }
        }
    }

    // ✨ FUNCIÓN AUXILIAR DE SINCRONIZACIÓN (CORREGIDA) ✨
    private suspend fun synchronizeFirestoreToRoom(userId: String, firestoreProjects: List<Project>) { // Recibe List<Project>
        Log.d("ProjectViewModel", "SYNC: Iniciando Firestore -> Room...")
        try {
            // Obtiene TODOS los originales locales (pub y no pub) para comparar
            val allLocalOriginals = getProjectsByUserUseCase(userId).firstOrNull() ?: emptyList()
            Log.d("ProjectViewModel", "SYNC: Comparando ${firestoreProjects.size} Firestore con ${allLocalOriginals.size} locales.")

            // 1. Asegurar que los de Firestore estén en Room y publicados
            firestoreProjects.forEach { firestoreProjectAsDomain ->
                val localMatch = allLocalOriginals.find { it.id == firestoreProjectAsDomain.id }
                if (localMatch == null) {
                    Log.i("ProjectViewModel", "SYNC: Insertando ${firestoreProjectAsDomain.id} desde Firestore en Room.")
                    // Inserta el Project (dominio) en Room (el UseCase lo convertirá a Entity)
                    insertProjectInDBUseCase(firestoreProjectAsDomain)
                } else if (!localMatch.isPublished) {
                    Log.i("ProjectViewModel", "SYNC: Marcando ${localMatch.id} como publicado en Room.")
                    // Actualiza el flag en Room (el UseCase lo convertirá a Entity)
                    insertProjectInDBUseCase(localMatch.copy(isPublished = true))
                }
            }

            // 2. Asegurar que los locales publicados que NO están en Firestore se desmarquen
            allLocalOriginals.forEach { localProject ->
                if (localProject.isPublished) {
                    val firestoreMatch = firestoreProjects.find { it.id == localProject.id }
                    if (firestoreMatch == null) {
                        Log.i("ProjectViewModel", "SYNC: Desmarcando ${localProject.id} en Room (no en Firestore).")
                        insertProjectInDBUseCase(localProject.copy(isPublished = false))
                    }
                }
            }
            Log.d("ProjectViewModel", "SYNC: Sincronización Firestore -> Room completada.")
        } catch (e: Exception) {
            Log.e("ProjectViewModel", "SYNC: Error durante sincronización", e)
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
        // TODO: Considerar añadir estado isPublishing a UiState para feedback visual
        // _uiState.update { it.copy(isPublishing = true, currentlyPublishingId = project.id) }

        viewModelScope.launch {
            var mixedMp3File: File? = null
            var individualMp3Files: List<File> = emptyList()
            var finalAudioUrl: String? = null
            val finalTrackUrls = mutableListOf<String>()
            // Usaremos 'projectDataToPublish' para asegurar que usamos datos consistentes
            var projectDataToPublish: Project? = null

            withContext(Dispatchers.Main) { Toast.makeText(context, "Preparando publicación...", Toast.LENGTH_SHORT).show() }

            try {
                // --- PASO 1: Exportar MP3 localmente ---
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

                // --- PASO 2: Subir MP3 a Firebase Storage ---
                Log.d("ProjectViewModel", "Subiendo MP3 principal a Storage...")
                withContext(Dispatchers.Main) { Toast.makeText(context, "Subiendo audio...", Toast.LENGTH_SHORT).show() }
                finalAudioUrl = uploadAudioToStorageUseCase(project.id, mainMp3ToUse, "mix.mp3").getOrThrow()
                Log.i("ProjectViewModel", "Audio principal subido. URL: $finalAudioUrl")

                // (Opcional) Subir tracks individuales
                coroutineScope { /* ... (tu código para subir tracks individuales) */ }
                Log.d("ProjectViewModel", "Subida de tracks individuales completada (si aplica).")

                // --- PASO 3: Guardar datos del Proyecto en Firestore ---
                Log.d("ProjectViewModel", "Guardando datos del proyecto en Firestore...")
                withContext(Dispatchers.Main) { Toast.makeText(context, "Guardando información...", Toast.LENGTH_SHORT).show() }

                // ✨✨✨ CORRECCIÓN AQUÍ ✨✨✨
                // 1. Convierte Project (Dominio) -> ProjectEntity (Room)
                //    (Asegúrate de que 'projectDataToPublish' no sea null)
                val projectEntity = projectDataToPublish!!.toDataBase(jsonUtils)

                // 2. Convierte ProjectEntity (Room) -> ProjectFirebaseModel (Firebase)
                var projectFirebaseModel = projectEntity.toFirebaseModel()

                // 3. Sobrescribe las URLs en el FirebaseModel con las URLs finales de Storage
                //    (Porque las de 'projectEntity' son probablemente paths locales)
                projectFirebaseModel = projectFirebaseModel.copy(
                    publishedAudioUrl = finalAudioUrl, // URL principal de Storage
                    // Codifica la lista de URLs de tracks a JSON String
                    publishedTrackUrls = jsonUtils.encodeToJson(finalTrackUrls)
                )
                upsertProjectInFirestoreUseCase(projectFirebaseModel).getOrThrow() // Guarda en Firestore
                Log.i("ProjectViewModel", "Datos del proyecto guardados en Firestore.")
                // --- PASO 4: Marcar Proyecto como publicado LOCALMENTE ---
                // Usa 'projectDataToPublish' como base para marcarlo como publicado
                val finalPublishedProjectLocal = projectDataToPublish.copy(isPublished = true)
                withContext(Dispatchers.IO){ insertProjectInDBUseCase(finalPublishedProjectLocal)}
                Log.d("ProjectViewModel", "Proyecto ${project.id} marcado como publicado localmente.")

                // --- PASO 5: Crear Post en Realtime Database ---
                Log.d("ProjectViewModel", "Creando Post en Realtime DB...")
                val post = Post(
                    id = System.currentTimeMillis().toString(), // Genera ID aquí o usa uno basado en el proyecto
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

                // --- PASO 6: Éxito ---
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "¡Proyecto publicado!", Toast.LENGTH_LONG).show()
                    // Refrescar la lista local si es necesario (aunque el Flow debería hacerlo)
                    // if (_uiState.value.tabSelected == ProjectTab.MY_PROJECTS) loadMyProjects()
                }

            } catch (e: Exception) {
                // --- Manejo de Errores ---
                Log.e("ProjectViewModel", "Error publicando proyecto ${project.id}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al publicar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
                // Intenta revertir el estado local si falló
                try {
                    // Usa 'projectDataToPublish' si no es null, si no, usa el 'project' original
                    val projectToRevert = projectDataToPublish ?: project
                    if(projectToRevert.isPublished){
                        insertProjectInDBUseCase(projectToRevert.copy(isPublished = false))
                    }
                } catch (_: Exception){}

            } finally {
                // --- Limpieza ---
                withContext(Dispatchers.IO){
                    mixedMp3File?.delete()
                    individualMp3Files.forEach { it.delete() }
                    Log.d("ProjectViewModel", "Archivos MP3 locales temporales eliminados (si existían).")
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