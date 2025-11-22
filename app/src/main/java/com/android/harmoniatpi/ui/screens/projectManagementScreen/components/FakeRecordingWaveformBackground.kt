package com.android.harmoniatpi.ui.screens.projectManagementScreen.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun FakeRecordingWaveformBackground(
    modifier: Modifier = Modifier,
    barCount: Int = 40,
    maxHeight: Dp = 100.dp,
    minHeight: Dp = 10.dp,
    animationDuration: Int = 800
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")

    // Cada barra tiene su "fase" inicial aleatoria (offset en el ciclo)
    val phaseOffsets = remember(barCount) {
        List(barCount) { Random.nextFloat() * 2 * PI.toFloat() }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        phaseOffsets.forEachIndexed { index, phase ->
            val time = infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 2 * PI.toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = animationDuration + index * 20,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                ),
                label = "barAnim$index"
            )

            // Movimiento senoidal (sube y baja suavemente con fase aleatoria)
            val heightFraction = (sin(time.value + phase) + 1f) / 2f
            val barHeight = lerp(minHeight, maxHeight, heightFraction)

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(barHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f))
            )
        }
    }
}