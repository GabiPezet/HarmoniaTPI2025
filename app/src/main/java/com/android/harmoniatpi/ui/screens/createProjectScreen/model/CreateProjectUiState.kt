package com.android.harmoniatpi.ui.screens.createProjectScreen.model

data class CreateProjectUiState(
    val title: String = "",
    val description: String = "",
    val hashtags: String = "",
    val audioWaveform: List<Float> = emptyList(),
    val isLoading: Boolean = false,
    val isTitleValid: Boolean = false,
    val isFormValid: Boolean = false
)
