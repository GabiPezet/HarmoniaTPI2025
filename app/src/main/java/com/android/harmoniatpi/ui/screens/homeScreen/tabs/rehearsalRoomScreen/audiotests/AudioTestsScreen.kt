package com.android.harmoniatpi.ui.screens.homeScreen.tabs.rehearsalRoomScreen.audiotests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.rehearsalRoomScreen.audiotests.viewmodel.AudioTestsViewModel

@Composable
fun AudioTestsScreen(viewModel: AudioTestsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = {
                if (state.isRecording) {
                    viewModel.stopRecording()
                } else {
                    viewModel.startRecording()
                }
            }
        ) {
            if (state.isRecording) {
                Text(text = "Parar")
            } else {
                Text(text = "Grabar")
            }
        }

        Button(
            onClick = {
                if (state.isPlaying) {
                    viewModel.pause()
                } else {
                    viewModel.play()
                }
            }
        ) {
            if (state.isPlaying) {
                Text(text = "pause")
            } else {
                Text(text = "play")
            }
        }
        Button(
            onClick = {
                viewModel.stopPlaying()
            }
        ) {
            Text(text = "stop playing")
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopRecording()
            viewModel.stopPlaying()
        }
    }
}