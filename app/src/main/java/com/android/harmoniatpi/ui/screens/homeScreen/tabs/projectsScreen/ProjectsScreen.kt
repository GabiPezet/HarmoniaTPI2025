package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components.CollabScreen
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components.MyProjectsLayout
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components.CreateProjectDialog
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components.EditProjectDialog
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components.ProjectTabSelector
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.model.ProjectTab
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.viewmodel.ProjectViewModel

@Composable
fun ProjectsScreen(
    onNavigateToProjectManagementScreen: () -> Unit,
    onNavigateToVersion: (Project) -> Unit,
    viewModel: ProjectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sharedState by viewModel.sharedMenuUiState.uiState.collectAsState()

    var showCreateForm by remember { mutableStateOf(false) }
    var projectToEdit by remember { mutableStateOf<Project?>(null) } // ➕ AÑADE ESTE ESTADO

    val handleProjectClick = { project: Project ->
        viewModel.setCurrentProject(project)
        onNavigateToProjectManagementScreen()
    }

    Column {
        ProjectTabSelector(
            selectedTab = uiState.tabSelected,
            onTabSelected = { viewModel.onTabSelected(it) })

        if (uiState.tabSelected == ProjectTab.MY_PROJECTS) {
            MyProjectsLayout(
                projects = uiState.myProjects,
                sharedStateUserID = sharedState.userID,
                onShowForm = { showCreateForm = true },
                onProjectClick = handleProjectClick,
                onNavigateToVersion = onNavigateToVersion,
                viewModel = viewModel,
                onEditClick = { projectToEdit = it }
            )
        } else {
            CollabScreen(
                projects = uiState.allProjects,
                currentUserId = sharedState.userID,
                onProjectClick = handleProjectClick,
                onNavigateToVersions = onNavigateToVersion,
                onForkClick = { project -> viewModel.cloneProject(project) },
                onEditClick = { projectToEdit = it },
                onDeleteClick = { id -> viewModel.deleteProject(id) }
            )
        }
    }

    // Diálogo para crear
    if (showCreateForm) {
        CreateProjectDialog(
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = { showCreateForm = false }
        )
    }

    // ➕ Diálogo para editar
    projectToEdit?.let { project ->
        EditProjectDialog(
            project = project,
            viewModel = viewModel,
            onDismiss = { projectToEdit = null }
        )
    }
}

