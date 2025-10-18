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
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components.ProjectTabSelector
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.model.ProjectTab
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.viewmodel.ProjectViewModel

@Composable
fun ProjectsScreen(
    onNavigateToProjectManagementScreen: () -> Unit,
    onNavigateToVersion: (Project) -> Unit,
    viewModel: ProjectViewModel = hiltViewModel()
) {
    // 1. Recolectamos ambos estados
    val uiState by viewModel.uiState.collectAsState()
    val sharedState by viewModel.sharedMenuUiState.uiState.collectAsState()

    // 2. El estado del formulario vive aquí
    var showForm by remember { mutableStateOf(false) }

    // 3. Creamos una lambda única para manejar el clic en un proyecto
    val handleProjectClick = { project: Project ->
        viewModel.setCurrentProject(project)
        onNavigateToProjectManagementScreen()
    }

    Column {
        ProjectTabSelector(
            selectedTab = uiState.tabSelected,
            onTabSelected = { viewModel.onTabSelected(it) })

        // 4. Lógica "Router" limpia y CORRECTA
        if (uiState.tabSelected == ProjectTab.MY_PROJECTS) {
            // Mostramos el layout de "Mis Proyectos"
            MyProjectsLayout(
                uiState = uiState,
                sharedStateUserID = sharedState.userID,
                onShowForm = { showForm = true },
                onProjectClick = handleProjectClick, // Pasamos la lambda
                onNavigateToVersion = onNavigateToVersion,
                viewModel = viewModel
            )
        } else {
            // Mostramos la pantalla de "Colaboraciones"
            CollabScreen(
                projects = uiState.listProjects,
                currentUserId = sharedState.userID,
                onProjectClick = handleProjectClick, // Pasamos la misma lambda
                onNavigateToVersions = onNavigateToVersion,
                onForkClick = { project -> viewModel.forkProject(project) }
            )
        }
    }

    // 5. El diálogo se muestra sobre todo lo demás
    if (showForm) {
        CreateProjectDialog(
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = { showForm = false }
        )
    }
}

