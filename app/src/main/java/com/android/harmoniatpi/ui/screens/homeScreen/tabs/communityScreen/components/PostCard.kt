package com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.harmoniatpi.domain.model.userPreferences.Post

@Composable
fun PostCard(
    post: Post,
    onLikeClicked: () -> Unit,
    onCommentClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    isMyPost: Boolean,
    isAlreadyCloned: Boolean,
    onCloneClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            // Header con información del usuario y opción de borrar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (post.userImagePathURL.isNotBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(post.userImagePathURL),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Foto de perfil",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Text("${post.name} ${post.lasName}", fontWeight = FontWeight.Bold)

                if (isMyPost) {
                    IconButton(
                        onClick = onDeleteClicked,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Borrar post",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(post.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(post.description)

            if (post.hashtags.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    post.hashtags.forEach {
                        AssistChip(onClick = {}, label = { Text("#$it") })
                    }
                }
            }
            // AÑADE EL BOTÓN DE CLONAR
            if (post.idProject.isNotBlank() && !isMyPost) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onCloneClicked,
                    enabled = !isAlreadyCloned,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        imageVector = if (isAlreadyCloned) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Clonar",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(if (isAlreadyCloned) "CLONADO" else "CLONAR")
                }
            }

            // Footer con acciones (like y comentarios)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLikeClicked() }
                ) {
                    Icon(
                        if (post.likes > 0) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                        contentDescription = "Like",
                        tint = if (post.likes > 0) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        post.likes.toString(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.width(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onCommentClicked() }
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.Comment,
                        contentDescription = "Comentarios"
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        post.comments.size.toString(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                // Muestra el contador de clones (usando 'totalShared')
                Spacer(Modifier.width(16.dp))
                if (post.totalShared > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ContentCopy, // O un ícono de "fork"
                            contentDescription = "Clones"
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            post.totalShared.toString(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}