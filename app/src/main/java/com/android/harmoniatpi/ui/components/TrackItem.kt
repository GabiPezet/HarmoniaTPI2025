package com.android.harmoniatpi.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.R
import com.android.harmoniatpi.ui.core.theme.HarmoniaTPITheme
import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.TrackUi
import kotlin.math.sin

@Composable
fun TrackItem(
    track: TrackUi,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showOptions by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color = Color.Transparent)
            .fillMaxWidth()
            .height(100.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background,
            border = if (track.selected) {
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            } else {
                null
            },
            modifier = Modifier
                .fillMaxHeight()
                .width(100.dp)
                .clickable(onClick = onClick)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = track.title,
                    modifier = Modifier.weight(1f),
                )
                Box {
                    IconButton(onClick = {
                        onClick()
                        showOptions = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Mostrar opciones de la pista",
                        )
                    }
                    TrackOptionsMenu(
                        visible = showOptions,
                        onDismiss = { showOptions = false },
                        onDelete = onDelete
                    )
                }
            }
        }
        DbWaveform(
            modifier = Modifier.fillMaxSize(),
            waveform = track.waveForm ?: listOf()
        )
    }
}

@Composable
private fun TrackOptionsMenu(
    visible: Boolean, onDismiss: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = visible, onDismissRequest = onDismiss, modifier = modifier
    ) {
        DropdownMenuItem(
            text = {
                Text(text = "Silenciar")
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.mute_icon),
                    contentDescription = "Silenciar"
                )
            },
            onClick = {}
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
            text = {
                Text(text = "Efectos")
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.fx_icon),
                    contentDescription = "Efectos"
                )
            },
            onClick = {}
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
    }
}

@Composable
fun DbWaveform(
    waveform: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .horizontalScroll(scrollState),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxHeight()
                    .width((waveform.size / 2).dp)
            ) {

                val centerY = size.height / 2
                val stepX = size.width / waveform.size

                val path = Path().apply {
                    moveTo(0f, centerY)
                    waveform.forEachIndexed { index, value ->
                        val x = index * stepX
                        val y = centerY - (value * centerY)
                        lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
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
            track = TrackUi(0, "", "Nombre", true, fakeWaveform),
            onClick = {},
            onDelete = {}
        )
    }
}
