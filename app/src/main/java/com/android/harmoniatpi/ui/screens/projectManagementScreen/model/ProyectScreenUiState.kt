package com.android.harmoniatpi.ui.screens.projectManagementScreen.model

import com.android.harmoniatpi.domain.model.project.Project

data class ProyectScreenUiState(
    val currentProjectSelected: Project? = null,
    val isRecording: Boolean = false,
    val isPlaying: Boolean = false,
    val tracks: List<TrackUi> = emptyList(),
    val timelineWidth: Int = 500,
    val previewTrackId: Long? = null,
    val currentPlaybackMs: Long = 0L
)
