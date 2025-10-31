package com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.android.harmoniatpi.domain.model.userPreferences.Post

@OptIn(ExperimentalLayoutApi::class)
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable { /* onPostClicked(post.id) */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp)
        ) {
            // --- Columna del Avatar ---
            Box(
                modifier = Modifier.padding(top = 4.dp)
            ) {
                if (post.userImagePathURL.isNotBlank()) {
                    AsyncImage(
                        model = post.userImagePathURL,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Foto de perfil",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // --- Columna de Contenido (Header, Body, Actions) ---
            Column(
                modifier = Modifier.weight(1f)
            ) {

                // --- Cabecera (Nombre, Handle, Botón Borrar) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${post.name} ${post.lasName}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "@${post.name.lowercase()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Spacer(Modifier.weight(1f))

                    if (isMyPost) {
                        IconButton(
                            onClick = onDeleteClicked,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Borrar post",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // --- Cuerpo del Post (Título + Descripción) ---
                Column(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(post.title)
                            }
                            append("\n")
                            append(post.description)
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (post.hashtags.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            post.hashtags.forEach {
                                Text(
                                    text = "#$it",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable { }
                                )
                            }
                        }
                    }
                }

                // --- Barra de Acciones (Estilo X) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val defaultTint = MaterialTheme.colorScheme.onSurfaceVariant

                    // 1. Comentarios
                    PostActionItem(
                        icon = Icons.AutoMirrored.Outlined.Comment,
                        text = post.comments.size.toString(),
                        onClick = onCommentClicked,
                        enabled = true
                    )

                    Spacer(Modifier.width(24.dp)) // <-- Spacer fijo

                    // 2. Clonar
                    if (post.idProject.isNotBlank() && post.clonedOption == true) {
                        val isCloneEnabled = !isAlreadyCloned && !isMyPost
                        val (cloneIcon, cloneTint) = if (isAlreadyCloned) {
                            Icons.Default.Check to MaterialTheme.colorScheme.primary
                        } else {
                            Icons.Default.ContentCopy to defaultTint
                        }

                        PostActionItem(
                            icon = cloneIcon,
                            text = "",
                            onClick = onCloneClicked,
                            enabled = isCloneEnabled
                        )

                        Spacer(Modifier.width(24.dp)) // <-- Spacer fijo
                    }

                    // 3. Like
                    val (likeIcon, likeTint) = if (post.likes > 0) {
                        Icons.Filled.Favorite to Color.Red
                    } else {
                        Icons.Outlined.Favorite to defaultTint
                    }
                    PostActionItem(
                        icon = likeIcon,
                        text = post.likes.toString(),
                        onClick = onLikeClicked,
                        enabled = true
                    )

                    Spacer(Modifier.width(24.dp)) // <-- Spacer fijo

                    // 4. Icono de Share (placeholder)
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Compartir",
                        tint = defaultTint,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { /* Lógica para compartir */ }
                    )
                }
            }
        }

        // Divisor entre posts
        Divider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            thickness = 1.dp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}