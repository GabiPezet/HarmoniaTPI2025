package com.android.harmoniatpi.ui.screens.projectManagementScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.ui.components.SquareButton
import com.android.harmoniatpi.ui.components.TrackItem
import com.android.harmoniatpi.ui.screens.recordingScreen.viewmodel.ProjectScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectManagementScreen(
    onNavigateToCollab: () -> Unit,
    viewModel: ProjectScreenViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                state.tracks.forEachIndexed { index, track ->
                    item {
                        TrackItem(
                            track = track,
                            onDelete = { viewModel.deleteTrack(index) }
                        )
                    }
                }
            }

            IconButton(
                onClick = {
                    showSheet = true
                },
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.Start)
                    .weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    tint = Color.Black
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                SquareButton(
                    onClick = { viewModel.stopPlaying() },
                    icon = Icons.Default.SkipPrevious,
                    contentDesc = "Limpiar audio"
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
            }

            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showSheet = false },
                    sheetState = sheetState
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = {
                                showSheet = false
                                viewModel.addTrack()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Nueva pista")
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                showSheet = false
                                // Lógica de pickear media iría acá
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Abrir archivo")
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                showSheet = false
                                onNavigateToCollab()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Biblioteca de colaboraciones")
                        }
                    }
                }
            }
        }
    }
}