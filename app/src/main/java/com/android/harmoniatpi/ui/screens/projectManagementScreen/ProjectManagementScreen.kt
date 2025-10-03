package com.android.harmoniatpi.ui.screens.projectManagementScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.android.harmoniatpi.ui.components.ProyectControlButtonRow
import com.android.harmoniatpi.ui.components.TrackItem
import com.android.harmoniatpi.ui.screens.projectManagementScreen.viewmodel.ProjectManagementScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectManagementScreen(
    onNavigateToCollab: () -> Unit,
    viewModel: ProjectManagementScreenViewModel = hiltViewModel(),
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
                state.tracks.forEach { track ->
                    item {
                        TrackItem(
                            track = track,
                            onClick = { viewModel.selectTrack(track.id) },
                            onDelete = { viewModel.deleteTrack() }
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
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    tint = Color.Black
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            ProyectControlButtonRow(
                onSkipPrevious = { },
                onPlay = { viewModel.play() },
                onPause = { viewModel.pause() },
                startRecording = { viewModel.startRecording() },
                stopRecording = { viewModel.stopRecording() },
                isRecording = state.isRecording,
                isPlaying = state.isPlaying,
                modifier = Modifier,
            )

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
                                viewModel.addNewTrack()
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