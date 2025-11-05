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
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RestartAlt
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.R
import com.android.harmoniatpi.domain.model.audio.AudioSourceType
import com.android.harmoniatpi.ui.core.theme.HarmoniaTPITheme
import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.TrackUi
import kotlin.math.roundToInt
import kotlin.math.sin


@Composable
fun TrackItem(
    track: TrackUi,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onUndo: () -> Unit,
    onMute: () -> Unit,
    onShowEffects: () -> Unit,
    scrollState: ScrollState,
    isBeingRecorded: Boolean,
    currentPlaybackMs: Long,
    onSeekClick: (Long) -> Unit,
    onOffsetChange: (Long, Long) -> Unit,
    onSelectionChanged: (startMs: Long?, endMs: Long?) -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onUndoEffect: () -> Unit,
    isUndoEffectAvailable: Boolean,
    isSelectionActive: Boolean,
    modifier: Modifier = Modifier,
    timelineWidth: Int,
    msPerDpScale: Float
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

    /*
    LaunchedEffect(currentPlaybackMs) {
        if (currentPlaybackMs > 0 && scrollState.maxValue > 0) {

            val playbackDp = (currentPlaybackMs / msPerDpScale).dp
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

     */


    Row(
        modifier = modifier
            .clip(RoundedCornerShape(0.dp))
            .background(Color(0xFF858585))
            .fillMaxWidth()
            .height(130.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (isBeingRecorded) {
            Surface(
                shape = RoundedCornerShape(0.dp),
                color = if (track.isMuted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceContainer,
                border = when {
                    isBeingRecorded -> BorderStroke(2.dp, animatedBorderColor)
                    track.selected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    else -> BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                    )
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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        text = track.title,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2
                    )

                    Spacer(Modifier.weight(1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onMute) {
                            val muteOptionText = if (track.isMuted) "Activar" else "Silenciar"
                            val muteOptionIcon =
                                if (track.isMuted) R.drawable.mute_icon else R.drawable.unmute_icon
                            Icon(
                                painter = painterResource(muteOptionIcon),
                                contentDescription = muteOptionText,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

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
                    }

                    TrackOptionsMenu(
                        visible = showOptions,
                        onDismiss = { showOptions = false },
                        onDelete = onDelete,
                        onMute = onMute,
                        onUndo = onUndo,
                        onShowEffects = onShowEffects,
                        isUndoAvailable = track.isUndoAvailable,
                        isMuted = track.isMuted,
                        onCopy = onCopy,
                        onCut = onCut,
                        onUndoEffect = onUndoEffect,
                        isUndoEffectAvailable = isUndoEffectAvailable,
                        isSelectionActive = isSelectionActive
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(width = 2.dp, color = animatedBorderColor)
                    .weight(1f)
            ) {
                FakeRecordingWaveformBackground(
                    modifier = Modifier.matchParentSize()
                )
            }
        } else {
            Surface(
                shape = RoundedCornerShape(0.dp),
                color = if (track.isMuted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceContainer,
                border = when {
                    isBeingRecorded -> BorderStroke(2.dp, animatedBorderColor)
                    track.selected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    else -> BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                    )
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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        text = track.title,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2
                    )

                    Spacer(Modifier.weight(1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onMute) {
                            val muteOptionText = if (track.isMuted) "Activar" else "Silenciar"
                            val muteOptionIcon =
                                if (track.isMuted) R.drawable.mute_icon else R.drawable.unmute_icon
                            Icon(
                                painter = painterResource(muteOptionIcon),
                                contentDescription = muteOptionText,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

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
                    }

                    TrackOptionsMenu(
                        visible = showOptions,
                        onDismiss = { showOptions = false },
                        onDelete = onDelete,
                        onMute = onMute,
                        onUndo = onUndo,
                        onShowEffects = onShowEffects,
                        isUndoAvailable = track.isUndoAvailable,
                        isMuted = track.isMuted,
                        onCopy = onCopy,
                        onCut = onCut,
                        onUndoEffect = onUndoEffect,
                        isUndoEffectAvailable = isUndoEffectAvailable,
                        isSelectionActive = isSelectionActive
                    )
                }
            }
            Box(
                modifier = Modifier
                    //.fillMaxSize()
                    .fillMaxHeight()
                    .weight(1f)
                    .horizontalScroll(scrollState)
            ) {

                Box(
                    modifier = Modifier
                        .width(timelineWidth.dp)
                        .fillMaxHeight()
                ) {
                    DbWaveform(
                        modifier = Modifier.fillMaxSize(),
                        waveform = track.waveForm ?: emptyList(),
                        isMuted = track.isMuted,
                        maxDurationMs = track.durationMs,
                        startOffsetMs = track.startOffsetMs,
                        onOffsetChange = { newOffset -> onOffsetChange(track.id, newOffset) },
                        selectionStartMs = track.selectionStartMs,
                        selectionEndMs = track.selectionEndMs,
                        onSelectionChanged = { startMs, endMs ->
                            onSelectionChanged(
                                startMs,
                                endMs
                            )
                        },
                        msPerDpScale = msPerDpScale,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )


                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()

                            .pointerInput(Unit, msPerDpScale, onSeekClick, density) {
                                detectTapGestures(onTap = { offset ->
                                    val tappedMs =
                                        (offset.x / density.density * msPerDpScale).toLong()
                                    onSeekClick(tappedMs)
                                })
                            }

                            .then(
                                when {
                                    isBeingRecorded -> BorderStroke(2.dp, animatedBorderColor)
                                    track.selected -> BorderStroke(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary
                                    )

                                    else -> BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                                    )
                                }
                                    .let { Modifier.border(it, RoundedCornerShape(8.dp)) }
                            )
                    ) {
                        if (currentPlaybackMs > 0) {
                            val xPos = (currentPlaybackMs / msPerDpScale) * density.density
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
}

@Composable
private fun TrackOptionsMenu(
    visible: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onMute: () -> Unit,
    onUndo: () -> Unit,
    onShowEffects: () -> Unit,
    isUndoAvailable: Boolean,
    isMuted: Boolean,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onUndoEffect: () -> Unit,
    isUndoEffectAvailable: Boolean,
    isSelectionActive: Boolean,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = visible, onDismissRequest = onDismiss, modifier = modifier
    ) {

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
            text = { Text(text = "Copiar") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copiar"
                )
            },
            onClick = {
                onDismiss()
                onCopy()
            },
            enabled = isSelectionActive
        )

        DropdownMenuItem(
            text = { Text(text = "Cortar") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.ContentCut,
                    contentDescription = "Cortar"
                )
            },
            onClick = {
                onDismiss()
                onCut()
            },
            enabled = isSelectionActive // Solo se activa si hay algo seleccionado
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

        if (isUndoEffectAvailable) {
            DropdownMenuItem(
                text = { Text(text = "Deshacer Efecto") },
                leadingIcon = { Icon(Icons.Default.RestartAlt, "Deshacer efecto") },
                onClick = {
                    onDismiss()
                    onUndoEffect()
                }
            )
        }


    }
}

@Composable
fun DbWaveform(
    modifier: Modifier = Modifier,
    waveform: List<Float>,
    isMuted: Boolean,
    maxDurationMs: Long,
    startOffsetMs: Long,
    onOffsetChange: (Long) -> Unit,
    selectionStartMs: Long?,
    selectionEndMs: Long?,
    onSelectionChanged: (startMs: Long?, endMs: Long?) -> Unit,
    msPerDpScale: Float,
    color: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    val waveformColor = if (isMuted) color else MaterialTheme.colorScheme.primary
    val backgroundColor =
        if (isMuted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceContainerHighest.copy(
            alpha = 0.6f
        )
    val density = LocalDensity.current

    val canvasWidthDp = (maxDurationMs / msPerDpScale).dp
    var dragOffsetMs by remember { mutableLongStateOf(0L) }
    val visualOffsetDp = ((startOffsetMs + dragOffsetMs) / msPerDpScale).dp

    val canvasWidthPx = with(density) { canvasWidthDp.toPx() }

    var handleStartPx by remember(selectionStartMs, density, msPerDpScale) {
        mutableFloatStateOf(
            selectionStartMs?.let { (it / msPerDpScale) * density.density } ?: 0f
        )
    }
    var handleEndPx by remember(selectionEndMs, canvasWidthPx, density, msPerDpScale) {
        mutableFloatStateOf(
            selectionEndMs?.let { (it / msPerDpScale) * density.density } ?: canvasWidthPx
        )
    }

    val minClipWidthPx = with(density) { 10.dp.toPx() }

    Box(
        modifier = modifier
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
                    .pointerInput(startOffsetMs, canvasWidthPx, visualOffsetDp) {
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
                                    (dragAmount.x / density.density * msPerDpScale).toLong()
                                val newOffsetCandidate = startOffsetMs + dragOffsetMs + dragMs

                                if (newOffsetCandidate >= 0) {
                                    dragOffsetMs += dragMs
                                } else {
                                    dragOffsetMs = -startOffsetMs
                                }
                            }
                        )
                    }
                    .drawWithContent {
                        drawContent()
                        val validStartPx = handleStartPx.coerceIn(0f, handleEndPx)
                        if (validStartPx > 0f) {
                            drawRect(
                                color = Color.Black.copy(alpha = 0.5f),
                                size = size.copy(width = validStartPx)
                            )
                        }
                        val validEndPx = handleEndPx.coerceIn(handleStartPx, size.width)
                        if (validEndPx < size.width) {
                            drawRect(
                                color = Color.Black.copy(alpha = 0.5f),
                                topLeft = Offset(validEndPx, 0f),
                                size = size.copy(width = size.width - validEndPx)
                            )
                        }
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

        val handleWidth = 20.dp
        val handleWidthPx = with(density) { handleWidth.toPx() }

        //BARRAS
        Box(
            modifier = Modifier
                .offset { IntOffset((handleStartPx - handleWidthPx / 2).roundToInt(), 0) }
                .width(handleWidth)
                .fillMaxHeight()
                .background(
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                    RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                ) // Color diferente para selección
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        val newPos =
                            (handleStartPx + delta).coerceIn(0f, handleEndPx - minClipWidthPx)
                        handleStartPx = newPos
                    },
                    onDragStopped = {
                        val startMs =
                            (handleStartPx / density.density * msPerDpScale).coerceAtLeast(
                                0f
                            ).toLong()
                        val endMs = (handleEndPx / density.density * msPerDpScale).coerceAtMost(
                            maxDurationMs.toFloat()
                        ).toLong()
                        onSelectionChanged(startMs, endMs)
                    }
                )
        ) {
            Icon(
                Icons.Filled.DragHandle,
                contentDescription = "Inicio selección",
                tint = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Box(
            modifier = Modifier
                .offset { IntOffset((handleEndPx - handleWidthPx / 2).roundToInt(), 0) }
                .width(handleWidth)
                .fillMaxHeight()
                .background(
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                    RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                ) // Color diferente
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->

                        val newPos = (handleEndPx + delta).coerceIn(
                            handleStartPx + minClipWidthPx,
                            canvasWidthPx
                        )
                        handleEndPx = newPos
                    },
                    onDragStopped = {

                        val startMs =
                            (handleStartPx / density.density * msPerDpScale).coerceAtLeast(
                                0f
                            ).toLong()
                        val endMs = (handleEndPx / density.density * msPerDpScale).coerceAtMost(
                            maxDurationMs.toFloat()
                        ).toLong()
                        onSelectionChanged(startMs, endMs)
                    }
                )
        ) {
            Icon(
                Icons.Filled.DragHandle,
                contentDescription = "Fin selección",
                tint = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
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
            onUndo = {},
            onMute = {},
            onShowEffects = {},
            scrollState = rememberScrollState(),
            isBeingRecorded = true,
            timelineWidth = 500,
            modifier = Modifier,
            currentPlaybackMs = 1500L,
            onSeekClick = {},
            onOffsetChange = { _, _ -> },
            onSelectionChanged = { _, _ -> },
            onCopy = {},
            onCut = {},
            isUndoEffectAvailable = false,
            onUndoEffect = {},
            isSelectionActive = false,
            msPerDpScale = 0F,
        )
    }
}