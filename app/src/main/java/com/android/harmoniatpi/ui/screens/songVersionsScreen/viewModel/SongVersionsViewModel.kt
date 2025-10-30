package com.android.harmoniatpi.ui.screens.songVersionsScreen.viewModel
import android.util.Log
import androidx.lifecycle.SavedStateHandle // <-- 1. IMPORTAR
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.interfaces.ExoAudioPlayerRepository
import com.android.harmoniatpi.domain.model.project.Project // <-- IMPORTAR
// 2. IMPORTAR LOS MODELOS QUE LA UI ESPERA
import com.android.harmoniatpi.domain.model.song.DerivedVersion
import com.android.harmoniatpi.domain.model.song.Song
import com.android.harmoniatpi.domain.model.user.User
import com.android.harmoniatpi.domain.usecases.GetProjectByIdUseCase // <-- 3. IMPORTAR USE CASE
import com.android.harmoniatpi.domain.usecases.GetSongDetailsUseCase
import com.android.harmoniatpi.domain.usecases.roomUseCases.GetAllProjectsFromDBUseCase // <-- 3. IMPORTAR USE CASE
import com.android.harmoniatpi.ui.screens.songVersionsScreen.createMockDerivedVersions
import com.android.harmoniatpi.ui.screens.songVersionsScreen.createMockSong
import com.android.harmoniatpi.ui.screens.songVersionsScreen.model.PlaybackState
import com.android.harmoniatpi.ui.screens.songVersionsScreen.model.SongVersionsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull // <-- IMPORTAR
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.android.harmoniatpi.domain.model.song.VersionType

/**
 * ViewModel para la pantalla de detalles de canciones [com.android.harmoniatpi.ui.screens.songVersionsScreen.SongVersionsScreen]¨.
 */
@HiltViewModel
class SongVersionsViewModel @Inject constructor(
    private val getSongDetailsUseCase: GetSongDetailsUseCase, // Puedes borrarlo si no se usa
    private val exoAudioPlayer: ExoAudioPlayerRepository,
    private val savedStateHandle: SavedStateHandle, // <-- 1. INYECTAR
    private val getProjectByIdUseCase: GetProjectByIdUseCase, // <-- 2. INYECTAR
    private val getAllProjectsFromDBUseCase: GetAllProjectsFromDBUseCase // <-- 2. INYECTAR
) : ViewModel() {

    private val _uiState = MutableStateFlow(SongVersionsUiState())
    val uiState: StateFlow<SongVersionsUiState> = _uiState.asStateFlow()

    init {
        // 3. Obtener el ID del proyecto desde la navegación
        val clickedProjectId: String? = savedStateHandle["projectId"] // Asume que la ruta es ".../{projectId}"

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
    }

    /**
     * Carga los datos del proyecto (original y derivados) desde la base de datos (Room).
     */
    private fun loadProjectData(clickedProjectId: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                // 1. Obtener el proyecto en el que se hizo clic
                val clickedProject = getProjectByIdUseCase(clickedProjectId)

                // 2. Determinar cuál es el proyecto original
                val originalProject: Project
                if (clickedProject.originalProjectId != null) {
                    // Es un clon, buscar el original
                    originalProject = getProjectByIdUseCase(clickedProject.originalProjectId)
                } else {
                    // Ya es el original
                    originalProject = clickedProject
                }

                // 3. Obtener TODOS los proyectos de la DB para filtrar las versiones derivadas
                // .firstOrNull() toma el primer valor del Flow y cancela la colección
                val allProjects = getAllProjectsFromDBUseCase().firstOrNull() ?: emptyList()

                // 4. Filtrar para encontrar solo las versiones derivadas (proyectos que apuntan al original)
                val derivedProjects = allProjects.filter {
                    it.originalProjectId == originalProject.id
                }

                // 5. Mapear los Proyectos a los modelos que espera la UI (Song y DerivedVersion)
                val originalSong = mapProjectToSong(originalProject)
                val derivedVersions = derivedProjects.map { mapProjectToDerivedVersion(it) }

                // 6. Actualizar el UI State con los datos reales
                _uiState.update { currentState ->
                    currentState.copy(
                        song = originalSong,
                        derivedVersions = derivedVersions,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                // Manejar error (ej. proyecto no encontrado)
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
                if (_uiState.value.playbackState.isPlaying) {
                    _uiState.update {
                        it.copy(playbackState = it.playbackState.copy(currentPositionMs = currentPositionMs))
                    }
                }
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
        val isPlayingThis = currentState.playingSongId == song.id

        if (isPlayingThis) {
            exoAudioPlayer.pause()
            _uiState.update { it.copy(playbackState = it.playbackState.copy(isPlaying = false)) }
        } else {
            exoAudioPlayer.stop()
            exoAudioPlayer.play(song.audioUrl) // Usa la URL del 'Song' mapeado
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
        val isPlayingThis = currentState.playingSongId == versionId

        if (isPlayingThis) {
            exoAudioPlayer.pause()
            _uiState.update {
                it.copy(
                    playbackState = it.playbackState.copy(isPlaying = false),
                    playingSongId = null
                )
            }
        } else {
            exoAudioPlayer.stop()
            targetVersion.audioUrl?.let { url ->
                exoAudioPlayer.play(url) // Usa la URL de la 'DerivedVersion' mapeada
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