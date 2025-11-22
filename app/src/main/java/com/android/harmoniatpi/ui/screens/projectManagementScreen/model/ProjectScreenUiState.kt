package com.android.harmoniatpi.ui.screens.projectManagementScreen.model

import com.android.harmoniatpi.domain.model.project.Project

data class ProjectScreenUiState(
    val currentProjectSelected: Project? = null,
    val isRecording: Boolean = false,
    val isPlaying: Boolean = false,
    val tracks: List<TrackUi> = emptyList(),
    val timelineWidth: Int = 500,
    val previewTrackId: Long? = null,
    val currentPlaybackMs: Long = 0L,
    val isClipboardFull: Boolean = false,
    val isRestoringTracks: Boolean = false,
    val msPerDpScale: Float = 10f,
    val importAudioLoading : Boolean = false,
    val activeSheetContent: BottomSheetContent? = null,
    val totalProjectMs: Long = 0L,
    val bpm: Int = 120,
    val isMetronomeEnabled: Boolean = false,
    val metronomeVolume: Float = 1.0f,
    val precountMessage: String? = null,
    val fabPulseTrigger: Int = 0
)
