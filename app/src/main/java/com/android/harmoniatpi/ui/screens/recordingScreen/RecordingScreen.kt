package com.android.harmoniatpi.ui.screens.recordingScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.ui.components.SquareButton
import com.android.harmoniatpi.ui.screens.recordingScreen.viewmodel.ProjectScreenViewModel

@Composable
fun RecordingScreen(
    viewModel: ProjectScreenViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // espacio para lo que sería el espectro musical posteriormente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.LightGray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    state.isRecording -> Text("Grabando...", style = MaterialTheme.typography.bodyLarge)
                    state.isPlaying -> Text("Reproduciendo...", style = MaterialTheme.typography.bodyLarge)
                    else -> Text("Sin audio", style = MaterialTheme.typography.bodyLarge)
                }
            }

            //barra de botones - el de borrar aun no hace nada.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SquareButton(
                    onClick = onBack,
                    icon = Icons.Default.Home,
                    contentDesc = "Inicio"
                )

                SquareButton(
                    onClick = {
                        if (state.isRecording) {
                            viewModel.stopRecording()
                        } else {
                            viewModel.startRecording()
                        }
                    },
                    icon = if (state.isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                    contentDesc = if (state.isRecording) "Parar grabación" else "Grabar",
                    color = if (state.isRecording) Color.Gray else Color.Red
                )

                SquareButton(
                    onClick = {
                        if (state.isPlaying) {
                            viewModel.pause()
                        } else {
                            viewModel.play()
                        }
                    },
                    icon = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDesc = if (state.isPlaying) "Pausa" else "Play"
                )

                SquareButton(
                    onClick = { viewModel.stopPlaying() },
                    icon = Icons.Default.Delete,
                    contentDesc = "Limpiar audio"
                )
            }
        }
    }

    // Limpieza al salir
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopRecording()
            viewModel.stopPlaying()
        }
    }
}
