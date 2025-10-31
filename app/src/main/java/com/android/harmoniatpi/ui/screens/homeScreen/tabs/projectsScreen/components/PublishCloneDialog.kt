package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.ui.components.HoloTextField
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.viewmodel.ProjectViewModel
import kotlinx.coroutines.launch

@Composable
fun PublishCloneDialog(
    project: Project, // El clon que queremos publicar
    viewModel: ProjectViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    // Estados para los campos del *nuevo Post*
    var postTitle by remember { mutableStateOf(project.title) } // Título por defecto
    var postMessage by remember { mutableStateOf("") } // Mensaje personal
    var isPublishing by remember { mutableStateOf(false) }

    // Para @mencionar al usuario original
    var originalUserName by remember { mutableStateOf("...") }

    // Buscamos los datos del proyecto original para la mención
    LaunchedEffect(key1 = project) {
        scope.launch {
            try {
                // 1. Buscamos el proyecto original
                val originalProject = viewModel.getProjectByIdUseCase(project.originalProjectId!!)
                // 2. Buscamos al dueño original
                val originalUser = viewModel.buscarporID(originalProject.ownerId)
                originalUserName = originalUser?.userName ?: "usuario original"

                // 3. Pre-llenamos el mensaje
                postMessage = "¡Miren mi versión de '${originalProject.title}' de @${originalUserName}!"

            } catch (e: Exception) {
                postMessage = "¡Miren mi versión de este proyecto!"
            }
        }
    }

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
                    "Publicar Versión",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                // Título del Post
                HoloTextField(
                    value = postTitle,
                    onValueChange = { postTitle = it },
                    label = "Título del Post",
                    leadingIcon = Icons.Default.Create,
                    placeholder = "Escribe un titulo para el post",
                    isError = postTitle.isBlank(),
                )

                // Mensaje personal (Descripción del Post)
                HoloTextField(
                    value = postMessage,
                    onValueChange = { postMessage = it },
                    label = "Mensaje personal",
                    placeholder = "Menciona al creador original...",
                    leadingIcon = Icons.Default.Description,
                    isError = postTitle.isBlank(),
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
                            isPublishing = true

                            // Llamamos a la nueva función en el ViewModel
                            viewModel.publishClonedProject(
                                projectToPublish = project,
                                postTitle = postTitle,
                                postDescription = postMessage,
                                onComplete = {
                                    isPublishing = false
                                    onDismiss()
                                }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        enabled = postTitle.isNotBlank() && !isPublishing
                    ) {
                        if (isPublishing) {
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