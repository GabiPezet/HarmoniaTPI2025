package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.android.harmoniatpi.R
import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.model.ProjectTab

@Composable
fun ProjectCard(
    project: Project,
    currentUserId: String,
    selectedTab: ProjectTab,
    forkedByUsers: List<UserPreferences>,
    onNavigateToManagement: () -> Unit,
    onTogglePlayPause: () -> Unit,
    isCurrentlyPlaying: Boolean,
    isPreviewLoading: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPublishClick: () -> Unit,
    onNavigateToVersions: (project: Project) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    val isMyProject = project.ownerId == currentUserId
    val isMyClone = isMyProject && project.originalProjectId != null
    val forksByOthers = project.forkedByUserIds.filter { it != project.ownerId }
    val hasBeenForkedByOthers = forkedByUsers.isNotEmpty()

    Card(
        modifier = Modifier
            .testTag("ProjectCard")
            .fillMaxWidth()
            .padding(4.dp)
            .clickable { onNavigateToManagement() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Añade elevación
        shape = RoundedCornerShape(8.dp) // Esquinas redondeadas

    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp) // Padding vertical interno
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Imagen del "Track" con Overlay de Play/Pause
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onTogglePlayPause() }, // El clic en la imagen/botón controla play/pause
                    contentAlignment = Alignment.Center
                ) {
                    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant

                    AsyncImage(
                        model = project.imageUrl,
                        placeholder = ColorPainter(placeholderColor),
                        error = painterResource(id = R.drawable.portada_proyecto_error),
                        contentDescription = project.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    )
                    // Icono de Play/Pause
                    when {
                        isPreviewLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        }

                        isCurrentlyPlaying -> {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pausar",
                                modifier = Modifier.size(32.dp), // Icono un poco más pequeño
                                tint = MaterialTheme.colorScheme.onPrimary // Color blanco sobre overlay
                            )
                        }

                        else -> {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Reproducir",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                Spacer(Modifier.width(16.dp))

                // 2. Información (Título, Autor)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = project.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (project.isPublished) {
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = "Proyecto publicado",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = "Proyecto no publicado (privado)",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), // Un color más sutil
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${project.name} ${project.lastName}",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(2.dp))
                    if (project.originalProjectId == null && selectedTab == ProjectTab.MY_PROJECTS) {
                        Spacer(Modifier.height(8.dp))
                        ForkedByUsersRow(users = forkedByUsers)
                    }
                }

                Spacer(Modifier.width(12.dp))

                // 3. Duración (Real)

                val calculatedDuration = remember(project.urlAudioTracks) {
                    // La duración total es el punto final (offset + duración)
                    // de la pista que termine más tarde.
                    project.urlAudioTracks.maxOfOrNull { track ->
                        val trackDuration = (track.trimEndMs.takeIf { it != -1L }
                            ?: track.durationMs) - track.trimStartMs
                        track.startOffsetMs + trackDuration
                    } ?: project.duration // Si no hay pistas, usa la (probablemente 0)
                }

                // 2. Usamos la duración calculada
                Text(
                    text = formatDuration(calculatedDuration),
                    style = MaterialTheme.typography.bodyMedium,
                )

                // 4. Botón de "Más Opciones" (se queda igual)
                Box {
                    IconButton(
                        modifier = Modifier.testTag("MoreOptions"),
                        onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Más opciones"
                        )
                    }

                    DropdownMenu(
                        modifier = Modifier.testTag("MoreOptionsMenuDropdown"),
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        // --- Opciones Comunes ---
                        DropdownMenuItem(
                            text = { Text("Compartir") },
                            onClick = { /* TODO */; showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Share, null) }
                        )

                        // --- Opciones Condicionales ---

                        // Opción: Publicar (Solo para mis proyectos originales NO publicados)
                        if (!project.isPublished) {
                            DropdownMenuItem(
                                text = { Text("Publicar") },
                                onClick = { onPublishClick(); showMenu = false },
                                leadingIcon = { Icon(Icons.Default.Publish, null) }
                            )
                        }

                        // Opción: Ver Versiones (Solo para mis proyectos originales publicados Y con forks)
                        if (project.isPublished) {
                            DropdownMenuItem(
                                text = { Text("Ver Versiones") },
                                onClick = { onNavigateToVersions(project); showMenu = false },
                                leadingIcon = { Icon(Icons.Default.LibraryMusic, null) }
                            )
                        }

                        // Opción: Editar (Solo para mis proyectos o mis clones)
                        if (isMyProject) { // Esto incluye originales y clones
                            DropdownMenuItem(
                                text = { Text("Editar") },
                                onClick = { onEditClick(); showMenu = false },
                                leadingIcon = { Icon(Icons.Default.Edit, null) }
                            )
                        }

                        // Opción: Borrar (Para mis originales en mi pestaña O mis clones en collab)
                        val canDelete =
                            (isMyProject && project.originalProjectId == null && selectedTab == ProjectTab.MY_PROJECTS) ||
                                    (isMyClone && selectedTab == ProjectTab.COLLABS)
                        if (canDelete) {
                            DropdownMenuItem(
                                modifier = Modifier.testTag("DeleteMenuItem"),
                                text = { Text("Eliminar") },
                                onClick = { onDeleteClick(); showMenu = false },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("Reportar") },
                            onClick = { /* TODO */; showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Flag, null) }
                        )
                    }
                }
            }
        }
    }
}


fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
