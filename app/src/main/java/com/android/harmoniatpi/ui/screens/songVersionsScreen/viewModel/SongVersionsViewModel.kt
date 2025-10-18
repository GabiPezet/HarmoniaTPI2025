package com.android.harmoniatpi.ui.screens.songVersionsScreen.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.interfaces.ExoAudioPlayerRepository
import com.android.harmoniatpi.domain.usecases.GetSongDetailsUseCase
import com.android.harmoniatpi.ui.screens.songVersionsScreen.createMockDerivedVersions
import com.android.harmoniatpi.ui.screens.songVersionsScreen.createMockSong
import com.android.harmoniatpi.ui.screens.songVersionsScreen.model.PlaybackState
import com.android.harmoniatpi.ui.screens.songVersionsScreen.model.SongVersionsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    private val getSongDetailsUseCase: GetSongDetailsUseCase,
    private val exoAudioPlayer: ExoAudioPlayerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SongVersionsUiState())
    val uiState: StateFlow<SongVersionsUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        observePlaybackPosition()
        observePlaybackDuration()
    }

    /**
     * Carga inicial de datos simulada por ahora
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            // Simulamos una carga de red de 2 segundos
            delay(2000)

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
    }

    /**
     * Limpieza del ViewModel.Se para lo que se este reproduciendo.
     */
    override fun onCleared() {
        super.onCleared()
        exoAudioPlayer.stop()
    }
}