package com.android.harmoniatpi.ui.screens.songVersionsScreen.viewModel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.interfaces.ExoAudioPlayerRepository
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.domain.model.song.DerivedVersion
import com.android.harmoniatpi.domain.model.song.Song
import com.android.harmoniatpi.domain.model.song.VersionType
import com.android.harmoniatpi.domain.model.user.User
import com.android.harmoniatpi.domain.usecases.GetProjectByIdUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.FetchAndSyncUsersUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.GetAllUserFromDBUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.GetDerivedProjectsFromFirestoreUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.GetProjectByIdFromFirestoreUseCase
import com.android.harmoniatpi.ui.screens.songVersionsScreen.createMockDerivedVersions
import com.android.harmoniatpi.ui.screens.songVersionsScreen.createMockSong
import com.android.harmoniatpi.ui.screens.songVersionsScreen.model.PlaybackState
import com.android.harmoniatpi.ui.screens.songVersionsScreen.model.SongVersionsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de detalles de canciones [com.android.harmoniatpi.ui.screens.songVersionsScreen.SongVersionsScreen]¨.
 */
@HiltViewModel
class SongVersionsViewModel @Inject constructor(
    private val exoAudioPlayer: ExoAudioPlayerRepository,
    private val savedStateHandle: SavedStateHandle,
    private val getProjectByIdUseCase: GetProjectByIdUseCase,
    private val getDerivedProjectsFromFirestoreUseCase: GetDerivedProjectsFromFirestoreUseCase,
    private val getProjectByIdFromFirestoreUseCase: GetProjectByIdFromFirestoreUseCase,
    private val getAllUsersUseCase: GetAllUserFromDBUseCase,
    private val fetchAndSyncUsersUseCase: FetchAndSyncUsersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SongVersionsUiState())
    val uiState: StateFlow<SongVersionsUiState> = _uiState.asStateFlow()

    init {
        // 3. Obtener el ID del proyecto desde la navegación
        val clickedProjectId: String? =
            savedStateHandle["projectId"] // Asume que la ruta es ".../{projectId}"

        if (clickedProjectId != null) {
            // 4. Cargar datos reales en lugar de los mocks
            loadProjectData(clickedProjectId)
        } else {
            // Error: No se proporcionó ID, mostrar estado de error/vacío
            Log.e("SongVersionsViewModel", "No se recibió projectId desde la navegación.")
            _uiState.update { it.copy(isLoading = false) }
        }

        observePlaybackPosition()
        observePlaybackDuration()
        observeAllUsersFromRoom()
    }

    /**
     * Carga los datos del proyecto (original y derivados) desde la base de datos (Room).
     */
    private fun loadProjectData(clickedProjectId: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                // 1. Obtener el proyecto clickeado (de Room, esto está bien)
                val clickedProject = getProjectByIdUseCase(clickedProjectId)

                // 2. Determinar el ID del original
                val originalProjectId = clickedProject.originalProjectId ?: clickedProject.id

                // 3. Buscar el original SIEMPRE en Firestore (Correcto)
                Log.d(
                    "SongVersionsViewModel",
                    "Buscando original $originalProjectId en Firestore..."
                )
                val originalProject = getProjectByIdFromFirestoreUseCase(originalProjectId)

                if (originalProject == null) {
                    Log.e(
                        "SongVersionsViewModel",
                        "¡Error fatal! No se encontró el proyecto original $originalProjectId en Firestore."
                    )
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }

                // DESPUÉS (Correcto, busca en remoto):
                Log.d(
                    "SongVersionsViewModel",
                    "Buscando derivados de ${originalProject.id} en Firestore..."
                )
                val derivedProjects = getDerivedProjectsFromFirestoreUseCase(originalProject.id)
                // --- FIN DEL ARREGLO ---

                viewModelScope.launch(Dispatchers.IO) {
                    val allOwnerIds =
                        (derivedProjects.map { it.ownerId } + originalProject.ownerId).distinct()
                    if (allOwnerIds.isNotEmpty()) {
                        Log.d("SongVersionsViewModel", "Sincronizando avatares para: $allOwnerIds")
                        fetchAndSyncUsersUseCase(allOwnerIds)
                    }
                }

                // 5. Mapear los Proyectos (Esto está bien)
                val originalSong = mapProjectToSong(originalProject)
                val derivedVersions = derivedProjects.map { mapProjectToDerivedVersion(it) }

                // 6. Actualizar el UI State (Esto está bien)
                _uiState.update { currentState ->
                    currentState.copy(
                        song = originalSong,
                        derivedVersions = derivedVersions,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                Log.e("SongVersionsViewModel", "Error al cargar datos del proyecto", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Mapea un objeto Project a un objeto Song (usado por la UI).
     * NOTA: El modelo 'Project' no tiene 'imageUrl' (portada) ni 'avatarUrl'.
     * La UI usará los placeholders definidos en 'SongVersionsScreen.kt'.
     */
    private fun mapProjectToSong(project: Project): Song {
        val creator = User(
            id = project.ownerId,
            name = "${project.name} ${project.lastName}",
            avatarUrl = null
        )

        // Asumo que tienes un enum o clase para VersionType
        val versionType = if (project.originalProjectId == null) {
            VersionType.ORIGINAL
        } else {
            VersionType.DERIVED
        }

        return Song(
            id = project.id,
            title = project.title,
            creator = creator,
            audioUrl = project.urlCompleteAudio ?: "", // URL del audio publicado (mix)
            durationMillis = project.duration,
            projectId = project.id, // ID para "Abrir proyecto"
            imageUrl = null, // Project no tiene esta info, la UI mostrará un placeholder
            versionType = versionType // Mapeo simple del tipo
        )
    }

    /**
     * Mapea un objeto Project a un objeto DerivedVersion (usado por la UI).
     */
    private fun mapProjectToDerivedVersion(project: Project): DerivedVersion {
        val creator = User(
            id = project.ownerId,
            name = "${project.name} ${project.lastName}",
            avatarUrl = null // Project no tiene esta info, la UI mostrará un placeholder
        )

        return DerivedVersion(
            id = project.id,
            creator = creator,
            projectId = project.id,
            audioUrl = project.urlCompleteAudio, // URL del audio publicado (mix)
            durationMillis = project.duration
        )
    }


    /**
     * Observador de la posición actual del audio.
     */
    private fun observePlaybackPosition() {
        viewModelScope.launch {
            exoAudioPlayer.getCurrentPositionMs().collect { currentPositionMs ->
                // Leemos el estado actual
                val currentState = _uiState.value
                val playbackState = currentState.playbackState

                // SOLO actualizamos la posición si el ViewModel CREE que está sonando
                if (playbackState.isPlaying) {

                    val totalDuration = playbackState.totalDurationMs

                    // Comprobamos si la canción terminó (mientras sonaba)
                    if (totalDuration > 0 && currentPositionMs >= totalDuration) {

                        // --- Lógica de FIN DE CANCIÓN ---
                        _uiState.update {
                            it.copy(
                                playbackState = it.playbackState.copy(
                                    isPlaying = false,
                                    currentPositionMs = totalDuration // Fija el slider al final
                                ),
                                playingSongId = null // Libera el ID
                            )
                        }
                    } else {
                        // --- Lógica de REPRODUCCIÓN ---
                        // Sigue sonando, solo actualiza la posición
                        _uiState.update {
                            it.copy(playbackState = it.playbackState.copy(currentPositionMs = currentPositionMs))
                        }
                    }
                }

                // Si 'playbackState.isPlaying' es false (es decir, pausado o detenido),
                // no hacemos NADA. El 'currentPositionMs' del UiState
                // se queda "congelado" en el último valor que tuvo,
                // que es exactamente lo que queremos.
            }
        }
    }

    /**
     * Observador de la duración total del audio.
     */
    private fun observePlaybackDuration() {
        viewModelScope.launch {
            exoAudioPlayer.getTotalDurationMs().collect { durationMs ->
                if (durationMs > 0) {
                    _uiState.update {
                        it.copy(playbackState = it.playbackState.copy(totalDurationMs = durationMs))
                    }
                }
            }
        }
    }

    /**
     * Maneja el clic en el botón de reproducción/pausa de la canción original.
     */
    fun onPlayPauseOriginal() {
        val currentState = _uiState.value
        val song = currentState.song ?: return

        val isThisSongActive = currentState.playingSongId == song.id
        val isCurrentlyPlaying = currentState.playbackState.isPlaying

        if (isThisSongActive && isCurrentlyPlaying) {
            exoAudioPlayer.pause()
            _uiState.update {
                it.copy(playbackState = it.playbackState.copy(isPlaying = false))
            }
        } else if (isThisSongActive && !isCurrentlyPlaying) {
            exoAudioPlayer.resume()
            _uiState.update {
                it.copy(playbackState = it.playbackState.copy(isPlaying = true))
            }
        } else {
            exoAudioPlayer.stop()
            exoAudioPlayer.play(song.audioUrl)
            _uiState.update {
                it.copy(
                    playingSongId = song.id,
                    playbackState = PlaybackState(
                        isPlaying = true,
                        totalDurationMs = song.durationMillis
                    )
                )
            }
        }
    }

    /**
     * Maneja el clic en el botón de reproducción/pausa de una versión derivada.
     */
    fun onPlayPauseDerived(versionId: String) {
        val currentState = _uiState.value
        val targetVersion = currentState.derivedVersions.find { it.id == versionId } ?: return

        val isThisSongActive = currentState.playingSongId == versionId
        val isCurrentlyPlaying = currentState.playbackState.isPlaying

        if (isThisSongActive && isCurrentlyPlaying) {
            exoAudioPlayer.pause()
            _uiState.update {
                it.copy(
                    playbackState = it.playbackState.copy(isPlaying = false),
                )
            }
        } else if (isThisSongActive && !isCurrentlyPlaying) {
            exoAudioPlayer.resume()
            _uiState.update {
                it.copy(playbackState = it.playbackState.copy(isPlaying = true))
            }
        } else {
            exoAudioPlayer.stop()
            targetVersion.audioUrl?.let { url ->
                exoAudioPlayer.play(url)
                _uiState.update {
                    it.copy(
                        playingSongId = versionId,
                        playbackState = PlaybackState(
                            isPlaying = true,
                            totalDurationMs = targetVersion.durationMillis ?: 0L
                        )
                    )
                }
            }
        }
    }

    /**
     * Maneja el cambio de posición en el reproductor de audio.
     */
    fun onSliderChange(newProgress: Float) {
        val totalDuration = _uiState.value.playbackState.totalDurationMs
        if (totalDuration > 0) {
            val newPositionMs = (newProgress * totalDuration).toLong()
            exoAudioPlayer.seekTo(newPositionMs)
            _uiState.update {
                it.copy(playbackState = it.playbackState.copy(currentPositionMs = newPositionMs))
            }
        }
    }

    /**
     * Maneja el clic en el botón de abrir proyecto.
     */
    fun onOpenProject(projectId: String?) {
        if (projectId == null) return
        // TODO: Lógica para navegar a la pantalla del proyecto
        // (Esta lógica ya estaba en tu VM)
    }

    private fun observeAllUsersFromRoom() {
        viewModelScope.launch {
            getAllUsersUseCase().collect { usersListFromRoom ->
                _uiState.update { it.copy(allUsers = usersListFromRoom) }
            }
        }
    }

    /**
     * Limpieza del ViewModel.Se para lo que se este reproduciendo.
     */
    override fun onCleared() {
        super.onCleared()
        exoAudioPlayer.stop()
    }

    /**
     * Carga inicial de datos simulada.
     * ESTA FUNCIÓN YA NO SE LLAMA, AHORA SE USA loadProjectData
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            // Simulamos una carga de red de 2 segundos
            // delay(2000) // Ya no es necesario, 'loadProjectData' es real

            // Creamos los datos de ejemplo
            val song = createMockSong()

            val derivedVersions = createMockDerivedVersions()

            _uiState.update { currentState ->
                currentState.copy(
                    song = song,
                    derivedVersions = derivedVersions,
                    isLoading = false
                )
            }
        }
    }
}