package com.android.harmoniatpi.ui.screens.songVersionsScreen.model

data class SongVersionsUiState(
    val originalSong: Song? = null,
    val derivedVersions: List<DerivedVersion> = emptyList(),
    val currentPlaybackProgress: Float = 0f,
    val isOriginalPlaying: Boolean = false,
    val playingDerivedVersionId: String? = null,
    val isLoading: Boolean = true
)