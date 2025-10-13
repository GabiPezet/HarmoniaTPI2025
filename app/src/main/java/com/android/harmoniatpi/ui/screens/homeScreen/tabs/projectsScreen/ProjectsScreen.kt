package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components.ProjectCard
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components.ProjectTabSelector
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.model.ProjectTab
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.viewmodel.ProjectListViewModel

@Composable
fun ProjectsScreen(
    onNavigateToProjectDetail: () -> Unit,
    onNavigateToCreateProjet: () -> Unit,
    onNavigateToVersion: () -> Unit,
    viewModel: ProjectListViewModel = hiltViewModel()
) {
    val projects by viewModel.projects.collectAsState()
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateProjet,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo proyecto")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ProjectTabSelector(
                selectedTab = ProjectTab.MY_PROJECTS,
                onTabSelected = {}
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(projects) { project ->
                    ProjectCard(
                        project = project,
                        onClick = onNavigateToProjectDetail,
                        onNavigateToVersions = onNavigateToVersion
                    )
                }
            }
        }
    }
}


