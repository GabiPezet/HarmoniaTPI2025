package com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.ui.screens.homeScreen.viewmodel.HomeScreenViewModel
import kotlinx.coroutines.launch

@Composable
fun CommunityScreen(
    onExitApp: () -> Unit,
    drawerState: DrawerState,
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()

    BackHandler {
        if (drawerState.isOpen) {
            scope.launch {
                drawerState.close()
            }
        } else {
            onExitApp()
        }

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "Comunidad",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )

        Button(onClick = { viewModel.sendNotification() }) {
            Text("Enviar Notificacion")
        }
    }
}