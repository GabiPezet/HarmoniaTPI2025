package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.model.ProjectTab
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme

@Composable
fun CollabScreen(
    projects: List<Project>,
    currentUserId: String,
    onProjectClick: (Project) -> Unit,
    onNavigateToVersions: (Project) -> Unit,
    onForkClick: (Project) -> Unit,
    onEditClick: (Project) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        if (projects.isEmpty()) {
                Text(text = "No hay proyectos para colaborar en este momento.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 32.dp))
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
                        onDeleteClick = onDeleteClick, // No se puede borrar desde aquí
                        onForkClick = onForkClick,
                        onEditClick = onEditClick,
                        onPublishClick = {}
                    )
                }
            }
        }
    }
    }
}