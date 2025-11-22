package com.android.harmoniatpi.ui.screens.projectManagementScreen.components


import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.R
import com.android.harmoniatpi.ui.core.theme.HarmoniaTPITheme
import com.android.harmoniatpi.ui.screens.projectManagementScreen.utils.formatTimeMillis

@Composable
fun TimeDisplayPanel(
    currentMillis: Long,
    totalMillis: Long,
    onMetronomeClick: () -> Unit,
    isBeingRecorded: Boolean,
    isPlaying: Boolean,
    bpm: Int,
    isMetronomeEnabled: Boolean ,
    modifier: Modifier = Modifier
) {
    val currentTimeStr = remember(currentMillis) { formatTimeMillis(currentMillis) }
    val totalTimeStr = remember(totalMillis) { formatTimeMillis(totalMillis) }

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val animatedRecordingColor by infiniteTransition.animateColor(
        initialValue = Color.Red,
        targetValue = MaterialTheme.colorScheme.background,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Recording border color animation"
    )

    Surface(
        modifier = modifier,
        border = when {
            isBeingRecorded -> BorderStroke(2.dp, animatedRecordingColor)
            else -> BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
            )
        },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            MetronomeControl(
                bpm = bpm,
                isEnabled = isMetronomeEnabled,
                isPlayingOrRecording = isBeingRecorded || isPlaying,
                onClick = onMetronomeClick
            )

            Spacer(Modifier.weight(1f))

            TimeDisplay(
                currentTimeStr = currentTimeStr,
                totalTimeStr = totalTimeStr,
                isBeingRecorded = isBeingRecorded,
                animatedRecordingColor = animatedRecordingColor
            )
        }
    }
}

/**
 * Muestra el estado de grabación y el tiempo.
 * Al apilar "Grabando..." y el timer verticalmente en una Columna,
 * evitamos que el layout "salte" horizontalmente.
 */
@Composable
private fun TimeDisplay(
    currentTimeStr: String,
    totalTimeStr: String,
    isBeingRecorded: Boolean,
    animatedRecordingColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End // Alinea ambos textos a la derecha
    ) {

            Text(
                text = "Grabando...",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = animatedRecordingColor,
                    fontWeight = FontWeight.Bold
                ),
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.alpha(if (isBeingRecorded) 1f else 0f)
            )


        val baseTimerColor = if (isBeingRecorded) {
            animatedRecordingColor
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = if (isBeingRecorded) {
                            baseTimerColor
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                ) {
                    append(currentTimeStr)
                }
                append(" / ")
                append(totalTimeStr)
            },
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = FontFamily.Monospace,
            color = baseTimerColor
        )
    }
}


/**
 * Un control clickable que muestra el BPM y el ícono del metrónomo.
 * Al hacer clic en él se invoca [onClick].
 */
@Composable
private fun MetronomeControl(
    bpm: Int,
    isEnabled: Boolean,
    isPlayingOrRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (bpmTextColor, iconColor) = when {
        isEnabled && isPlayingOrRecording -> {
            val beatDurationMs = (60_000 / bpm).toInt()
            val transition = rememberInfiniteTransition(label = "MetronomeBlink")
            val animatedBlinkColor by transition.animateColor(
                initialValue = MaterialTheme.colorScheme.primary,
                targetValue = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                animationSpec = infiniteRepeatable(
                    animation = tween(beatDurationMs / 2, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "MetronomeBlinkColor"
            )
            animatedBlinkColor to animatedBlinkColor
        }

        isEnabled && !isPlayingOrRecording -> {
            MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primary
        }
        else -> {
            MaterialTheme.colorScheme.onSurface to MaterialTheme.colorScheme.onSurfaceVariant
        }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Configurar metrónomo y tempo. $bpm BPM." }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text= bpm.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = bpmTextColor
            )
            Text(
                "BPM",
                style = MaterialTheme.typography.labelSmall,
                color = bpmTextColor
            )
        }

        Icon(
            painter = painterResource(R.drawable.metronome),
            contentDescription = null,
            tint = iconColor
        )
    }
}
@Preview(showBackground = true)
@Composable
fun TimeDisplayPanelPreview() {
    HarmoniaTPITheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            TimeDisplayPanel(
                currentMillis = 15300L,  // 00:15.3
                totalMillis = 105000L, // 01:45.0
                onMetronomeClick = { },
                isBeingRecorded = true,
                isPlaying = true,
                bpm = 128,
                isMetronomeEnabled = true
            )
            Spacer(modifier = Modifier.padding(16.dp))
            TimeDisplayPanel(
                currentMillis = 15300L,
                totalMillis = 105000L,
                onMetronomeClick = { },
                isBeingRecorded = false, 
                isPlaying = false,
                bpm = 128,
                isMetronomeEnabled = true
            )
        }
    }
}