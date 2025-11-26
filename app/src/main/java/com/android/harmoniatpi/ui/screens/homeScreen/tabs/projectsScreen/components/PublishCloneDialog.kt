package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.viewmodel.ProjectViewModel
import kotlinx.coroutines.launch


@Composable
fun PublishCloneDialog(
    project: Project,
    viewModel: ProjectViewModel,
    onDismiss: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val sharedUiState by viewModel.sharedMenuUiState.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var postTitle by remember { mutableStateOf(project.title) }
    var personalMessage by remember { mutableStateOf("") }
    var postHashtags by remember { mutableStateOf(project.hashtags.joinToString(", ")) }
    var postImageUrl by remember(project) { mutableStateOf(project.imageUrl) }
    var attributionMessage by remember { mutableStateOf("Cargando atribución...") }
    var isPublishing by remember { mutableStateOf(false) }

    // --- Lógica Específica del Clon: Obtener atribución ---
    LaunchedEffect(key1 = project) {
        scope.launch {
            attributionMessage = try {
                val originalProject = viewModel.getProjectByIdFromFirestoreUseCase(project.originalProjectId!!)
                val originalUser = viewModel.getUserOnFirebaseByIDUseCase(originalProject?.ownerId ?: "original")
                "¡Miren mi versión de '${originalProject?.title}' de @${originalUser?.userName ?: "usuario"}!"
            } catch (e: Exception) {
                "¡Miren mi versión de este proyecto!"
            }
        }
    }

    BasePublishDialog(
        dialogTitle = "Publicar Versión",
        isPublishing = isPublishing,
        // Habilita solo si hay título y contenido válido
        isPublishButtonEnabled = postTitle.isNotBlank(),
        onDismissRequest = onDismiss,
        onPublishClick = {
            keyboardController?.hide()
            isPublishing = true
            // Combinamos el mensaje de atribución con el mensaje personal para la descripción del post
            val finalDescription = "$attributionMessage\n${personalMessage.trim()}"

            viewModel.publishClonedProject(
                project = project,
                postTitle = postTitle,
                postDescription = finalDescription,
                postHashtags = postHashtags,
                postImageUrl = postImageUrl,
                onComplete = {
                    isPublishing = false
                    onDismiss()
                }
            )
        }
    ) {
        // --- Contenido Específico para el Diálogo de Clon ---
        PostEditor(
            ownerName = sharedUiState.userName,
            postImageUrl = postImageUrl,
            postHashtags = postHashtags,
            onImageUrlChange = { postImageUrl = it },
            onHashtagsChange = { postHashtags = it }
        ) {
            // Contenido específico que va dentro del editor: Título, Atribución y Mensaje
            PostEditorTextField(
                value = postTitle,
                onValueChange = { postTitle = it },
                placeholder = "Título del Post",
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                singleLine = true
            )
            Text(
                text = attributionMessage,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )
            PostEditorTextField(
                value = personalMessage,
                onValueChange = { personalMessage = it },
                placeholder = "Añade un comentario (ej. '¡Le agregué un bajo!')",
                textStyle = MaterialTheme.typography.bodyMedium
            )
        }
    }
}