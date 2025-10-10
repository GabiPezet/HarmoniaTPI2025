package com.android.harmoniatpi.ui.screens.projectManagementScreen.model

data class TrackUi(
    val id: Long,
    val path: String,
    val title: String,
    val selected: Boolean,
    val waveForm: List<Float>? = null
)
