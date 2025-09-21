package com.android.harmoniatpi.ui.screens.editAudioTest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.ui.screens.editAudioTest.model.RecordMode
import com.android.harmoniatpi.ui.screens.editAudioTest.viewmodel.CameraXVideoRecorder
import com.android.harmoniatpi.ui.screens.editAudioTest.viewmodel.EditAudioTestViewModel

@Composable
fun EditAudioTest(
    viewModel: EditAudioTestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Selector Audio/Video
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { viewModel.setMode(RecordMode.AUDIO) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.recordMode == RecordMode.AUDIO) Color.Blue else Color.LightGray
                )
            ) { Text("Audio", color = Color.White) }

            Button(
                onClick = { viewModel.setMode(RecordMode.VIDEO) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.recordMode == RecordMode.VIDEO) Color.Blue else Color.LightGray
                )
            ) { Text("Video", color = Color.White) }
        }

        Spacer(Modifier.height(24.dp))

        when (uiState.recordMode) {
            RecordMode.AUDIO -> {
                Button(
                    onClick = {
                        if (uiState.isRecording) viewModel.stopAudioRecording()
                        else viewModel.startAudioRecording()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isRecording) Color.Red else Color.Green
                    )
                ) {
                    Text(
                        if (uiState.isRecording) "Detener" else "Grabar",
                        color = Color.White
                    )
                }

                if (uiState.recordedFileUri != null) {
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.togglePlayback() }) {
                        Text(
                            if (uiState.isPlaying) "Detener" else "Reproducir",
                            color = Color.White
                        )
                    }

                    if (uiState.isPlaying) {
                        Spacer(Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { uiState.playbackProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Blue
                        )
                    }
                }

                if (uiState.isRecording) {
                    Spacer(Modifier.height(16.dp))
                    Text("Grabando... ${uiState.recordingDuration}s", color = Color.Red)
                }
            }

            RecordMode.VIDEO -> {
                // Preview y grabación con CameraX
                CameraXVideoRecorder(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    onVideoSaved = { uri ->
                        viewModel.setMode(RecordMode.VIDEO) // asegura el modo
                        viewModel.stopAudioRecording() // resetea estado audio
                        viewModel.togglePlayback() // opcional
                        // update uiState con uri del video grabado
                    }
                )
            }
        }
    }
}
