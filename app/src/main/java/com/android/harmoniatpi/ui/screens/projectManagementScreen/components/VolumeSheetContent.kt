package com.android.harmoniatpi.ui.screens.projectManagementScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.TrackUi
import java.text.DecimalFormat

@Composable
fun VolumeSheetContent(
    track: TrackUi,
    onVolumeChange: (trackId: Long, newVolume: Float) -> Unit,
    onDismiss: () -> Unit
) {
    var sliderValue by remember(track.volume) { mutableFloatStateOf(track.volume) }
    val decimalFormat = remember { DecimalFormat("0") } // Usamos "0" para no mostrar decimales

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Volumen: ${track.title}",
            style = MaterialTheme.typography.titleLarge
        )

        Column {
            Text(
                "Volumen: ${decimalFormat.format(sliderValue * 100)}%",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Slider(
                value = sliderValue,
                onValueChange = {
                    sliderValue = it
                }, // Actualiza el estado local mientras se desliza
                onValueChangeFinished = {
                    // Llama al ViewModel solo cuando el usuario suelta el slider
                    onVolumeChange(track.id, sliderValue)
                },
                valueRange = 0f..1.5f, // Rango de 0% a 150%
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "0% (Silencio) - 100% (Normal) - 150% (Boost)",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }

            Spacer(Modifier.width(12.dp))

            Button(
                onClick = {
                    onVolumeChange(track.id, sliderValue)
                    onDismiss()
                    //en un futuro creo que esto podría servir para que se persista el volumen en la pista y no cada vez que mueve el slider
                },
                enabled = sliderValue != track.volume
            ) {
                Text("Aceptar")
            }
        }
    }
}
