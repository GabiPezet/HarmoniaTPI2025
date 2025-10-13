package com.android.harmoniatpi.ui.screens.songVersionsScreen.util

// --- Función Helper para formatear tiempo (ejemplo básico) ---
fun formatMillisToTimeString(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}