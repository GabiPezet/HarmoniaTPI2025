package com.android.harmoniatpi.ui.screens.projectManagementScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun MetronomeSheetContent(
    currentBpm: Int,
    isMetronomeEnabled: Boolean,
    currentVolume: Float,
    onBpmChange: (Int) -> Unit,
    onMetronomeEnabledChange: (Boolean) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    // Usamos un estado local para el slider para que sea fluido.
    // Solo actualizamos el ViewModel cuando el usuario "suelta" el slider.

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Configuración del Metrónomo",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Activar Metrónomo",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Switch(
                checked = isMetronomeEnabled,
                onCheckedChange = onMetronomeEnabledChange // Llama a viewModel.setMetronomeEnabled
            )
        }

        // Slider de BPM ---
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "$currentBpm BPM",
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )

            Slider(
                value = currentBpm.toFloat(),
                onValueChange = { newValue ->
                    onBpmChange(newValue.roundToInt())
                },
                valueRange = 40f..240f, // Rango de BPM
                steps = (240 - 40 - 1), // Pasos (uno por cada BPM)
                modifier = Modifier.fillMaxWidth()
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Volumen: ${(currentVolume * 100).roundToInt()}%",                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Slider(
                value = currentVolume,
                onValueChange = { newValue ->
                    // 4. Llama al ViewModel en CADA cambio
                    onVolumeChange(newValue)
                },
                valueRange = 0f..1f, // Rango de volumen
                modifier = Modifier.fillMaxWidth()
            )
        }
        // --- 4. Botón de Listo ---
        Button(
            onClick = onDismiss, // Simplemente cierra el sheet
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Listo")
        }
    }
}