package com.android.harmoniatpi.domain.model.project

data class Project(
    val title: String,
    val description: String,
    val hashtags: List<String>,
    val audioWaveform: List<Float> = emptyList() // simulado o vacío
)