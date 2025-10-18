package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.model.ProjectTab

@Composable
fun ProjectCard(
    project: Project,
    selectedTab: ProjectTab,
    currentUserId: String,
    onClick: () -> Unit,
    onNavigateToVersions: () -> Unit,
    onDeleteClick: (String) -> Unit,
    onForkClick: (Project) -> Unit
) {
    val isMyProject = project.ownerId == currentUserId
    val forksByOthers = project.forkedByUserIds.filter { it != project.ownerId }
    val hasBeenForkedByOthers = forksByOthers.isNotEmpty()
    val hasCurrentUserForked = project.forkedByUserIds.contains(currentUserId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 🔹 Encabezado con título y botón de eliminar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Solo muestra el botón de borrar si es MI proyecto y estoy en MI pestaña
                if (isMyProject && selectedTab == ProjectTab.MY_PROJECTS) {
                    IconButton(
                        onClick = { onDeleteClick(project.id) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar proyecto",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = project.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = project.hashtags.joinToString(" "),
                fontStyle = FontStyle.Italic,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(12.dp))

            // 🔹 Lógica de botones condicionales
            when (selectedTab) {
                ProjectTab.MY_PROJECTS -> {
                    // El dueño del proyecto solo ve "Escuchar Versiones" si OTRO usuario ha forkeado.
                    if (hasBeenForkedByOthers) {
                        Button(
                            onClick = { onNavigateToVersions() }, // 🟢 FIX 4: Corregido de mi error anterior
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.LibraryMusic,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(
                                text = "ESCUCHAR VERSIONES",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
                ProjectTab.COLLABS -> {
                    // En Colaboraciones, no mostramos el botón de "Guardar" en nuestros propios proyectos.
                    if (!isMyProject) {
                        Button(
                            onClick = { onForkClick(project) },
                            enabled = !hasCurrentUserForked,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (hasCurrentUserForked) Icons.Default.Check else Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(text = if (hasCurrentUserForked) "GUARDADO" else "GUARDAR CAMBIOS")
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Muestra la fila de avatares solo si otros han forkeado.
            if (hasBeenForkedByOthers) {
                // Pasamos la lista filtrada para no mostrar al dueño del proyecto.
                ForkedByUsersRow(forkedByUserIds = forksByOthers)
                Spacer(Modifier.height(12.dp))
            }

            // 🔹 Fila de acciones inferiores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { /* Likear */ }) {
                    Icon(Icons.Default.Favorite, contentDescription = "Like")
                }
                IconButton(onClick = { /* Comentar */ }) {
                    Icon(Icons.Default.ModeComment, contentDescription = "Comment")
                }
                IconButton(onClick = { /* Compartir */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
                IconButton(onClick = { /* Descargar */ }) {
                    Icon(Icons.Default.Download, contentDescription = "Download")
                }
                IconButton(onClick = { /* Configuración */ }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        }
    }
}

