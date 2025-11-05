package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components.BottomMiniPlayer
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components.CreateProjectDialog
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components.EditProjectDialog
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components.EmptyListMessage
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components.ProjectCard
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components.PublishCloneDialog
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components.PublishOriginalDialog
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components.SoundCloudTabRow
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components.UserProfileHeader
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.model.ProjectTab
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    onNavigateToProjectManagementScreen: () -> Unit,
    onNavigateToVersion: (Project) -> Unit,
    viewModel: ProjectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sharedState by viewModel.sharedMenuUiState.uiState.collectAsState()
    var projectToEdit by remember { mutableStateOf<Project?>(null) }
    var showCreateForm by remember { mutableStateOf(false) }
    var projectToPublishAsClone by remember { mutableStateOf<Project?>(null) }
    //Calcula si el mini-reproductor debe mostrarse
    val showMiniPlayer = uiState.currentlyPlayingProject != null
    // Padding inferior dinámico
    val bottomPadding = if (showMiniPlayer) 64.dp else 0.dp
    var projectToPublishAsOriginal by remember { mutableStateOf<Project?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = bottomPadding + 80.dp) // 80.dp aprox para FAB + margen
        ) {
            item {
                Spacer(modifier = Modifier.height(5.dp))
                UserProfileHeader(
                    sharedState = sharedState, // Pasa el estado completo
                    projectsCount = uiState.myProjects.size,
                    clonesCount = uiState.allProjects.size
                )
            }
            stickyHeader {
                SoundCloudTabRow(
                    selectedTab = uiState.tabSelected,
                    onTabSelected = { viewModel.onTabSelected(it) }
                )
            }

            // --- 3. Lista de Proyectos/Clones ---
            val listToShow = if (uiState.tabSelected == ProjectTab.MY_PROJECTS) {
                uiState.myProjects
            } else {
                uiState.allProjects
            }

            if (listToShow.isEmpty()) {
                item {
                    EmptyListMessage(
                        tab = uiState.tabSelected,
                        modifier = Modifier.padding(top = 48.dp)
                    )
                }
            } else {
                items(listToShow) { project ->
                    val isCurrentlyPlaying = uiState.currentlyPlayingProject?.id == project.id
                    val isPreviewLoading = uiState.isPreviewLoading && isCurrentlyPlaying
                    val forkedByUsers = remember(project.forkedByUserIds, uiState.allUsers) {
                        project.forkedByUserIds
                            .mapNotNull { userId -> viewModel.buscarporID(userId) }
                            .filter { it.userID != project.ownerId } // Filtramos al dueño
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ProjectCard(
                        project = project,
                        currentUserId = sharedState.userID,
                        selectedTab = uiState.tabSelected,
                        forkedByUsers = forkedByUsers,
                        onNavigateToManagement = {
                            viewModel.setCurrentProject(project)
                            onNavigateToProjectManagementScreen()
                        },
                        onTogglePlayPause = { viewModel.togglePlayPause(project) },
                        isCurrentlyPlaying = isCurrentlyPlaying,
                        onEditClick = { projectToEdit = project },
                        onDeleteClick = { viewModel.deleteProject(project.id) },
                        onPublishClick = {
                            if (project.originalProjectId != null && project.ownerId == sharedState.userID) {
                                // Es un clon mío, mostrar diálogo de clon
                                projectToPublishAsClone = project
                            } else {
                                // Es un proyecto original, mostrar diálogo original
                                projectToPublishAsOriginal = project
                            }
                        },
                        onNavigateToVersions = { onNavigateToVersion(project) },
                        isPreviewLoading = isPreviewLoading,
                    )
                }
            }
        } // Fin del LazyColumn

        // 3. Botón "Crear" (FAB)
        if (uiState.tabSelected == ProjectTab.MY_PROJECTS) {
            FloatingActionButton(
                onClick = { showCreateForm = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .padding(bottom = bottomPadding + 8.dp), // Lo subimos encima del mini-reproductor
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Proyecto")
            }
        }

        // 4. Mini-Reproductor Fijo
        if (showMiniPlayer) {
            BottomMiniPlayer(
                playingProject = uiState.currentlyPlayingProject,
                onStopClick = { viewModel.stopPlayback() },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    // --- Diálogos ---
    projectToEdit?.let { project ->
        EditProjectDialog(
            project = project,
            viewModel = viewModel,
            onDismiss = { projectToEdit = null }
        )
    }

    if (showCreateForm) {
        CreateProjectDialog(
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = { showCreateForm = false /* viewModel.dismissCreateDialog() */ },
        )
    }

    projectToPublishAsClone?.let { project ->
        PublishCloneDialog(
            project = project,
            viewModel = viewModel,
            onDismiss = { projectToPublishAsClone = null }
        )
    }

    projectToPublishAsOriginal?.let { project ->
        PublishOriginalDialog(
            project = project,
            viewModel = viewModel,
            onDismiss = { projectToPublishAsOriginal = null }
        )
    }
}


