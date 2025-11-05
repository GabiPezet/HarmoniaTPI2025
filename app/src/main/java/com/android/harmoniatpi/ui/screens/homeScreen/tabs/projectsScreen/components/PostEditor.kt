package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components

// /components/PostEditor.kt


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
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.components.AudioPlayerSection
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.util.defaultImages

/**
 * Componente que encapsula el formulario de edición de una publicación.
 * @param ownerName El nombre del dueño del post (para el AudioPlayer).
 * @param postImageUrl La URL de la imagen de portada.
 * @param postHashtags Los hashtags del post.
 * @param onImageUrlChange Callback para cuando la imagen cambia.
 * @param onHashtagsChange Callback para cuando los hashtags cambian.
 * @param content Slot para los campos de texto que varían (título, descripción, atribución, etc.).
 */
@Composable
fun PostEditor(
    ownerName: String,
    postImageUrl: String?,
    postHashtags: String,
    onImageUrlChange: (String) -> Unit,
    onHashtagsChange: (String) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            uri?.let { onImageUrlChange(it.toString()) }
        }
    )

    Text(
        text = "Edita cómo se verá tu publicación:",
        style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Slot para Título, Descripción, Atribución...
        content()

        Spacer(Modifier.height(8.dp))

        // --- Sección de Audio (Compartida) ---
        AudioPlayerSection(
            imageUrl = postImageUrl ?: "",
            ownerName = ownerName,
            isPlaying = false,
            isPrepared = true,
            onPlayPauseClicked = {}
        )

        // --- Selección de Imagen (Compartida) ---
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = {
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Cambiar portada", style = MaterialTheme.typography.bodySmall)
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
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onImageUrlChange(drawableUri) }
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

        Spacer(Modifier.height(4.dp))

        // --- Hashtags (Compartido) ---
        PostEditorTextField(
            value = postHashtags,
            onValueChange = onHashtagsChange,
            placeholder = "#música, #creatividad",
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            ),
            singleLine = true
        )
    }
}