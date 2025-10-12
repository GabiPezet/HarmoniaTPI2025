package com.android.harmoniatpi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Toolbar(
    title: String,
    onBackClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    hasNotifications: Boolean = false,
    showNotificationIcon: Boolean = true,
    showMenuPrincipal: Boolean = false,
    onMenuClick: () -> Unit = {},
    isInternetAvailable: Boolean = false,

    ) {
    val isBackAvailable = remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    CenterAlignedTopAppBar(
        title = {
            if (isInternetAvailable) {
                Text(
                    text = title.uppercase(Locale.getDefault()),
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleLarge
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title.uppercase(Locale.getDefault()),
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.titleLarge
                    )
                    NoInternetMessage()

                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    scope.launch {
                        if (isBackAvailable.value) {
                            if (showMenuPrincipal) onMenuClick() else onBackClick()
                            isBackAvailable.value = false

                            delay(300)
                            isBackAvailable.value = true

                        }
                    }
                }
            ) {
                Icon(
                    imageVector = if (showMenuPrincipal) Icons.Default.Menu else Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = if (showMenuPrincipal) "Abrir menú" else "Volver",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        actions = {
            Box(
                modifier = Modifier.padding(end = 8.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                if (showNotificationIcon) {
                    IconButton(onClick = onNotificationClick) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Notificaciones",
                                tint = MaterialTheme.colorScheme.secondary
                            )

                            if (hasNotifications) {
                                Box(
                                    modifier = Modifier
                                        .size(11.dp)
                                        .background(Color.Red, CircleShape)
                                        .align(Alignment.TopEnd)
                                        .offset(
                                            x = 2.dp,
                                            y = (-2).dp
                                        ) // ajuste fino sobre la campana
                                )
                            }
                        }
                    }
                }

            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}