package com.android.harmoniatpi.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.R
import com.android.harmoniatpi.domain.model.audio.AudioSourceType
import com.android.harmoniatpi.ui.core.theme.HarmoniaTPITheme
import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.TrackUi
import kotlin.math.roundToInt
import kotlin.math.sin

private const val MS_PER_DP_SCALE = 10f

@Composable
fun TrackItem(
    track: TrackUi,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onTrim: () -> Unit,
    onUndo: () -> Unit,
    onMute: () -> Unit,
    onShowEffects: () -> Unit,
    scrollState: ScrollState,
    isBeingRecorded: Boolean,
    currentPlaybackMs: Long,
    onSeekClick: (Long) -> Unit,
    onOffsetChange: (Long, Long) -> Unit,
    modifier: Modifier = Modifier,
    timelineWidth: Int,
) {
    var showOptions by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val animatedBorderColor by infiniteTransition.animateColor(
        initialValue = Color.Red,
        targetValue = MaterialTheme.colorScheme.background,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Recording border color animation"
    )

    val density = LocalDensity.current

    LaunchedEffect(currentPlaybackMs) {
        if (currentPlaybackMs > 0 && scrollState.maxValue > 0) {

            val playbackDp = (currentPlaybackMs / MS_PER_DP_SCALE).dp
            val playbackPx = with(density) { playbackDp.toPx() }

            //desplazamiento de track en reproduccion
            val screenWidthPx = with(density) { 300.dp.toPx() }
            val targetScrollPosition =
                (playbackPx - screenWidthPx / 3).coerceAtLeast(0f).roundToInt()

            if (targetScrollPosition != scrollState.value) {
                // Anima scroll
                scrollState.animateScrollTo(targetScrollPosition)
            }
        }
    }


    Row(
        modifier = modifier
            .clip(RoundedCornerShape(0.dp))
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxWidth()
            .height(130.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(0.dp),
            color = if (track.isMuted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceContainer,
            border = when {
                isBeingRecorded -> BorderStroke(2.dp, animatedBorderColor)
                track.selected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                else ->BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
            },
            modifier = Modifier
                .fillMaxHeight()
                .width(100.dp)
                .clickable(onClick = onClick)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    text = track.title,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall

                )
                Box {

                    IconButton(onClick = {
                        onClick()
                        showOptions = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Mostrar opciones de la pista",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant

                        )
                    }
                    TrackOptionsMenu(
                        visible = showOptions,
                        onDismiss = { showOptions = false },
                        onDelete = onDelete,
                        onTrim = onTrim,
                        onMute = onMute,
                        onUndo = onUndo,
                        onShowEffects = onShowEffects,
                        isUndoAvailable = track.isUndoAvailable,
                        isMuted = track.isMuted
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(scrollState)
        ) {

            Box(
                modifier = Modifier
                    .width(timelineWidth.dp)
                    .fillMaxHeight()
            ) {
                DbWaveform(
                    waveform = track.waveForm ?: emptyList(),
                    isMuted = track.isMuted,
                    maxDurationMs = track.durationMs,
                    startOffsetMs = track.startOffsetMs,
                    onSeekClick = onSeekClick,
                    onOffsetChange = { newOffset -> onOffsetChange(track.id, newOffset) },
                )


                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            when {
                                isBeingRecorded -> Modifier.border(
                                    width = 2.dp,
                                    color = animatedBorderColor,
                                    shape = RoundedCornerShape(8.dp)
                                )

                                track.selected -> Modifier.border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(8.dp)
                                )

                                else -> Modifier.border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        )
                ) {
                    if (currentPlaybackMs > 0) {
                        val xPos = (currentPlaybackMs / MS_PER_DP_SCALE) * density.density
                        if (xPos in 0f..size.width) {
                            drawLine(
                                color = Color.Red,
                                start = Offset(xPos, 0f),
                                end = Offset(xPos, size.height),
                                strokeWidth = 2.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackOptionsMenu(
    visible: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onTrim: () -> Unit,
    onMute: () -> Unit,
    onUndo: () -> Unit,
    onShowEffects: () -> Unit,
    isUndoAvailable: Boolean,
    isMuted: Boolean,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = visible, onDismissRequest = onDismiss, modifier = modifier
    ) {
        val muteOptionText = if (isMuted) "Activar" else "Silenciar"
        val muteOptionIcon = if (isMuted) R.drawable.mute_icon else R.drawable.unmute_icon

        DropdownMenuItem(
            text = {
                Text(text = muteOptionText)
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(muteOptionIcon),
                    contentDescription = muteOptionText
                )
            },
            onClick = onMute
        )
        DropdownMenuItem(
            text = {
                Text(text = "Volumen")
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.mix_icon),
                    contentDescription = "Volumen"
                )
            },
            onClick = {}
        )
        DropdownMenuItem(
            text = {
                Text(text = "Paneo")
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.pan_icon),
                    contentDescription = "Paneo"
                )
            },
            onClick = {}
        )
        DropdownMenuItem(
            text = {
                Text(text = "Editar")
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.edit_icon),
                    contentDescription = "Editar"
                )
            },
            onClick = {}
        )
        DropdownMenuItem(
            text = { Text(text = "Efectos") },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.fx_icon),
                    contentDescription = "Efectos"
                )
            },
            onClick = {
                onDismiss()
                onShowEffects()
            }
        )

        DropdownMenuItem(
            text = {
                Text(text = "Recortar")
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.edit_icon),
                    contentDescription = "Recortar"
                )
            },
            onClick = {
                onDismiss()
                onTrim()
            }
        )

        DropdownMenuItem(
            text = {
                Text(text = "Eliminar")
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.delete_icon),
                    contentDescription = "Efectos"
                )
            },
            onClick = {
                onDismiss()
                onDelete()
            }
        )

        if (isUndoAvailable) {
            DropdownMenuItem(
                text = {
                    Text(text = "Deshacer Recorte")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Deshacer recorte"
                    )
                },
                onClick = {
                    onDismiss()
                    onUndo()
                }
            )
        }

    }
}

@Composable
fun DbWaveform(
    waveform: List<Float>,
    isMuted: Boolean,
    maxDurationMs: Long,
    startOffsetMs: Long,
    onSeekClick: (Long) -> Unit,
    onOffsetChange: (Long) -> Unit,
    color: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    val waveformColor = if (isMuted) color else MaterialTheme.colorScheme.primary
    val backgroundColor =
        if (isMuted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceContainerHighest.copy(
            alpha = 0.6f
        )
    val density = LocalDensity.current

    val canvasWidthDp = (maxDurationMs / MS_PER_DP_SCALE).dp
    var dragOffsetMs by remember { mutableLongStateOf(0L) }
    val visualOffsetDp = ((startOffsetMs + dragOffsetMs) / MS_PER_DP_SCALE).dp


    Box(
        modifier = Modifier
            .fillMaxHeight()
            .padding(start = visualOffsetDp.coerceAtLeast(0.dp))
    ) {
        Surface(
            modifier = Modifier
                .width(canvasWidthDp)
                .fillMaxHeight(),
            shape = RoundedCornerShape(8.dp),
            color = backgroundColor
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    //muevo el waveform con hold y drag
                    .pointerInput(startOffsetMs) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { dragOffsetMs = 0L },
                            onDragEnd = {
                                val finalOffsetMs = (startOffsetMs + dragOffsetMs).coerceAtLeast(0L)
                                onOffsetChange(finalOffsetMs)
                                dragOffsetMs = 0L
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val dragMs =
                                    (dragAmount.x / density.density * MS_PER_DP_SCALE).toLong()
                                val newOffsetCandidate = startOffsetMs + dragOffsetMs + dragMs

                                if (newOffsetCandidate >= 0) {
                                    dragOffsetMs += dragMs
                                } else {
                                    dragOffsetMs = -startOffsetMs
                                }
                            }
                        )
                    }
                    //seek para elegir donde reproduzco
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { offset ->
                            val tappedMs =
                                startOffsetMs + (offset.x / density.density * MS_PER_DP_SCALE).toLong()
                            onSeekClick(tappedMs)
                        })
                    }
            ) {
                if (waveform.isNotEmpty()) {
                    val centerY = size.height / 2
                    val stepX = size.width / waveform.size.toFloat()
                    val path = Path().apply {
                        moveTo(0f, centerY)
                        waveform.forEachIndexed { index, value ->
                            val x = index * stepX
                            val y = centerY - (value * centerY)
                            lineTo(x, y)
                        }
                    }
                    drawPath(
                        path,
                        color = waveformColor,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun TrackPrev() {
    // Simulamos una onda con valores entre 0 y 1
    val fakeWaveform = List(300) { index ->
        val angle = index * 0.1f
        sin(angle) // <-- Solo sin(angle) para generar valores entre -1 y 1
    }

    HarmoniaTPITheme(false) {
        TrackItem(
            track = TrackUi(
                0,
                "",
                "Nombre",
                true,
                waveForm = fakeWaveform,
                durationMs = 3000L,
                startOffsetMs = 1000L,
                sourceType = AudioSourceType.INSTRUMENT
            ),
            onClick = {},
            onDelete = {},
            onTrim = {},
            onUndo = {},
            onMute = {},
            onShowEffects = {},
            scrollState = rememberScrollState(),
            isBeingRecorded = true,
            timelineWidth = 500,
            modifier = Modifier,
            currentPlaybackMs = 1500L,
            onSeekClick = {},
            onOffsetChange = { _, _ -> }
        )
    }
}
