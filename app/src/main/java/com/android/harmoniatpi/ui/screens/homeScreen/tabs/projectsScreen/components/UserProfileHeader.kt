package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.MenuUiState

@Composable
fun UserProfileHeader(
    sharedState: MenuUiState,
    projectsCount: Int,
    clonesCount: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Imagen de Perfil
        Image(
            painter = rememberAsyncImagePainter(
                model = sharedState.userPhotoPath.ifBlank { "https://picsum.photos/seed/profile/150/150" } // Usa placeholder si está vacía
            ),
            contentDescription = "Foto de perfil",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
        )

        Spacer(Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // Nombre de Usuario
            Text(
                text = "${sharedState.userName} ${sharedState.userLastName}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            // Instrumento/Ubicación (Mostrar si no está vacío)
            val detailText = sharedState.instrument.ifBlank { sharedState.location } // O combina ambos
            if (detailText.isNotBlank()) {
                Text(
                    text = detailText,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(4.dp))

            // Fila de Estadísticas
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CompactStatItem(count = projectsCount.toString(), label = "Proyectos")
                CompactStatItem(count = clonesCount.toString(), label = "Clones")
                // TODO: Añadir Followers reales si los tienes
                CompactStatItem(count = "1.2K", label = "Followers")
            }
        }
    }
}

@Composable
fun CompactStatItem(count: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) { // Usa Row para ponerlos lado a lado
        Text(
            text = count,
            style = MaterialTheme.typography.bodyMedium, // Texto más pequeño
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(4.dp)) // Espacio pequeño
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}