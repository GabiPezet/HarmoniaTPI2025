package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.domain.model.project.Project

@Composable
fun BottomMiniPlayer(
    // 🟢 CAMBIO: Recibe el proyecto que está sonando
    playingProject: Project?,
    // 🟢 CAMBIO: Callback para detener
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Si no hay proyecto sonando, no muestra nada
    if (playingProject == null) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { /* TODO: Abrir reproductor completo */ },
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // TODO: Icono de Play/Pause real (necesitaría estado de isPlaying)
            Icon(
                imageVector = Icons.Default.Pause,
                contentDescription = "Reproducir/Pausar",
                modifier = Modifier.size(32.dp)
            )

            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    // 🟢 CAMBIO: Muestra título real
                    text = playingProject.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    // 🟢 CAMBIO: Muestra autor real
                    text = "${playingProject.name} ${playingProject.lastName}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }

            // TODO: Botón de Like real
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Like",
                tint = MaterialTheme.colorScheme.primary
            )

            // 🟢 CAMBIO: Botón para cerrar el mini-reproductor
            IconButton(onClick = onStopClick) {
                Icon(Icons.Default.Close, contentDescription = "Detener")
            }
        }
    }
}