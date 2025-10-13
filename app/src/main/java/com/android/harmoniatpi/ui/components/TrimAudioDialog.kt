package com.android.harmoniatpi.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.TrackUi
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrimAudioDialog(
    track: TrackUi,
    previewTrackId: Long?,
    onDismiss: () -> Unit,
    onConfirmTrim: (trackId: Long, startMs: Long, endMs: Long) -> Unit,
    onPreviewTrim: (trackId: Long, startMs: Long, endMs: Long) -> Unit,
    onStopPreview: (trackId: Long) -> Unit
) {
    val minTrimMs = 50L // Mínimo de 50ms de longitud del corte
    val maxMs = track.durationMs

    if (maxMs <= minTrimMs) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Error de Recorte") },
            text = { Text("La pista es demasiado corta para recortar (duración: ${formatDuration(maxMs)}).") },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        )
        return
    }


    var trimRange by remember {
        mutableStateOf(0f..maxMs.toFloat())
    }

    // Asegurar que el rango de recorte es siempre al menos minTrimMs (para evitar archivos vacíos)
    val isValidRange = (trimRange.endInclusive - trimRange.start).roundToInt().toLong() > minTrimMs
    val isPreviewing = previewTrackId == track.id

    DisposableEffect(Unit) {
        onDispose {
            if (isPreviewing) {
                onStopPreview(track.id)
            }
        }
    }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Recortar ${track.title}") },
        text = {
            Column {
                Text(
                    "Selecciona el rango de audio (Duración total: ${formatDuration(maxMs)}):",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(16.dp))

// Range Slider para seleccionar el inicio y el fin
                RangeSlider(
                    value = trimRange,
                    onValueChange = { newRange ->
                        trimRange = newRange.start.coerceAtLeast(0f)..newRange.endInclusive.coerceAtMost(maxMs.toFloat())
                        if (isPreviewing) { // Detener si el usuario modifica el rango mientras escucha
                            onStopPreview(track.id)
                        }
                    },
                    valueRange = 0f..maxMs.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Indicadores de tiempo y botón de Play/Pause
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val startMsValue = trimRange.start.roundToInt().toLong()
                    val endMsValue = trimRange.endInclusive.roundToInt().toLong()

                    Column {
                        Text("Inicio: ${formatDuration(startMsValue)}", style = MaterialTheme.typography.bodyMedium)
                        Text("Fin: ${formatDuration(endMsValue)}", style = MaterialTheme.typography.bodyMedium)
                    }

                    // Botón de Preview (Play/Pause)
                    Button(
                        onClick = {
                            if (isPreviewing) {
                                onStopPreview(track.id)
                            } else {
                                onPreviewTrim(track.id, startMsValue, endMsValue)
                            }
                        },
                        enabled = isValidRange
                    ) {
                        Icon(
                            imageVector = if (isPreviewing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPreviewing) "Pausa Previsualización" else "Previsualizar Recorte",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (!isValidRange) {
                    Text(
                        "El rango de recorte debe ser mayor a ${minTrimMs}ms",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val start = trimRange.start.roundToInt().toLong()
                    val end = trimRange.endInclusive.roundToInt().toLong()
                    if (isPreviewing) onStopPreview(track.id) // Detener antes de confirmar
                    onConfirmTrim(track.id, start, end)
                },
                enabled = isValidRange
            ) {
                Text("Confirmar Recorte")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (isPreviewing) onStopPreview(track.id) // Detener antes de salir
                onDismiss()
            }) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Formatea milisegundos a un string de minutos:segundos.milisegundos
 */
fun formatDuration(ms: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(minutes)
    val milliseconds = ms % 1000 // Milisegundos restantes
    return String.format("%02d:%02d.%03d", minutes, seconds, milliseconds)
}