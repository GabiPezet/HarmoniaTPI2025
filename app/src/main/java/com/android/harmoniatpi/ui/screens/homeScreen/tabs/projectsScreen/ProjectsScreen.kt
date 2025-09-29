package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ProjectsScreen(
    onNavigateToProjectManagement: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        //Text(text = "RehearsalRoomScreen", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Button(
            onClick = onNavigateToProjectManagement
        ) {
            Text("Manejo de Proyectos")
        }
    }
}