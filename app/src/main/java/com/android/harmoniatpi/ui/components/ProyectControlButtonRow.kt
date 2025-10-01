package com.android.harmoniatpi.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun ProyectControlButtonRow(
    onSkipPrevious: () -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    startRecording: () -> Unit,
    stopRecording: () -> Unit,
    isRecording: Boolean,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        SquareButton(
            onClick = onSkipPrevious,
            icon = Icons.Default.SkipPrevious,
            contentDesc = "Volver al comienzo"
        )

        SquareButton(
            onClick = {
                if (isRecording) {
                    stopRecording()
                } else {
                    startRecording()
                }
            },
            icon = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
            contentDesc = if (isRecording) "Parar grabación" else "Grabar",
            color = if (isRecording) Color.Gray else Color.Red
        )

        SquareButton(
            onClick = {
                if (isPlaying) {
                    onPause()
                } else {
                    onPlay()
                }
            },
            icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDesc = if (isPlaying) "Pausa" else "Play"
        )
    }
}