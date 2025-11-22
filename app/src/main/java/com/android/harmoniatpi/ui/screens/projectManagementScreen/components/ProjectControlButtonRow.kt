package com.android.harmoniatpi.ui.screens.projectManagementScreen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.R

@Composable
fun ProjectControlButtonRow(
    onSkipPrevious: () -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    startRecording: () -> Unit,
    stopRecording: () -> Unit,
    isRecording: Boolean,
    isPlaying: Boolean,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF1C1C1C),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 12.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp)
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            val isSkipEnabled = !isRecording
            val isRecordEnabled = isRecording || !isPlaying
            val isPlayEnabled = !isRecording

            // ⏮ Stop / SkipPrevious
            CircleIconButton(
                onClick = {
                    if (isSkipEnabled) {
                        onSkipPrevious()
                    } else {
                        onError("Acción no permitida durante la grabación")
                    }
                },
                iconColor = Color(0xFFEEEEEE),
                backgroundColor = Color(0xFFB8B1B1),
                pngRes = R.drawable.skip_previous_base,
                isActuallyEnabled = isSkipEnabled
            )

            // ⏺ Record / Stop
            CircleIconButton(
                onClick = {
                    if (isRecordEnabled) {
                        if (isRecording) stopRecording() else startRecording()
                    } else {
                        // Este es el caso "isRecording=false && isPlaying=true"
                        onError("No puedes grabar mientras reproduces")
                    }
                },
                pngRes = if (isRecording) R.drawable.stop_button_base else R.drawable.record_button_base,
                iconColor = Color.White,
                backgroundColor = if (isRecordEnabled)
                    Color(0xFFFF1744)
                else
                    Color(0xFFB71C1C),
                glow = true,
                isActuallyEnabled = isRecordEnabled
            )

            // ▶ / ⏸ Play / Pause
            CircleIconButton(
                onClick = {
                    if (isPlayEnabled) {
                        if (isPlaying) onPause() else onPlay()
                    } else {
                        onError("Acción no permitida durante la grabación")
                    }
                },
                pngRes = if (isPlaying) R.drawable.pause_button_base else R.drawable.play_button_base,
                iconColor = Color.Black,
                backgroundColor = if (isPlayEnabled)
                // Color(0xFF9E9E9E)
                    Color(0xFF36B37E)
                else
                    Color(0xFF036239),
                isActuallyEnabled = isPlayEnabled
            )
        }
    }
}

@Composable
private fun CircleIconButton(
    onClick: () -> Unit,
    icon: ImageVector? = null,
    pngRes: Int? = null,
    backgroundColor: Color,
    iconColor: Color,
    glow: Boolean = false,
    isActuallyEnabled: Boolean = true,
) {
    val glowBrush = if (glow) {
        Brush.radialGradient(
            colors = listOf(backgroundColor.copy(alpha = 0.8f), Color.Transparent)
        )
    } else null

    Surface(
        shape = CircleShape,
        color = backgroundColor,
        modifier = Modifier
            .shadow(if (glow) 12.dp else 4.dp, CircleShape)
            .background(
                glowBrush ?: Brush.linearGradient(
                    listOf(
                        backgroundColor,
                        backgroundColor
                    )
                )
            )
            .alpha(if (isActuallyEnabled) 1f else 0.5f),
    ) {
        IconButton(
            onClick = onClick,
            colors = IconButtonDefaults.iconButtonColors(contentColor = iconColor),
            modifier = Modifier.size(60.dp),
            enabled = true,
//          modifier = Modifier.padding(12.dp)
        ) {
//            Icon(
//                imageVector = icon,
//                contentDescription = null,
//                tint = iconColor
//            )

            when {
                pngRes != null -> Image(
                    painter = painterResource(id = pngRes),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )

                icon != null -> Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(64.dp)
                )
            }

        }
    }
}


//package com.android.harmoniatpi.ui.components
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.FiberManualRecord
//import androidx.compose.material.icons.filled.Pause
//import androidx.compose.material.icons.filled.PlayArrow
//import androidx.compose.material.icons.filled.SkipPrevious
//import androidx.compose.material.icons.filled.Stop
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//
//@Composable
//fun ProyectControlButtonRow(
//    onSkipPrevious: () -> Unit,
//    onPlay: () -> Unit,
//    onPause: () -> Unit,
//    startRecording: () -> Unit,
//    stopRecording: () -> Unit,
//    isRecording: Boolean,
//    isPlaying: Boolean,
//    modifier: Modifier = Modifier
//) {
//    Row(
//        modifier = modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceEvenly
//    ) {
//
//        SquareButton(
//            onClick = onSkipPrevious,
//            icon = Icons.Default.SkipPrevious,
//            contentDesc = "Volver al comienzo"
//        )
//
//        SquareButton(
//            onClick = {
//                if (isRecording) {
//                    stopRecording()
//                } else {
//                    startRecording()
//                }
//            },
//            icon = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
//            contentDesc = if (isRecording) "Parar grabación" else "Grabar",
//            color = if (isRecording) Color.Gray else Color.Red
//        )
//
//        SquareButton(
//            onClick = {
//                if (isPlaying) {
//                    onPause()
//                } else {
//                    onPlay()
//                }
//            },
//            icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
//            contentDesc = if (isPlaying) "Pausa" else "Play"
//        )
//    }
//}