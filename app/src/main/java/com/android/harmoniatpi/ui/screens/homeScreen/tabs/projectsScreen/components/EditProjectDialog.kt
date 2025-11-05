package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.android.harmoniatpi.R
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.util.defaultImages
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.viewmodel.ProjectViewModel

@Composable
fun EditProjectDialog(
    project: Project, // El proyecto a editar
    viewModel: ProjectViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val uiState by viewModel.uiState.collectAsState()

    // --- 1. ESTADO LOCAL PARA LA EDICIÓN ---
    // Pre-cargado con los datos del proyecto
    var title by remember(project) { mutableStateOf(project.title) }
    var description by remember(project) { mutableStateOf(project.description) }
    var hashtags by remember(project) { mutableStateOf(project.hashtags.joinToString(", ")) }
    var selectedImageUri by remember(project) { mutableStateOf(project.imageUrl) } // <-- NUEVO ESTADO
    val isTitleValid = title.isNotBlank()

    // --- 2. LAUNCHER PARA IMÁGENES (igual que en Create) ---
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            // Actualiza el estado local
            selectedImageUri = uri?.toString()
        }
    )

    // --- 3. CONTENEDOR MODERNO (igual que en Create) ---
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // --- 4. COLUMNA DE CONTENIDO (SCROLLABLE) ---
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Editar proyecto", // <-- Título cambiado
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    // --- 5. INPUTS MODERNOS ---
                    ModernProjectInputRow(
                        labelText = "Nombre del proyecto o canción",
                        value = title,
                        onValueChange = { title = it }, // Actualiza estado local
                        placeholderText = "Ej. Mi primer proyecto",
                        icon = Icons.Default.Create,
                        singleLine = true,
                        isError = !isTitleValid && title.isNotBlank(),
                        supportingText = if (!isTitleValid && title.isNotBlank()) {
                            "El título no puede estar vacío"
                        } else null
                    )

                    ModernProjectInputRow(
                        labelText = "Descripción (Opcional)",
                        value = description,
                        onValueChange = { description = it }, // Actualiza estado local
                        placeholderText = "Describe tu proyecto",
                        icon = Icons.Default.Description
                    )

                    ModernProjectInputRow(
                        labelText = "Hashtags (Opcional)",
                        value = hashtags,
                        onValueChange = { hashtags = it }, // Actualiza estado local
                        placeholderText = "#música, #creatividad",
                        icon = Icons.Default.Tag,
                        singleLine = true
                    )

                    // --- 6. SECCIÓN DE IMAGEN (NUEVA) ---
                    Text(
                        text = "Elige una portada para tu proyecto",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    AsyncImage(
                        model = selectedImageUri, // <-- Lee del estado local
                        contentDescription = "Vista previa de la portada",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop,
                        placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                        error = painterResource(id = R.drawable.portada_proyecto_error),
                    )

                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Seleccionar desde Galería")
                    }

                    Text(
                        "O elegir una por defecto:",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // --- Usa la lista 'internal' que refactorizamos ---
                        items(defaultImages) { drawableRes ->
                            val drawableUri = "android.resource://${context.packageName}/$drawableRes"
                            AsyncImage(
                                model = drawableUri,
                                contentDescription = "Imagen por defecto",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedImageUri = drawableUri } // Actualiza estado local
                                    .border(
                                        BorderStroke(
                                            2.dp,
                                            if (selectedImageUri == drawableUri) MaterialTheme.colorScheme.primary else Color.Transparent
                                        ), RoundedCornerShape(8.dp)
                                    ),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                } // --- Fin de la columna scrollable ---

                Spacer(Modifier.height(16.dp))

                // --- 7. BOTONES FIJOS ---
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
                            // --- 8. LÓGICA DE GUARDADO (ACTUALIZADA) ---
                            // Creamos el proyecto actualizado desde el estado local
                            val updatedProject = project.copy(
                                title = title,
                                description = description,
                                hashtags = hashtags.split(",").map { it.trim() },
                                imageUrl = selectedImageUri // <-- Añade la imagen
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
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        enabled = isTitleValid && !uiState.isLoading,
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
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
        }
    }
}