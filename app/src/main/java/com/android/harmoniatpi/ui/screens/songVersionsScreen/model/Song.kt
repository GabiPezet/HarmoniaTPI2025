package com.android.harmoniatpi.ui.screens.songVersionsScreen.model

data class Song(
    val id: String,
    val title: String,
    val artistName: String,
    val versionType: String, // "Versión Original", "Versión Derivada"
    val artistImageUrl: String?, // URL o placeholder
    val audioUrl: String, // Para reproducción
    val durationMillis: Long,
    val projectId: String? = null // Para "Abrir proyecto"
)