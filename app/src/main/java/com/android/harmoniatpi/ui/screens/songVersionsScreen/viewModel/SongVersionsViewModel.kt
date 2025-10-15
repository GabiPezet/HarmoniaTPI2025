package com.android.harmoniatpi.ui.screens.songVersionsScreen.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.model.song.DerivedVersion
import com.android.harmoniatpi.domain.model.song.Song
import com.android.harmoniatpi.domain.model.song.VersionType
import com.android.harmoniatpi.domain.model.user.User
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
                creator = User(id = "creator-01", name = "Luna Beats", avatarUrl = "url_de_avatar"),
                imageUrl = "url_de_imagen",
                durationMillis = 464000L,
                versionType = VersionType.ORIGINAL,
                audioUrl = "url_de_audio",
                projectId = "proj-01",
            )

            val derivedVersions = listOf(
                DerivedVersion("v1",
                    User(
                        "u1",
                        "Luna Beats",
                        "https://images.unsplash.com/photo-1492684223066-81342ee5ff30"
                    ), "projectA"),
                DerivedVersion("v2", User("u2", "Echo Rivera", "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91"), "projectB"),
                DerivedVersion("v3", User("u3", "Kai Harmonix", "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e"), "projectC"),
                DerivedVersion("v4", User("u4", "Selene Nova", "https://images.unsplash.com/photo-1524504388940-b1c1722653e1"), "projectD"),
                DerivedVersion("v5", User("u5", "Aria Flow", "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61"), "projectE"),
                DerivedVersion("v6", User("u6", "Noah Frequenza", "https://images.unsplash.com/photo-1494790108377-be9c29b29330"), "projectF"),
                DerivedVersion("v7", User("u7", "Zion Wave", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d"), "projectG"),
                DerivedVersion("v8", User("u8", "Vera Pulse", "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e"), "projectH"),
                DerivedVersion("v9", User("u9", "Milo Resonance", "https://images.unsplash.com/photo-1521119989659-a83eee488004"), "projectI"),
                DerivedVersion("v10", User("u10", "Nia Groove", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde"), "projectJ"),
                DerivedVersion("v11", User("u11", "Riley Sound", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde"), "projectK"),
                DerivedVersion("v12", User("u12", "Ivy Echo", "https://images.unsplash.com/photo-1544005313-94ddf0286df2"), "projectL")
            )

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