package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.data.ProjectRepository
import com.android.harmoniatpi.domain.model.project.Project
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProjectListViewModel @Inject constructor(
    projectRepository: ProjectRepository
) : ViewModel() {
    val projects: StateFlow<List<Project>> =
        projectRepository.getAllProjects()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}