package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.ui.components.HoloTextField
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.viewmodel.ProjectViewModel

@Composable
fun EditProjectDialog(
    project: Project, // El proyecto a editar
    viewModel: ProjectViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Estado local para la edición, pre-cargado con los datos del proyecto
    var title by remember (project) { mutableStateOf(project.title) }
    var description by remember(project) { mutableStateOf(project.description) }
    var hashtags by remember(project) { mutableStateOf(project.hashtags.joinToString(", ")) }

    val isTitleValid = title.isNotBlank()
    val uiState by viewModel.uiState.collectAsState()

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
                    "Editar proyecto",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                HoloTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Título",
                    placeholder = "Título",
                    leadingIcon = Icons.Default.Create,
                    isError = !isTitleValid && title.isNotBlank(),
                    supportingText = if (!isTitleValid && title.isNotBlank()) "El título no puede estar vacío" else null
                )

                HoloTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Descripción",
                    placeholder = "Describe tu proyecto",
                    leadingIcon = Icons.Default.Description
                )

                HoloTextField(
                    value = hashtags,
                    onValueChange = { hashtags = it },
                    label = "Hashtags",
                    placeholder = "#música, #creatividad",
                    leadingIcon = Icons.Default.Tag
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
                            // Creamos el proyecto actualizado
                            val updatedProject = project.copy(
                                title = title,
                                description = description,
                                hashtags = hashtags.split(",").map { it.trim() }
                            )

                            // Llamamos a la nueva función de guardado
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
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        enabled = isTitleValid && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text("Guardar")
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
    )
}