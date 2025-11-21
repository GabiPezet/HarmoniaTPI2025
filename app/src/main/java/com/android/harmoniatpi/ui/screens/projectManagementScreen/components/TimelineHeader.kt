package com.android.harmoniatpi.ui.screens.projectManagementScreen.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun TimelineHeader(
    timelineWidth: Int,
    msPerDpScale: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val markerColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface

    // Convertimos el ancho total en Dp a milisegundos
    val totalMs = (timelineWidth * msPerDpScale).toLong()

    // Determinamos el intervalo de las marcas (ej. cada 1 segundo)
    val majorMarkerIntervalMs = 1000L // 1 segundo
    val minorMarkerIntervalMs = 250L  // 1/4 de segundo

    val majorMarkerHeight = 16.dp
    val minorMarkerHeight = 8.dp
    val textHeight = 12.dp

    Canvas(
        modifier = modifier
            .width(timelineWidth.dp)
            .height(majorMarkerHeight + textHeight + 8.dp) // Altura total
            .padding(start = 100.dp) // Offset para alinear con las pistas
    ) {
        val canvasHeight = size.height
        val timelineBottomY = canvasHeight - textHeight.toPx() - 4.dp.toPx()

        var currentMs = 0L
        while (currentMs <= totalMs) {
            val xPos = with(density) { (currentMs / msPerDpScale).dp.toPx() }

            if (currentMs % majorMarkerIntervalMs == 0L) {
                // Marca Mayor (cada segundo)
                drawLine(
                    color = markerColor,
                    start = Offset(xPos, timelineBottomY - majorMarkerHeight.toPx()),
                    end = Offset(xPos, timelineBottomY),
                    strokeWidth = 1.dp.toPx()
                )

                // Texto (cada 5 segundos para no saturar)
                if (currentMs % (majorMarkerIntervalMs * 5) == 0L) {
                    val seconds = currentMs / 1000
                    val minutes = seconds / 60
                    val remainingSeconds = seconds % 60
                    val timeString = String.format("%d:%02d", minutes, remainingSeconds)

                    // (Esta es una forma simple de dibujar texto en Canvas,
                    // para algo más complejo se usaría Text)
                    // drawContext.canvas.nativeCanvas.drawText( ... )
                }
            } else {
                // Marca Menor (cada 250ms)
                drawLine(
                    color = markerColor.copy(alpha = 0.5f),
                    start = Offset(xPos, timelineBottomY - minorMarkerHeight.toPx()),
                    end = Offset(xPos, timelineBottomY),
                    strokeWidth = 1.dp.toPx()
                )
            }
            currentMs += minorMarkerIntervalMs
        }

        // Línea base de la regla
        drawLine(
            color = markerColor,
            start = Offset(0f, timelineBottomY),
            end = Offset(size.width, timelineBottomY),
            strokeWidth = 1.dp.toPx()
        )
    }
}