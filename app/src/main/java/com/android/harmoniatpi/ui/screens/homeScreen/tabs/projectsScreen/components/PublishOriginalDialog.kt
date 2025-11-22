package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.domain.model.project.CloningAccess
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.viewmodel.ProjectViewModel


@Composable
fun PublishOriginalDialog(
    project: Project,
    viewModel: ProjectViewModel,
    onDismiss: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val sharedUiState by viewModel.sharedMenuUiState.uiState.collectAsState()

    var postTitle by remember { mutableStateOf(project.title) }
    var postDescription by remember { mutableStateOf(project.description) }
    var postHashtags by remember { mutableStateOf(project.hashtags.joinToString(", ")) }
    var postImageUrl by remember(project) { mutableStateOf(project.imageUrl) }
    var isPublishing by remember { mutableStateOf(false) }
    var cloningAccess by remember { mutableStateOf(CloningAccess.PUBLIC) }
    BasePublishDialog(
        dialogTitle = "Publicar Proyecto",
        isPublishing = isPublishing,
        isPublishButtonEnabled = postTitle.isNotBlank(),
        onDismissRequest = onDismiss,
        onPublishClick = {
            keyboardController?.hide()
            isPublishing = true
            viewModel.publishProject(
                project = project,
                postTitle = postTitle,
                postDescription = postDescription,
                postHashtags = postHashtags,
                postImageUrl = postImageUrl,
                cloningAccess = cloningAccess,
                onComplete = {
                    isPublishing = false
                    onDismiss()
                }
            )
        }
    ) {
        // --- Contenido Específico para el Diálogo Original ---
        PostEditor(
            ownerName = sharedUiState.userName,
            postImageUrl = postImageUrl,
            postHashtags = postHashtags,
            onImageUrlChange = { postImageUrl = it },
            onHashtagsChange = { postHashtags = it }
        ) {
            // Contenido específico que va dentro del editor: Título y Descripción
            PostEditorTextField(
                value = postTitle,
                onValueChange = { postTitle = it },
                placeholder = "Título del Post",
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                singleLine = true
            )
            PostEditorTextField(
                value = postDescription,
                onValueChange = { postDescription = it },
                placeholder = "Describe tu publicación...",
                textStyle = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            CloningAccessSelector(
                selectedOption = cloningAccess,
                onOptionSelected = { cloningAccess = it }
            )
        }
    }
}
