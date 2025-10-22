package com.android.harmoniatpi.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.R

@Composable
fun ProyectControlButtonRow(
    onSkipPrevious: () -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    startRecording: () -> Unit,
    stopRecording: () -> Unit,
    isRecording: Boolean,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF1C1C1C),
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ⏮ Stop / SkipPrevious
            CircleIconButton(
                onClick = onSkipPrevious,
                iconColor = Color(0xFFEEEEEE),
                backgroundColor = Color(0xFF2C2C2C),
                icon = Icons.Default.SkipPrevious
            )

            // ⏺ Record / Stop
            CircleIconButton(
                onClick = {
                    if (isRecording) stopRecording() else startRecording()
                },
                //icon = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                pngRes = if (isRecording) R.drawable.stop_button_base else R.drawable.record_button_base,
                iconColor = Color.White,
                backgroundColor = if (isRecording)
                    Color(0xFFB71C1C)
                else
                    Color(0xFFFF1744),
                glow = true
            )

            // ▶ / ⏸ Play / Pause
            CircleIconButton(
                onClick = {
                    if (isPlaying) onPause() else onPlay()
                },
                //icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                pngRes = if (isPlaying) R.drawable.pause_button_base else R.drawable.play_button_base,
                iconColor = Color.Black,
                backgroundColor = if (isPlaying)
                    Color(0xFF00E676)
                else
                    Color(0xFF9E9E9E)
            )
        }
    }
}

@Composable
private fun CircleIconButton(
    onClick: () -> Unit,
    icon: ImageVector? = null,
    pngRes: Int? = null, //nueva modificación
    backgroundColor: Color,
    iconColor: Color,
    glow: Boolean = false
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
            .background(glowBrush ?: Brush.linearGradient(listOf(backgroundColor, backgroundColor))),
    ) {
        IconButton(
            onClick = onClick,
            colors = IconButtonDefaults.iconButtonColors(contentColor = iconColor),
            modifier = Modifier.size(60.dp)
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