package com.android.harmoniatpi.ui.screens.projectManagementScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectManagementScreen(
    onNavigateToRecording: () -> Unit,
    onNavigateToCollab: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Opciones")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text("Pantalla de gestión de proyecto musical", modifier = Modifier.align(Alignment.Center))
        }

        if (showSheet) {
            ModalBottomSheet (
                onDismissRequest = { showSheet = false },
                sheetState = sheetState
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Button(
                        onClick = {
                            showSheet = false
                            onNavigateToRecording()
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