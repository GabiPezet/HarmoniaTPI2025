package com.android.harmoniatpi.ui.screens.editAudioTest.model

import android.net.Uri

enum class RecordMode { AUDIO, VIDEO }

data class EditAudioTestState(
    val recordMode: RecordMode = RecordMode.AUDIO,
    val isRecording: Boolean = false,
    val isPlaying: Boolean = false,
    val recordedFileUri: Uri? = null,
    val recordingDuration: Long = 0,
    val playbackProgress: Float = 0f
)