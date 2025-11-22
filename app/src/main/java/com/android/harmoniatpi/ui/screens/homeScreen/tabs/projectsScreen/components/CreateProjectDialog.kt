package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.model.ProjectUiState
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.viewmodel.ProjectViewModel

@Composable
fun CreateProjectDialog(
    uiState: ProjectUiState,
    viewModel: ProjectViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    BaseProjectDialog(
        dialogTitle = "Crear nuevo proyecto",
        confirmButtonText = "Crear",
        isConfirmEnabled = uiState.isFormValid,
        isLoading = uiState.isLoading,
        onDismiss = onDismiss,
        onConfirm = {
            keyboardController?.hide()
            viewModel.saveProject(
                onSuccess = {
                    Toast.makeText(context, "Proyecto guardado", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                onError = { error ->
                    Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                }
            )
        }
    ) {
        ProjectForm(
            title = uiState.title,
            description = uiState.description,
            hashtags = uiState.hashtags,
            selectedImageUri = uiState.selectedImageUri,
            isTitleValid = uiState.isTitleValid,
            onTitleChange = viewModel::onTitleChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onHashtagsChange = viewModel::onHashtagsChange,
            onImageSelected = viewModel::onImageSelected
        )
    }
}