package com.android.harmoniatpi.ui.screens.notificationScreen

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.R
import com.android.harmoniatpi.ui.components.SetOrientationScreen
import com.android.harmoniatpi.ui.components.Toolbar
import com.android.harmoniatpi.ui.screens.notificationScreen.components.NotificationCard
import com.android.harmoniatpi.ui.screens.notificationScreen.model.NotificationHarmonia
import com.android.harmoniatpi.ui.screens.notificationScreen.viewModel.NotificationsViewModel

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    viewModelNotification: NotificationsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModelNotification.uiState.collectAsState()
    var selectedNotificationHarmonia by remember { mutableStateOf<NotificationHarmonia?>(null) }
    val hasNavigatedBack = remember { mutableStateOf(false) }
    val lastBackTime = remember { mutableLongStateOf(0L) }
    val debounceMillis = 300L

    SetOrientationScreen(context = context, orientation = true)

    fun safeNavigateToBack() {
        val now = SystemClock.elapsedRealtime()
        if (!hasNavigatedBack.value && now - lastBackTime.longValue > debounceMillis) {
            lastBackTime.longValue = now
            hasNavigatedBack.value = true
            onBack()
        }
    }

    BackHandler {
        safeNavigateToBack()
    }

    LaunchedEffect(Unit) {
        viewModelNotification.markNotificationsAsRead()
    }

    Scaffold(
        topBar = {
            Toolbar(
                title = "Notificaciones",
                onBackClick = { safeNavigateToBack() },
                onNotificationClick = { },
                isInternetAvailable = uiState.internetAvailable,
            )
        },
        content = { innerPadding ->

            if (uiState.notificationsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.onSecondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Icon(
                            modifier = Modifier.size(48.dp),
                            painter = painterResource(R.drawable.ic_empty_screen),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = stringResource(R.string.notification_screen_no_notifications_tittle),
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            text = stringResource(R.string.notification_screen_no_notifications_message),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Normal,
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 18.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.onSecondaryContainer)
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    items(items = uiState.notificationsList) { notification ->
                        NotificationCard(
                            notificationHarmonia = notification,
                            onClick = { selectedNotificationHarmonia = it },
                            onDeleteClick = { viewModelNotification.deleteNotification(notification.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                if (selectedNotificationHarmonia != null) {
                    AlertDialog(
                        onDismissRequest = { selectedNotificationHarmonia = null },
                        title = { Text(selectedNotificationHarmonia?.title.orEmpty()) },
                        text = { Text(selectedNotificationHarmonia?.description.orEmpty()) },
                        confirmButton = {
                            TextButton(onClick = { selectedNotificationHarmonia = null }) {
                                Text(stringResource(R.string.notification_screen_text_close))
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.secondary,
                        textContentColor = MaterialTheme.colorScheme.secondary
                    )
                }

            }
        }
    )
}