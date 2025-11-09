package com.android.harmoniatpi.ui.screens.projectManagementScreen.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AddTrackSheetContent(
    onImportFromFile: () -> Unit,
    onRecordVoice: () -> Unit,
    onRecordInstrument: () -> Unit,
    onPasteTrack: () -> Unit,
    isClipboardFull: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Añadir pista",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Fila 1: Grabar Voz e Instrumento
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OptionCard(
                title = "Grabar Voz\n(Cancelación\n de eco)",
                icon = Icons.Default.Mic,
                onClick = onRecordVoice,
                modifier = Modifier.weight(1f)
            )
            OptionCard(
                title = "Grabar Instrumento\n(Hi-Fi)",
                icon = Icons.Default.MusicNote,
                onClick = onRecordInstrument,
                modifier = Modifier.weight(1f)
            )
        }

        // Fila 2: Importar desde archivo
        OptionCard(
            title = "Importar desde archivo",
            icon = Icons.Default.Folder,
            onClick = onImportFromFile,
            modifier = Modifier.fillMaxWidth()
        )

        // Fila 3: Pegar (Condicional)
        if (isClipboardFull) {
            OptionCard(
                title = "Pegar Pista",
                icon = Icons.Default.ContentPaste,
                onClick = onPasteTrack,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Composable para cada opción de la BottomSheet
@Composable
fun OptionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Spacer(modifier = Modifier.padding(240.dp))
            // Icono circular flotante
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .background(Color(0xFFFF8117), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
