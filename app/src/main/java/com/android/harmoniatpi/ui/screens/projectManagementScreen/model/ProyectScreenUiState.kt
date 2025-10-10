package com.android.harmoniatpi.ui.screens.projectManagementScreen.model

data class ProyectScreenUiState(
    val isRecording: Boolean = false,
    val isPlaying: Boolean = false,
    val tracks: List<TrackUi> = emptyList(),
    val timelineWidth: Int = 500
)
