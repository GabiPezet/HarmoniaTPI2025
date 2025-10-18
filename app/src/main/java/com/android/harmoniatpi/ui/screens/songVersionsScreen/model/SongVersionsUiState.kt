package com.android.harmoniatpi.ui.screens.songVersionsScreen.model

import com.android.harmoniatpi.domain.model.song.DerivedVersion
import com.android.harmoniatpi.domain.model.song.Song

data class SongVersionsUiState(
    val originalSong: Song? = null,
    val derivedVersions: List<DerivedVersion> = emptyList(),
    val currentPlaybackProgress: Float = 0f,
    val isOriginalPlaying: Boolean = false,
    val playingDerivedVersionId: String? = null,
    val isLoading: Boolean = true
)