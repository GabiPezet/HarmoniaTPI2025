package com.android.harmoniatpi.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.R
import com.android.harmoniatpi.ui.core.theme.HarmoniaTPITheme
import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.TrackUi

@Composable
fun TrackItem(
    track: TrackUi, modifier: Modifier = Modifier, onDelete: () -> Unit = {}
) {
    var showOptions by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .background(
                color = Color.Transparent, shape = RoundedCornerShape(16.dp)
            )
            .fillMaxWidth()
            .height(100.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxHeight()
                .width(100.dp)
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
                    color = MaterialTheme.colorScheme.secondary
                )
                Box {
                    IconButton(onClick = { showOptions = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Mostrar opciones de la pista",
                            tint = MaterialTheme.colorScheme.secondary
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
        DbWaveForm(
            modifier = Modifier.fillMaxSize()
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
                Text(
                    text = "Silenciar",
                    color = MaterialTheme.colorScheme.secondary
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.mute_icon),
                    contentDescription = "Silenciar",
                    tint = MaterialTheme.colorScheme.secondary
                )
            },
            onClick = {}
        )
        DropdownMenuItem(
            text = {
                Text(
                    text = "Volumen",
                    color = MaterialTheme.colorScheme.secondary
                )
            }, leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.mix_icon),
                    contentDescription = "Volumen",
                    tint = MaterialTheme.colorScheme.secondary
                )
            },
            onClick = {}
        )
        DropdownMenuItem(
            text = {
                Text(
                    text = "Paneo",
                    color = MaterialTheme.colorScheme.secondary
                )
            }, leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.pan_icon),
                    contentDescription = "Paneo",
                    tint = MaterialTheme.colorScheme.secondary
                )
            },
            onClick = {}
        )
        DropdownMenuItem(
            text = {
                Text(
                    text = "Editar",
                    color = MaterialTheme.colorScheme.secondary
                )
            }, leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.edit_icon),
                    contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.secondary
                )
            },
            onClick = {}
        )
        DropdownMenuItem(
            text = {
                Text(
                    text = "Efectos",
                    color = MaterialTheme.colorScheme.secondary
                )
            }, leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.fx_icon),
                    contentDescription = "Efectos",
                    tint = MaterialTheme.colorScheme.secondary
                )
            },
            onClick = {}
        )

        DropdownMenuItem(
            text = {
                Text(
                    text = "Eliminar",
                    color = MaterialTheme.colorScheme.secondary
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.delete_icon),
                    contentDescription = "Efectos",
                    tint = MaterialTheme.colorScheme.secondary
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
private fun DbWaveForm(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            Text(text = "Waveform")
        }
    }
}

@Preview
@Composable
private fun TrackPrev() {
    HarmoniaTPITheme(false) {
        TrackItem(
            track = TrackUi("Nombre")
        )
    }
}