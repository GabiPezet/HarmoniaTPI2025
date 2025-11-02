package com.android.harmoniatpi.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun GlobalPlayhead(
    currentPlaybackMs: Long,
    msPerDpScale: Float,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    if (currentPlaybackMs <= 0) return

    val density = LocalDensity.current

    // 1. Calcula la posición en Píxeles del cabezal basado en los ms
    val playheadX = with(density) { (currentPlaybackMs / msPerDpScale).dp.toPx() }

    // 2. Ajusta esa posición restando el scroll actual
    val scrolledX = playheadX - scrollState.value

    // 3. Añade el offset de 100.dp del TrackItem
    val trackListOffsetPx = with(density) { 100.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .offset { IntOffset(x = (scrolledX + trackListOffsetPx).roundToInt(), y = 0) }
            .width(2.dp)
            .background(Color.Red)
    )
}