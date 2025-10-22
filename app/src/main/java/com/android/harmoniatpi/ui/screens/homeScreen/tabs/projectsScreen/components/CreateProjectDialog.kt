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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.harmoniatpi.ui.components.HoloTextField
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
                    "Crear nuevo proyecto",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                // Asumo que HarmoniaTextField es un Composable tuyo
                HoloTextField(
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

                HoloTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = "Descripción",
                    placeholder = "Describe tu proyecto",
                    leadingIcon = Icons.Default.Description
                )

                HoloTextField(
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

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Cancelar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

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
                                    onDismiss()
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
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Guardar",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold, fontSize = 16.sp
                                ),
                            )
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
    )
}