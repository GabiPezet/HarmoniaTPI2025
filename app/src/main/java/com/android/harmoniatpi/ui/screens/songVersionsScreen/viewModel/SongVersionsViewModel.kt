package com.android.harmoniatpi.ui.screens.songVersionsScreen.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.ui.screens.songVersionsScreen.model.DerivedVersion
import com.android.harmoniatpi.ui.screens.songVersionsScreen.model.Song
import com.android.harmoniatpi.ui.screens.songVersionsScreen.model.SongVersionsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongVersionsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SongVersionsUiState())
    val uiState: StateFlow<SongVersionsUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    // LÓGICA DE NEGOCIO (simulada):
    private fun loadInitialData() {
        // Usamos viewModelScope para lanzar una corrutina que no se cancelará
        // si la pantalla rota, por ejemplo.
        viewModelScope.launch {
            // Simulamos una carga de red de 2 segundos
            delay(2000)

            // Creamos los datos de ejemplo
            val originalSong = Song(
                id = "original-01",
                title = "El paso del tiempo",
                artistName = "Jane Smith",
                durationMillis = 464000L,
                versionType = "original",
                audioUrl = "url_de_audio",
                projectId = "proj-01",
                artistImageUrl = "url_de_imagen",
            )
            val derivedVersions = List(5) { index ->
                DerivedVersion(
                    id = "derived-$index",
                    userName = listOf(
                        "Brian Perez",
                        "Adelaida Rojas",
                        "Nico Rizzo",
                        "Charly Giménez",
                        "Sebastián Prato"
                    )[index],
                    userImageUrl = "url_de_avatar",
                    projectId = "proj-$index"
                )
            }

            // Actualizamos el estado con los datos cargados.
            // `update` es la forma segura de modificar el StateFlow.
            _uiState.update { currentState ->
                currentState.copy(
                    originalSong = originalSong,
                    derivedVersions = derivedVersions,
                    isLoading = false // ¡Muy importante! Desactivamos la carga.
                )
            }
        }
    }

    // MANEJADORES DE EVENTOS:
    //    Estas son las funciones públicas que la UI llamará.

    fun onPlayPauseOriginal() {
        _uiState.update { currentState ->
            currentState.copy(isOriginalPlaying = !currentState.isOriginalPlaying)
        }
        // TODO: Aquí iría la lógica real para controlar el reproductor de audio
    }

    fun onPlayPauseDerived(versionId: String) {
        _uiState.update { currentState ->
            val currentlyPlaying = currentState.playingDerivedVersionId
            currentState.copy(
                // Si la versión clickeada ya estaba sonando, la detenemos.
                // Si no, detenemos la anterior (si hay) y reproducimos la nueva.
                playingDerivedVersionId = if (currentlyPlaying == versionId) null else versionId
            )
        }
        // TODO: Lógica del reproductor para la versión derivada
    }

    fun onSliderChange(newProgress: Float) {
        _uiState.update { it.copy(currentPlaybackProgress = newProgress) }
        // TODO: Lógica para que el reproductor busque (seek) a la nueva posición
    }

    fun onOpenProject(projectId: String?) {
        if (projectId == null) return
        // TODO: Lógica para navegar a la pantalla del proyecto
    }
}