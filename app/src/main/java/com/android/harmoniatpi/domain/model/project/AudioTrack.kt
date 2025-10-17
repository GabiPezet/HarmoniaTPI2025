package com.android.harmoniatpi.domain.model.project

import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.TrackUi

data class AudioTrack(
    val id: Long,
    val path: String,
    val title: String,
    val selected: Boolean,
    val waveForm: List<Float>? = null,
    val durationMs: Long = 0L,
    val isUndoAvailable: Boolean = false,
    val isMuted: Boolean = false,
    val volume: Float = 1f
) {
    fun toTrackUi(): TrackUi {
        return TrackUi(
            id = id,
            path = path,
            title = title,
            selected = selected,
            waveForm = waveForm,
            durationMs = durationMs,
            isUndoAvailable = isUndoAvailable,
            isMuted = isMuted,
            volume = volume
        )
    }
}