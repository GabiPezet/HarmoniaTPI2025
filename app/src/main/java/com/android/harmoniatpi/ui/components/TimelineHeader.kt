package com.android.harmoniatpi.ui.components

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
    val totalMs = (timelineWidth * msPerDpScale).toLong()
    val majorMarkerIntervalMs = 1000L // 1 segundo
    val minorMarkerIntervalMs = 250L  // 1/4 de segundo
    val majorMarkerHeight = 16.dp
    val minorMarkerHeight = 8.dp
    val textHeight = 12.dp

    Canvas(
        modifier = modifier
            .width(timelineWidth.dp)
            .height(majorMarkerHeight + textHeight + 8.dp)
            .padding(start = 100.dp)
    ) {
        val canvasHeight = size.height
        val timelineBottomY = canvasHeight - textHeight.toPx() - 4.dp.toPx()

        var currentMs = 0L
        while (currentMs <= totalMs) {
            val xPos = with(density) { (currentMs / msPerDpScale).dp.toPx() }

            if (currentMs % majorMarkerIntervalMs == 0L) {

                drawLine(
                    color = markerColor,
                    start = Offset(xPos, timelineBottomY - majorMarkerHeight.toPx()),
                    end = Offset(xPos, timelineBottomY),
                    strokeWidth = 1.dp.toPx()
                )

                if (currentMs % (majorMarkerIntervalMs * 5) == 0L) {
                    val seconds = currentMs / 1000
                    val minutes = seconds / 60
                    val remainingSeconds = seconds % 60
                    val timeString = String.format("%d:%02d", minutes, remainingSeconds)

                }
            } else {

                drawLine(
                    color = markerColor.copy(alpha = 0.5f),
                    start = Offset(xPos, timelineBottomY - minorMarkerHeight.toPx()),
                    end = Offset(xPos, timelineBottomY),
                    strokeWidth = 1.dp.toPx()
                )
            }
            currentMs += minorMarkerIntervalMs
        }

        drawLine(
            color = markerColor,
            start = Offset(0f, timelineBottomY),
            end = Offset(size.width, timelineBottomY),
            strokeWidth = 1.dp.toPx()
        )
    }
}