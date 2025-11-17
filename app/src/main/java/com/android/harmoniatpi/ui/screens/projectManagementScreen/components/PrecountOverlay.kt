package com.android.harmoniatpi.ui.screens.projectManagementScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

/**
 * Un overlay modal que muestra el mensaje de pre-cuenta
 * y bloquea la interacción con la UI de abajo.
 */
@Composable
fun PrecountOverlay(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            // Fondo oscuro semitransparente
            .background(Color.Black.copy(alpha = 0.7f))
            // Bloquea todos los clics
            .clickable(enabled = true, onClick = {}), 
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.displayLarge, 
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}