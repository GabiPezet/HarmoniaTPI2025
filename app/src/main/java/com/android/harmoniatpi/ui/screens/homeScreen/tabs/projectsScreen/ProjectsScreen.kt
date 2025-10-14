package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.ui.components.HarmoniaTextField
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components.CollabScreen
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components.ProjectCard
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
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var showForm by remember { mutableStateOf(false) }

    Column {
        ProjectTabSelector(
            selectedTab = uiState.tabSelected,
            onTabSelected = { viewModel.onTabSelected(it) })
        if (uiState.tabSelected == ProjectTab.MY_PROJECTS) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 🔹 Lista de proyectos
                    if (uiState.listProjects.isEmpty()) {
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
                            items(uiState.listProjects, key = { it.id }) { project ->
                                ProjectCard(
                                    project = project,
                                    onClick = {
                                        viewModel.setCurrentProject(project)
                                        onNavigateToProjectManagementScreen()
                                    },
                                    onNavigateToVersions = { onNavigateToVersion(project) },
                                    onDeleteClick = { id -> viewModel.deleteProject(id) }
                                )
                            }
                        }
                    }
                }

                // 🔹 Floating Action Button
                FloatingActionButton(
                    onClick = { showForm = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo proyecto")
                }

                // 🔹 AlertDialog para crear nuevo proyecto
                if (showForm) {
                    AlertDialog(
                        onDismissRequest = { showForm = false },
                        confirmButton = {},
                        text = {
                            Column(
                                modifier = Modifier
                                    .verticalScroll(rememberScrollState())
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "Crear nuevo proyecto",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )

                                HarmoniaTextField(
                                    value = uiState.title,
                                    onValueChange = viewModel::onTitleChange,
                                    label = "Título",
                                    placeholder = "Ej. Mi primer proyecto",
                                    leadingIcon = Icons.Default.Create,
                                    isError = !uiState.isTitleValid && uiState.title.isNotBlank(),
                                    supportingText = if (!uiState.isTitleValid && uiState.title.isNotBlank()) {
                                        "El título no puede estar vacío"
                                    } else null
                                )

                                HarmoniaTextField(
                                    value = uiState.description,
                                    onValueChange = viewModel::onDescriptionChange,
                                    label = "Descripción",
                                    placeholder = "Describe tu proyecto",
                                    leadingIcon = Icons.Default.Description
                                )

                                HarmoniaTextField(
                                    value = uiState.hashtags,
                                    onValueChange = viewModel::onHashtagsChange,
                                    label = "Hashtags",
                                    placeholder = "#música, #creatividad",
                                    leadingIcon = Icons.Default.Tag
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = {
                                            keyboardController?.hide()
                                            viewModel.saveProject(
                                                onSuccess = {
                                                    Toast.makeText(
                                                        context,
                                                        "Proyecto guardado",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    showForm = false
                                                },
                                                onError = { error ->
                                                    Toast.makeText(
                                                        context,
                                                        "Error: $error",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(16.dp),
                                        enabled = uiState.isFormValid && !uiState.isLoading,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(
                                                alpha = 0.5f
                                            )
                                        )
                                    ) {
                                        if (uiState.isLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = MaterialTheme.colorScheme.secondary,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Text(
                                                "Guardar",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold, fontSize = 16.sp
                                                ),
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { showForm = false },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Text(
                                            text = "Cancelar",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                }
            }
        } else {
            CollabScreen()
        }
    }


}


