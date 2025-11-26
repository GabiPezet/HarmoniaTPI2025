package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.android.harmoniatpi.R
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.ui.components.ShowConfirmationDialog
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
    val context = LocalContext.current
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
    //dialogo borrado
    var projectToDelete by remember { mutableStateOf<Project?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.stopPlayback()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    Box(modifier = Modifier.fillMaxSize().testTag("ProjectsScreen")) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = bottomPadding + 80.dp
            ) // 80.dp aprox para FAB + margen
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
                        modifier = Modifier.padding(top = 48.dp).testTag("EmptyListMessage")
                    )
                }
            } else {
                items(listToShow) { project ->
                    val index = listToShow.indexOf(project)
                    val isCurrentlyPlaying = uiState.currentlyPlayingProject?.id == project.id
                    val isPreviewLoading = uiState.isPreviewLoading && isCurrentlyPlaying
                    val forkedByUsers = remember(project.forkedByUserIds, uiState.allUsers) {
                        project.forkedByUserIds
                            .mapNotNull { userId -> viewModel.buscarporID(userId) }
                            .filter { it.userID != project.ownerId } // Filtramos al dueño
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.testTag("POST_ITEM_$index")){
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
                            onEditClick = {
                                viewModel.stopPlayback()
                                projectToEdit = project
                            },
                            onDeleteClick = {
                                viewModel.stopPlayback()
                                projectToDelete = project
                            },
                            onPublishClick = {
                                viewModel.stopPlayback()
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
                            onShareClick = {
                                if (project.isPublished) {
                                    shareProject(context, project)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Debes publicar el proyecto para poder compartirlo.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            }
        } // Fin del LazyColumn

        // 3. Botón "Crear" (FAB)
        if (uiState.tabSelected == ProjectTab.MY_PROJECTS) {
            FloatingActionButton(
                onClick = {
                    viewModel.stopPlayback()
                    showCreateForm = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .padding(bottom = bottomPadding + 8.dp).testTag("AddProjectButton"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_new_project_add), // nuevo png de add project
                    contentDescription = "Nuevo Proyecto",
                    modifier = Modifier.size(28.dp),
                    colorFilter = ColorFilter.tint(Color.White) // verificar si dejamos el add en blanco
                )
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
            onNavigateToManagement = {
                onNavigateToProjectManagementScreen()
            }
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

    if (projectToDelete != null) {
        ShowConfirmationDialog(
            modifier = Modifier.testTag("DeleteProjectDialog"),
            show = true,
            onDismiss = { projectToDelete = null },
            onConfirm = {
                projectToDelete?.let {
                    viewModel.deleteProject(it.id)
                }
                projectToDelete = null
            },
            title = "Eliminar Proyecto",
            message = if (projectToDelete?.isPublished == true)
                "Este proyecto está publicado. Si lo eliminas, también se borrará la publicación en la Comunidad y no podrá recuperarse. ¿Estás seguro?"
            else
                "Se eliminará este proyecto de tu dispositivo. Esta acción no se puede deshacer. ¿Estás seguro?",
            confirmText = "Eliminar"
        )
    }
}

fun shareProject(context: Context, project: Project) {
    val shareText = buildString {
        append("¡Escucha mi proyecto '${project.title}' en HoloJam!\n\n")
        append("Creado por: ${project.name} ${project.lastName}\n")
        if (!project.description.isNullOrBlank()) {
            append("${project.description}\n\n")
        }
        if (!project.urlCompleteAudio.isNullOrBlank()) {
            append(project.urlCompleteAudio)
        }
    }

    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Compartir proyecto vía...")
    context.startActivity(shareIntent)
}

