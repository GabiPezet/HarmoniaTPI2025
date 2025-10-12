package com.android.harmoniatpi.ui.screens.notificationScreen.model

data class NotificationState(
    val notificationsList: List<NotificationHarmonia> = emptyList(),
    val newNotifications: Boolean = false,
    val internetAvailable: Boolean = true,
    val gpsAvailable: Boolean = true
)