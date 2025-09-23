package com.android.harmoniatpi.ui.screens.editAudioTest

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness1
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.R
import com.android.harmoniatpi.ui.screens.editAudioTest.model.EditAudioTestState
import com.android.harmoniatpi.ui.screens.editAudioTest.model.RecordMode
import com.android.harmoniatpi.ui.screens.editAudioTest.viewmodel.CameraXVideoRecorder
import com.android.harmoniatpi.ui.screens.editAudioTest.viewmodel.EditAudioTestViewModel
import kotlin.random.Random

@Composable
fun EditAudioTest(
    viewModel: EditAudioTestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E1E2E), Color(0xFF2D1B69))
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header con título y modo
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Studio Recorder",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            // Selector de modo mejorado
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp)
                    .background(Color(0xFF363646), shape = RoundedCornerShape(25.dp)),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ModeSelectorItem(
                    text = "Audio",
                    isSelected = uiState.recordMode == RecordMode.AUDIO,
                    onClick = { viewModel.setMode(RecordMode.AUDIO) }
                )

                ModeSelectorItem(
                    text = "Video",
                    isSelected = uiState.recordMode == RecordMode.VIDEO,
                    onClick = { viewModel.setMode(RecordMode.VIDEO) }
                )
            }
        }

        // Área principal de contenido
        when (uiState.recordMode) {
            RecordMode.AUDIO -> {
                AudioRecordingSection(uiState, viewModel)
            }

            RecordMode.VIDEO -> {
                VideoRecordingSection(uiState, viewModel)
            }
        }

        // Footer con información adicional
        if (uiState.isRecording || uiState.isPlaying) {
            RecordingInfoSection(uiState)
        }
    }
}

@Composable
fun ModeSelectorItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .padding(4.dp)
            .background(
                color = if (isSelected) Color(0xFF6C5CE7) else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun AudioRecordingSection(
    uiState: EditAudioTestState,
    viewModel: EditAudioTestViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Visualizador de onda de audio
        Spacer(Modifier.height(32.dp))

        AudioWaveformVisualizer(
            isRecording = uiState.isRecording,
            isPlaying = uiState.isPlaying,
            progress = uiState.playbackProgress
        )

        Spacer(Modifier.height(48.dp))

        // Botón de grabación principal
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = if (uiState.isRecording) Color.Red else Color(0xFF6C5CE7),
                    shape = CircleShape
                )
                .clickable {
                    if (uiState.isRecording) viewModel.stopAudioRecording()
                    else viewModel.startAudioRecording()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    id = if (uiState.isRecording)
                        R.drawable.ic_record
                    else
                        R.drawable.ic_pause
                ),
                contentDescription = if (uiState.isRecording) "Detener" else "Grabar",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        // Controles de reproducción
        if (uiState.recordedFileUri != null) {
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Botón de reproducción
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0xFF00B894), shape = CircleShape)
                        .clickable { viewModel.togglePlayback() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (uiState.isPlaying)
                                R.drawable.ic_pause
                            else
                                R.drawable.ic_play
                        ),
                        contentDescription = if (uiState.isPlaying) "Pausar" else "Reproducir",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AudioWaveformVisualizer(
    isRecording: Boolean,
    isPlaying: Boolean,
    progress: Float
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 100),
        label = "waveform_progress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(Color(0xFF363646), shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Barra de progreso
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Color.White.copy(alpha = 0.3f), shape = RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(4.dp)
                    .background(Color(0xFF6C5CE7), shape = RoundedCornerShape(2.dp))
            )
        }

        // Visualizador de onda
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isRecording || isPlaying) {
                // Simulación de barras de audio animadas
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    repeat(20) { index ->
                        val randomHeight by animateFloatAsState(
                            targetValue = if (isRecording || isPlaying)
                                (10f + Random.nextFloat() * 40f)
                            else 10f,
                            animationSpec = tween(durationMillis = 200),
                            label = "wave_bar_$index"
                        )

                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(randomHeight.dp)
                                .background(
                                    color = if (index % 4 == 0) Color(0xFF6C5CE7) else Color(
                                        0xFF00B894
                                    ),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            } else {
                Text(
                    text = "Presiona grabar para comenzar",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun VideoRecordingSection(
    uiState: EditAudioTestState,
    viewModel: EditAudioTestViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CameraXVideoRecorder(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp)),
            onVideoSaved = { uri ->
                viewModel.setMode(RecordMode.VIDEO)
                viewModel.stopAudioRecording()
                viewModel.togglePlayback()
            }
        )

        Spacer(Modifier.height(24.dp))

        // Controles de video similares a audio
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color.Red, shape = CircleShape)
                .clickable { /* Lógica de video */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Brightness1,
                contentDescription = "Grabar video",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun RecordingInfoSection(uiState: EditAudioTestState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        // Información de tiempo
        Text(
            text = when {
                uiState.isRecording -> "Grabando • ${uiState.recordingDuration}s"
                uiState.isPlaying -> "Reproduciendo • ${(uiState.playbackProgress * 100).toInt()}%"
                else -> ""
            },
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        // Indicador de estado
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (uiState.isRecording) Color.Red else Color.Green,
                        shape = CircleShape
                    )
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (uiState.isRecording) "En vivo" else "Reproduciendo",
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
