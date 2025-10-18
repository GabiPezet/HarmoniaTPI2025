package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.model.ProjectTab
import androidx.compose.foundation.lazy.items
@Composable
fun CollabScreen(
    projects: List<Project>,
    currentUserId: String,
    onProjectClick: (Project) -> Unit,
    onNavigateToVersions: (Project) -> Unit,
    onForkClick: (Project) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (projects.isEmpty()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("No hay proyectos para colaborar en este momento.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(projects) { project ->
                    ProjectCard(
                        project = project,
                        selectedTab = ProjectTab.COLLABS,
                        currentUserId = currentUserId,
                        onClick = { onProjectClick(project) },
                        onNavigateToVersions = { onNavigateToVersions(project) },
                        onDeleteClick = {}, // No se puede borrar desde aquí
                        onForkClick = onForkClick
                    )
                }
            }
        }
    }
}