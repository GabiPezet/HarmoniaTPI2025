package com.android.harmoniatpi.ui.screens.songVersionsScreen.model

/**
 * Modelo de datos para representar el estado del reproductor de audio.
 */
data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L,
)
