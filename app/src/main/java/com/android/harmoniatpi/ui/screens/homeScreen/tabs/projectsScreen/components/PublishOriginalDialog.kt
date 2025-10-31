package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.ui.components.HoloTextField
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.viewmodel.ProjectViewModel

@Composable
fun PublishOriginalDialog(
    project: Project,
    viewModel: ProjectViewModel,
    onDismiss: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val uiState by viewModel.uiState.collectAsState()

    // Estado solo para el título del Post
    var postTitle by remember { mutableStateOf(project.title) }
    val isTitleValid = postTitle.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Publicar Proyecto",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                // Título del Post
                HoloTextField(
                    value = postTitle,
                    onValueChange = { postTitle = it },
                    label = "Título del Post",
                    placeholder = "Elige un título para tu publicación",
                    leadingIcon = Icons.Default.Create,
                    isError = !isTitleValid && postTitle.isNotBlank(),
                    supportingText = if (!isTitleValid) "El título no puede estar vacío" else null
                )

                Row (
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = "Cancelar")
                    }

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            // Llama a la nueva función 'publishProject'
                            viewModel.publishProject(
                                project = project,
                                postTitle = postTitle,
                                onComplete = {
                                    onDismiss() // Cierra el diálogo al completar
                                }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        enabled = isTitleValid && !uiState.isPublishing
                    ) {
                        if (uiState.isPublishing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text("Publicar")
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
    )
}