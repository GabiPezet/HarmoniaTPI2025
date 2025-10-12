package com.android.harmoniatpi.ui.screens.notificationScreen.model

data class NotificationHarmonia(
    val id: Int,
    val title: String,
    val description: String,
    val date: String,
    val isNew: Boolean = true
)
