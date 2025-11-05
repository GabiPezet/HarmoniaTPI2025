package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.viewmodel.ProjectViewModel

@Composable
fun EditProjectDialog(
    project: Project,
    viewModel: ProjectViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val uiState by viewModel.uiState.collectAsState()

    // Estado local para los campos editables
    var title by remember(project) { mutableStateOf(project.title) }
    var description by remember(project) { mutableStateOf(project.description) }
    var hashtags by remember(project) { mutableStateOf(project.hashtags.joinToString(", ")) }
    var selectedImageUri by remember(project) { mutableStateOf(project.imageUrl) }
    val isTitleValid = title.isNotBlank()

    BaseProjectDialog(
        dialogTitle = "Editar proyecto",
        confirmButtonText = "Guardar",
        isConfirmEnabled = isTitleValid,
        isLoading = uiState.isLoading,
        onDismiss = onDismiss,
        onConfirm = {
            keyboardController?.hide()
            val updatedProject = project.copy(
                title = title,
                description = description,
                hashtags = hashtags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                imageUrl = selectedImageUri
            )
            viewModel.saveProjectEdits(
                projectToSave = updatedProject,
                onSuccess = {
                    Toast.makeText(context, "Cambios guardados", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                onError = { error ->
                    Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                }
            )
        }
    ) {
        ProjectForm(
            title = title,
            description = description,
            hashtags = hashtags,
            selectedImageUri = selectedImageUri,
            isTitleValid = isTitleValid,
            onTitleChange = { title = it },
            onDescriptionChange = { description = it },
            onHashtagsChange = { hashtags = it },
            onImageSelected = { selectedImageUri = it }
        )
    }
}