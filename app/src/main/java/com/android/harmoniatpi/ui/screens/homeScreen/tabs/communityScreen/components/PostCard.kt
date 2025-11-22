package com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.components

import android.util.Log
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import com.android.harmoniatpi.R
import com.android.harmoniatpi.domain.model.userPreferences.Post
import com.android.harmoniatpi.ui.components.ShowConfirmationDialog
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.util.sharePost
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostCard(
    post: Post,
    userName: String,
    userLastName: String,
    onLikeClicked: () -> Unit,
    onCommentClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    isMyPost: Boolean,
    isAlreadyCloned: Boolean,
    onCloneClicked: () -> Unit,
    viewUserProfile: (String) -> Unit,
    isCloningThisPost: Boolean,
    isFriend: Boolean
) {
    val postAudio = post.urlCompleteAudio
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var isPrepared by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val formattedDate = remember(post.createdAt) {
        formatPostDateToHoursAgo(post.createdAt)
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(postAudio.toUri()))
            prepare()
        }
    }

    if (showDeleteDialog) {
        ShowConfirmationDialog(
            show = true,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                onDeleteClicked()
                showDeleteDialog = false
            },
            title = "Borrar Post",
            message = "Si eliminas el Post, no podrás recuperarlo.",
            confirmText = "Borrar"
        )
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying = isPlayingNow
            }

            override fun onPlaybackStateChanged(state: Int) {
                isPrepared = state == Player.STATE_READY
                if (state == Player.STATE_ENDED) {
                    exoPlayer.seekTo(0)
                    exoPlayer.pause()
                    isPlaying = false
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Column(
        modifier = if (isMyPost) {
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = LocalIndication.current,
                    onClick = { },
                    onLongClick = { showDeleteDialog = true }
                )
        } else {
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable {
                        viewUserProfile(post.userID)
                    }
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

            Column(
                modifier = Modifier.weight(1f)
            ) {

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
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 4.dp),
                        text = formattedDate,
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

                    // --- Sección de Audio si está disponible ---
                    if (postAudio.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Log.d("Post", "$post PostImage: ${post.imageUrl}")
                        AudioPlayerSection(
                            ownerName = post.name,
                            isPlaying = isPlaying,
                            isPrepared = isPrepared,
                            imageUrl = post.imageUrl,
                            onPlayPauseClicked = {
                                if (isPrepared) {
                                    if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                                }
                            }
                        )
                    }

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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val defaultTint = MaterialTheme.colorScheme.onSurfaceVariant

                    Spacer(modifier = Modifier.weight(0.2f))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.6f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            // 1. Comentarios
                            PostActionItem(
                                icon = Icons.AutoMirrored.Outlined.Comment,
                                text = post.comments.size.toString(),
                                totalCloned = post.comments.size,
                                onClick = onCommentClicked,
                                enabled = true
                            )


                            // 2. Clonar
                            if (post.idProject.isNotBlank() && post.clonedOption == true && isFriend) {
                                val isCloneEnabled = !isAlreadyCloned && !isMyPost && !isCloningThisPost
                                if (isCloningThisPost) {
                                    Box(modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp)) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                } else {
                                // Muestra el botón de clonar
                                val (cloneIcon) = if (isAlreadyCloned) {
                                    Icons.Default.Check to MaterialTheme.colorScheme.primary
                                } else {
                                    Icons.Default.ContentCopy to defaultTint
                                }

                                PostActionItem(
                                    icon = cloneIcon,
                                    totalCloned = post.totalShared,
                                    text = "",
                                    onClick = onCloneClicked,
                                    enabled = isCloneEnabled
                                )
                            }

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
                                totalCloned = post.likes,
                                onClick = onLikeClicked,
                                enabled = true
                            )

                            // 4. Icono de Share (placeholder)
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Compartir",
                                tint = defaultTint,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { sharePost(post, context , isMyPost,userName,userLastName) }
                            )

                        }

                    }
                }


            }
        }
    }

    HorizontalDivider(
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        thickness = 1.dp,
        modifier = Modifier.padding(top = 4.dp)
    )
}


@Composable
fun AudioPlayerSection(
    imageUrl: String,
    ownerName: String,
    isPlaying: Boolean,
    isPrepared: Boolean,
    onPlayPauseClicked: () -> Unit
) {
    // --- 3. Cambia Surface por Card y Box ---
    // Card nos da un contenedor fácil de recortar
    Card(
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        // Box nos permite apilar la imagen, el scrim y el contenido
        Box(
            // Dale una altura fija para que todas las portadas se vean igual
            modifier = Modifier.height(90.dp)
        ) {

            // --- 4. IMAGEN DE FONDO ---
            val placeholderColor = MaterialTheme.colorScheme.surfaceVariant
            AsyncImage(
                model = imageUrl,
                placeholder = ColorPainter(placeholderColor),
                error = painterResource(id = R.drawable.portada_proyecto_error), // Tu imagen de error
                contentDescription = "Portada del proyecto",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // --- 5. SCRIM (CAPA OSCURA) ---
            // Esto asegura que el texto blanco sea legible encima de cualquier imagen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )

            // --- 6. CONTENIDO (Tu Row original, con colores ajustados) ---
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPlayPauseClicked,
                    modifier = Modifier.size(36.dp),
                    enabled = isPrepared
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                        // --- CAMBIO DE COLOR ---
                        tint = if (isPrepared) Color.White else Color.White.copy(alpha = 0.5f)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Proyecto HoloJam de $ownerName",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White // --- CAMBIO DE COLOR ---
                    )
                    Text(
                        text = when {
                            !isPrepared -> "Preparando audio..."
                            isPlaying -> "Reproduciendo..."
                            else -> "Reproducir audio"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f) // --- CAMBIO DE COLOR ---
                    )
                }

                if (!isPrepared) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White // --- CAMBIO DE COLOR ---
                    )
                } else if (isPlaying) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Pasa el color blanco a la animación
                        SoundWaveAnimation(color = Color.White) // <-- MODIFICADO
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Sonando",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White // --- CAMBIO DE COLOR ---
                        )
                    }
                }
            }
        }
    }
}
// Animación simple de ondas de sonido
@Composable
fun SoundWaveAnimation(
    color: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(3) { index ->
            val animatedHeight by animateFloatAsState(
                targetValue = if (true) (8 + index * 4).toFloat() else 4f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 800
                        4f at 0
                        (8 + index * 4).toFloat() at 200
                        4f at 400
                    },
                    repeatMode = RepeatMode.Reverse
                )
            )
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(animatedHeight.dp)
                    .background(color)
            )
        }
    }
}

private fun formatPostDateToHoursAgo(createdAt: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
        val postDate = inputFormat.parse(createdAt) ?: return "Fecha no disponible"
        val now = Date()

        val diffInMillis = now.time - postDate.time
        val minutesAgo = diffInMillis / (1000 * 60) // Convertir a minutos
        val hoursAgo = minutesAgo / 60
        val daysAgo = hoursAgo / 24

        when {
            daysAgo >= 1 -> "$daysAgo día" + if (daysAgo > 1) "s" else ""
            hoursAgo >= 1 -> "$hoursAgo hs"
            minutesAgo < 1 -> "Ahora"
            minutesAgo == 1L -> "1 min"
            else -> "$minutesAgo min"
        }
    } catch (_: Exception) {
        "Fecha no disponible"
    }
}