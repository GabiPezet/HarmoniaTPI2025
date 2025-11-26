package com.android.harmoniatpi.ui.screens.songVersionsScreen.viewModel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.interfaces.ExoAudioPlayerRepository
import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.domain.model.song.DerivedVersion
import com.android.harmoniatpi.domain.model.song.Song
import com.android.harmoniatpi.domain.model.song.VersionType
import com.android.harmoniatpi.domain.model.user.User
import com.android.harmoniatpi.domain.usecases.GetProjectByIdUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.GetDerivedProjectsFromFirestoreUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.GetProjectByIdFromFirestoreUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.GetUsersFromFirestoreUseCase
import com.android.harmoniatpi.ui.screens.songVersionsScreen.model.PlaybackState
import com.android.harmoniatpi.ui.screens.songVersionsScreen.model.SongVersionsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val getUsersFromFirestoreUseCase: GetUsersFromFirestoreUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SongVersionsUiState())
    val uiState: StateFlow<SongVersionsUiState> = _uiState.asStateFlow()

    init {
        val clickedProjectId: String? = savedStateHandle["projectId"]

        if (clickedProjectId != null) {
            loadProjectData(clickedProjectId)
        } else {
            Log.e("SongVersionsViewModel", "No se recibió projectId desde la navegación.")
            _uiState.update { it.copy(isLoading = false) }
        }

        observePlaybackPosition()
        observePlaybackDuration()
    }

    private fun loadProjectData(clickedProjectId: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                // 1. Obtener proyecto local (clickeado)
                val clickedProject = getProjectByIdUseCase(clickedProjectId)

                // 2. Obtener original desde Firestore
                val originalProjectId = clickedProject.originalProjectId ?: clickedProject.id
                val originalProject = getProjectByIdFromFirestoreUseCase(originalProjectId)

                if (originalProject == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }

                // 3. Obtener derivados desde Firestore
                val derivedProjects = getDerivedProjectsFromFirestoreUseCase(originalProject.id)

                // 4. --- GESTIÓN DE USUARIOS FRESCA ---
                // Recolectar todos los IDs de creadores (original + derivados)
                val allOwnerIds = (derivedProjects.map { it.ownerId } + originalProject.ownerId).distinct()

                // Obtener datos frescos de Firestore (sin guardar en DB local)
                val usersResult =getUsersFromFirestoreUseCase(allOwnerIds).getOrNull() ?: emptyList()

                // Actualizar la lista en memoria por si acaso la UI la usa directamente
                _uiState.update { it.copy(allUsers = usersResult) }

                // 5. Mapear los proyectos a objetos de UI (Song/Derived)
                // Pasamos la lista de usuarios para que el mapeador busque la foto correcta
                val originalSong = mapProjectToSong(originalProject, usersResult)
                val derivedVersions = derivedProjects.map { mapProjectToDerivedVersion(it, usersResult) }

                // 6. Actualizar UI State
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


    private fun mapProjectToSong(project: Project, users: List<UserPreferences>): Song {
        // Buscamos al usuario en la lista fresca
        val userProfile = users.find { it.userID == project.ownerId }

        // Creamos el objeto User con la foto real (si existe)
        val creator = User(
            id = project.ownerId,
            name = userProfile?.userName ?: project.name,
            avatarUrl = userProfile?.userPhotoPathRemote?.ifBlank { null }
        )

        val versionType = if (project.originalProjectId == null) {
            VersionType.ORIGINAL
        } else {
            VersionType.DERIVED
        }

        return Song(
            id = project.id,
            title = project.title,
            creator = creator,
            audioUrl = project.urlCompleteAudio ?: "",
            durationMillis = project.duration,
            projectId = project.id,
            imageUrl = project.imageUrl,
            versionType = versionType
        )
    }

    private fun mapProjectToDerivedVersion(project: Project, users: List<UserPreferences>): DerivedVersion {
        val userProfile = users.find { it.userID == project.ownerId }

        val creator = User(
            id = project.ownerId,
            name = userProfile?.userName ?: project.name,
            // ✨ LA MAGIA: Foto fresca aquí también
            avatarUrl = userProfile?.userPhotoPathRemote?.ifBlank { null }
        )

        return DerivedVersion(
            id = project.id,
            creator = creator,
            projectId = project.id,
            audioUrl = project.urlCompleteAudio,
            durationMillis = project.duration
        )
    }

    // ... Resto de métodos de reproducción (onPlayPause, Slider, etc.) sin cambios ...

    private fun observePlaybackPosition() {
        viewModelScope.launch {
            exoAudioPlayer.getCurrentPositionMs().collect { currentPositionMs ->
                val currentState = _uiState.value
                val playbackState = currentState.playbackState

                if (playbackState.isPlaying) {
                    val totalDuration = playbackState.totalDurationMs
                    if (totalDuration > 0 && currentPositionMs >= totalDuration) {
                        _uiState.update {
                            it.copy(
                                playbackState = it.playbackState.copy(
                                    isPlaying = false,
                                    currentPositionMs = totalDuration
                                ),
                                playingSongId = null
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(playbackState = it.playbackState.copy(currentPositionMs = currentPositionMs))
                        }
                    }
                }
            }
        }
    }

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

    fun onOpenProject(projectId: String?) {
        if (projectId == null) return
    }

    override fun onCleared() {
        super.onCleared()
        exoAudioPlayer.stop()
    }
}