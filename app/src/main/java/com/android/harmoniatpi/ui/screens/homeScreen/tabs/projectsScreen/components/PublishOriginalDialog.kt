package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components

import android.net.Uri
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.components.AudioPlayerSection
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.util.defaultImages
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.viewmodel.ProjectViewModel


@Composable
fun PublishOriginalDialog(
    project: Project,
    viewModel: ProjectViewModel,
    onDismiss: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val uiState by viewModel.uiState.collectAsState()
    val sharedUiState by viewModel.sharedMenuUiState.uiState.collectAsState()
    val context = LocalContext.current

    // --- 1. ESTADO LOCAL (AHORA INCLUYE LA IMAGEN) ---
    var postTitle by remember { mutableStateOf(project.title) }
    var postDescription by remember { mutableStateOf(project.description) }
    var postHashtags by remember { mutableStateOf(project.hashtags.joinToString(", ")) }
    var postImageUrl by remember(project) { mutableStateOf(project.imageUrl) } // <-- ESTADO DE IMAGEN
    val isTitleValid = postTitle.isNotBlank()

    // --- 2. LAUNCHER DE IMÁGENES (DE CreateProjectDialog) ---
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            postImageUrl = uri?.toString() // Actualiza el estado local
        } as (Uri?) -> Unit
    )

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

                // --- CONTENIDO (SCROLLABLE) ---
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Publicar Proyecto",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Text(
                        text = "Edita cómo se verá tu publicación:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    // --- EDITOR WYSIWYG ---
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(horizontal = 12.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                        // --- Título y Descripción (editables) ---
                        PostEditorTextField(
                            value = postTitle,
                            onValueChange = { postTitle = it },
                            placeholder = "Título del Post",
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            singleLine = true
                        )
                        PostEditorTextField(
                            value = postDescription,
                            onValueChange = { postDescription = it },
                            placeholder = "Describe tu publicación...",
                            textStyle = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(8.dp))

                        // --- Sección de Audio (Actualizada) ---
                        // Ahora usa el 'postImageUrl' del estado local
                        AudioPlayerSection(
                            imageUrl = postImageUrl ?: "",
                            ownerName = sharedUiState.userName,
                            isPlaying = false,
                            isPrepared = true,
                            onPlayPauseClicked = {}
                        )

                        // --- 3. SELECCIÓN DE IMAGEN (NUEVO) ---
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Cambiar portada del post",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Text(
                            "O elegir una por defecto:",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        // Usamos la lista 'internal val defaultImages'
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(defaultImages) { drawableRes ->
                                val drawableUri =
                                    "android.resource://${context.packageName}/$drawableRes"
                                AsyncImage(
                                    model = drawableUri,
                                    contentDescription = "Imagen por defecto",
                                    modifier = Modifier
                                        .size(60.dp) // Un poco más pequeño para el diálogo
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            postImageUrl = drawableUri
                                        } // Actualiza el estado
                                        .border(
                                            BorderStroke(
                                                2.dp,
                                                if (postImageUrl == drawableUri) MaterialTheme.colorScheme.primary else Color.Transparent
                                            ), RoundedCornerShape(8.dp)
                                        ),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        // --- FIN DE SELECCIÓN DE IMAGEN ---

                        Spacer(Modifier.height(4.dp))
                        // --- Hashtags (editables) ---
                        PostEditorTextField(
                            value = postHashtags,
                            onValueChange = { postHashtags = it },
                            placeholder = "#música, #creatividad",
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            ),
                            singleLine = true
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // --- BOTONES FIJOS ---
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
                            // --- 4. LLAMADA AL VIEWMODEL (ACTUALIZADA) ---
                            viewModel.publishProject(
                                project = project,
                                postTitle = postTitle,
                                postDescription = postDescription,
                                postHashtags = postHashtags,
                                postImageUrl = postImageUrl,
                                onComplete = {
                                    onDismiss()
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
                            Text(
                                "Publicar",
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