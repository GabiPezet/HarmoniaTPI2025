package com.android.harmoniatpi.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Diálogo que invita al usuario a adquirir la versión Premium.
 * Se muestra cuando se intenta acceder a una funcionalidad bloqueada.
 */
@Composable
fun UpsellDialog(
    onDismiss: () -> Unit,
    onConfirmPurchase: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700)) }, // Color Dorado
        title = { Text("Desbloquea HoloJam Premium") },
        text = {
            Text("Esta función es exclusiva para usuarios Premium. \n\nAdquiere la versión completa para aplicar efectos ilimitados, exportar en alta calidad y apoyar el desarrollo.")
        },
        confirmButton = {
            TextButton(onClick = onConfirmPurchase) {
                Text("Obtener Premium")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Quizás luego")
            }
        }
    )
}