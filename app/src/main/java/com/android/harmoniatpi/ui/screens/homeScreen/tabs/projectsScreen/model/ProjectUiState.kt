package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.model

import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.project.Project

data class ProjectUiState(
    val title: String = "",
    val description: String = "",
    val hashtags: String = "",
    val selectedImageUri: String? = null,
    val audioWaveform: List<Float> = emptyList(),
    val isLoading: Boolean = false,
    val isTitleValid: Boolean = false,
    val isFormValid: Boolean = false,
    val myProjects : List<Project> = emptyList(),
    val allProjects : List<Project> = emptyList(),
    val tabSelected : ProjectTab = ProjectTab.MY_PROJECTS,
    val currentlyPlayingProject: Project? = null,
    val isPreviewLoading: Boolean = false,
    val allUsers: List<UserPreferences> = emptyList(),
    val isPublishing: Boolean = false
)