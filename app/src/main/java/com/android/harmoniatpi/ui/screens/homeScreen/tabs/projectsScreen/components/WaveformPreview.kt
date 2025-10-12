package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun WaveformPreview(
    waveform: List<Float>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(64.dp),
    barColor: Color = Color(0xFFFAFAFA),
    backgroundColor: Color = Color.Transparent
) { //En esta parte se crea una onda y si waveform viene vacía la completa con valores aleatorios
    // vas a ver que ahora en este testeo la onda viene rellena con 150 muestras más arriba
    // cuando recibimos la onda, la normalizamos para poder hacer que todas ocupen el mismo espacio y tengan formas similares
    val safeWave = if (waveform.isEmpty()) listOf(0.5f) else waveform
    val normalized = remember(safeWave) {
        // Clamp y normalizar entre 0..1
        val clamped = safeWave.map { it.coerceIn(0f, 1f) }
        val max = clamped.maxOrNull().takeIf { it != 0f } ?: 1f
        clamped.map { it / max }
    }
    Canvas(modifier = modifier.background(backgroundColor)) {
        val w = size.width
        val h = size.height
        val count = normalized.size
        val spacing =
            (w * 0.03f) / (count.coerceAtLeast(1)) // Este es el valor para un espaciado relativo, hay que ir probandoló.
        val availableWidth = w - spacing * (count - 1)
        val barWidth = (availableWidth / count).coerceAtLeast(1f)

        normalized.forEachIndexed { i, value ->
            val left = i * (barWidth + spacing)
            val barHeight = value * h
            val top = (h - barHeight) / 2f
            drawRect(
                color = barColor,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight)
            )
        }
    }
}