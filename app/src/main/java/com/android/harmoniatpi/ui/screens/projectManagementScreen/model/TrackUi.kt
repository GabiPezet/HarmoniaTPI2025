package com.android.harmoniatpi.ui.screens.projectManagementScreen.model

import com.android.harmoniatpi.domain.model.audio.AudioSourceType

data class TrackUi(
    val id: Long,
    val path: String,
    val title: String,
    val selected: Boolean,
    val waveForm: List<Float>? = null,
    val durationMs: Long = 0L,
    val isUndoAvailable: Boolean = false,
    val isMuted: Boolean = false,
    val volume: Float = 1f,
    val startOffsetMs: Long = 0L,
    val sourceType: AudioSourceType
)
