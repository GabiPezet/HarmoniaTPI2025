package com.android.harmoniatpi.ui.screens.songVersionsScreen.model

import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.song.DerivedVersion
import com.android.harmoniatpi.domain.model.song.Song

/**
 * Modelo de datos para representar el estado de la pantalla de detalles de canciones.
 */
data class SongVersionsUiState(
    val song: Song? = null,
    val derivedVersions: List<DerivedVersion> = emptyList(),
    val playingSongId: String? = null,
    val playbackState: PlaybackState = PlaybackState(),
    val allUsers: List<UserPreferences> = emptyList(),
    val isLoading: Boolean = true
)