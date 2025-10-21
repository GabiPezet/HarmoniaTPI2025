package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.model.ProjectTab
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.model.ProjectUiState
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.viewmodel.ProjectViewModel
import androidx.compose.foundation.lazy.items
@Composable
fun MyProjectsLayout(
    projects: List<Project>,
    sharedStateUserID: String,
    onShowForm: () -> Unit,
    onProjectClick: (Project) -> Unit,
    onNavigateToVersion: (Project) -> Unit,
    viewModel: ProjectViewModel,
    onEditClick: (Project) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (projects.isEmpty()) {
                Text(
                    text = "Todavía no has creado ningún proyecto",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(projects) { project ->
                        ProjectCard(
                            project = project,
                            selectedTab = ProjectTab.MY_PROJECTS,
                            currentUserId = sharedStateUserID,
                            onClick = { onProjectClick(project) },
                            onNavigateToVersions = { onNavigateToVersion(project) },
                            onDeleteClick = { id -> viewModel.deleteProject(id) },
                            onForkClick = { proj -> viewModel.cloneProject(proj) }, // Llama a 'cloneProject'
                            onEditClick = onEditClick, // PASA EL PARÁMETRO
                            onPublishClick = { proj -> viewModel.publishProject(proj) }
                        )
                    }
                }
            }
        }

        // Floating Action Button para crear un nuevo proyecto
        FloatingActionButton(
            onClick = onShowForm,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nuevo proyecto")
        }
    }
}