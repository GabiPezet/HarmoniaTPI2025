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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.android.harmoniatpi.R
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.util.defaultImages

/**
 * Formulario reutilizable para crear o editar un proyecto.
 * Contiene los campos de texto y el selector de imágenes.
 */
@Composable
fun ProjectForm(
    title: String,
    description: String,
    hashtags: String,
    selectedImageUri: String?,
    isTitleValid: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onHashtagsChange: (String) -> Unit,
    onImageSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            uri?.let { onImageSelected(it.toString()) }
        }
    )

    // --- INPUTS MODERNOS ---
    ModernProjectInputRow(
        modifier = Modifier.testTag("ProjectTitleInput"),
        labelText = "Nombre del proyecto o canción",
        value = title,
        onValueChange = onTitleChange,
        placeholderText = "Ej. Mi primer proyecto",
        icon = Icons.Default.Create,
        singleLine = true,
        isError = !isTitleValid && title.isNotEmpty(),
        supportingText = if (!isTitleValid && title.isNotEmpty()) "El título no puede estar vacío" else null
    )

    ModernProjectInputRow(
        modifier = Modifier.testTag("ProjectDescriptionInput"),
        labelText = "Descripción (Opcional)",
        value = description,
        onValueChange = onDescriptionChange,
        placeholderText = "Describe tu proyecto",
        icon = Icons.Default.Description
    )

    ModernProjectInputRow(
        modifier = Modifier.testTag("ProjectHashtagsInput"),
        labelText = "Hashtags (Opcional)",
        value = hashtags,
        onValueChange = onHashtagsChange,
        placeholderText = "#música, #creatividad",
        icon = Icons.Default.Tag,
        singleLine = true
    )

    // --- SECCIÓN DE IMAGEN ---
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
        model = selectedImageUri,
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
        items(defaultImages) { drawableRes ->
            val drawableUri = "android.resource://${context.packageName}/$drawableRes"
            AsyncImage(
                model = drawableUri,
                contentDescription = "Imagen por defecto",
                modifier = Modifier
                    .size(80.dp)
                    .testTag("DefaultImage_$drawableRes")
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .clickable { onImageSelected(drawableUri) }
                    .border(
                        BorderStroke(
                            2.dp,
                            if (selectedImageUri == drawableUri) MaterialTheme.colorScheme.primary else Color.Transparent
                        ), androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ),
                contentScale = ContentScale.Crop
            )
        }
    }
}