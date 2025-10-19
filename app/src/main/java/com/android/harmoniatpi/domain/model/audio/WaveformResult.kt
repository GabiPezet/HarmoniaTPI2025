package com.android.harmoniatpi.domain.model.audio

data class WaveformResult(
    val waveform: List<Float>,
    val durationMs: Long
)
