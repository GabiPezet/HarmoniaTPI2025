package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.model.ProjectTab

@Composable
fun TrackItemCard(
    project: Project,
    currentUserId: String,
    selectedTab: ProjectTab,
    onNavigateToManagement: () -> Unit,
    onTogglePlayPause: () -> Unit, // Se mantiene para el botón sobre la imagen
    isCurrentlyPlaying: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPublishClick: () -> Unit,
    onNavigateToVersions: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val isMyProject = project.ownerId == currentUserId
    val isMyClone = isMyProject && project.originalProjectId != null
    val forksByOthers = project.forkedByUserIds.filter { it != project.ownerId }
    val hasBeenForkedByOthers = forksByOthers.isNotEmpty()

    Column( // Usamos Column para poder añadir ForkedByUsersRow debajo
        modifier = Modifier
            .fillMaxWidth()
            // ✨ CAMBIO: El clickable principal ahora navega a Management
            .clickable { onNavigateToManagement() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
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
                Image(
                    painter = rememberAsyncImagePainter(
                        // TODO: Usar una imagen real del proyecto si existe
                        model = "https://picsum.photos/seed/${project.id}/200/200"
                    ),
                    contentDescription = project.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Overlay semitransparente
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                )
                // Icono de Play/Pause
                Icon(
                    imageVector = if (isCurrentlyPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isCurrentlyPlaying) "Pausar" else "Reproducir",
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            // 2. Información (Título, Autor)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${project.name} ${project.lastName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(Modifier.width(12.dp))

            // 3. Duración (Real)
            Text(
                // 🟢 CAMBIO: Usa la duración real formateada
                text = formatDuration(project.duration),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 4. Botón de "Más Opciones" (se queda igual)
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Más opciones"
                    )
                }

                DropdownMenu(
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
                    if (isMyProject && project.originalProjectId == null && !project.isPublished && selectedTab == ProjectTab.MY_PROJECTS) {
                        DropdownMenuItem(
                            text = { Text("Publicar") },
                            onClick = { onPublishClick(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Publish, null) }
                        )
                    }

                    // Opción: Ver Versiones (Solo para mis proyectos originales publicados Y con forks)
                    if (isMyProject && project.originalProjectId == null && project.isPublished && hasBeenForkedByOthers && selectedTab == ProjectTab.MY_PROJECTS) {
                        DropdownMenuItem(
                            text = { Text("Ver Versiones") },
                            onClick = { onNavigateToVersions(); showMenu = false },
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
        if (isMyProject && project.originalProjectId == null && hasBeenForkedByOthers && selectedTab == ProjectTab.MY_PROJECTS) {
            Spacer(Modifier.height(8.dp))
            ForkedByUsersRow(forkedByUserIds = forksByOthers) // Asegúrate que ForkedByUsersRow sea público
        }
    }
}


fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
