package com.android.harmoniatpi.domain.model

data class Project (
    val title: String,
    val description: String,
    val hashtags: List<String>,
    val audioWaveform: List<Float>
)