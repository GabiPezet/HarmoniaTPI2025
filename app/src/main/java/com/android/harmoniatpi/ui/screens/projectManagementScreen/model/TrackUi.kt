package com.android.harmoniatpi.ui.screens.projectManagementScreen.model

import com.android.harmoniatpi.domain.model.audio.AudioSourceType
import com.android.harmoniatpi.domain.model.project.AudioTrack

data class TrackUi(
    val id: Long,
    val path: String,
    val title: String,
    val selected: Boolean,
    val sourceType: AudioSourceType,
    val waveForm: List<Float>? = null,
    val durationMs: Long = 0L,
    val isUndoAvailable: Boolean = false,
    val isMuted: Boolean = false,
    val volume: Float = 1f,
    val startOffsetMs: Long = 0L,
    val selectionStartMs: Long? = null,
    val selectionEndMs: Long? = null,
    val isUndoEffectAvailable: Boolean = false
) {
    fun toAudioTrack(): AudioTrack {
        return AudioTrack(
            id = id,
            path = path,
            title = title,
            selected = selected,
            sourceType = sourceType,
            waveForm = waveForm,
            durationMs = durationMs,
            isUndoAvailable = isUndoAvailable,
            isMuted = isMuted,
            volume = volume,
            startOffsetMs = startOffsetMs,
        )
    }
}
