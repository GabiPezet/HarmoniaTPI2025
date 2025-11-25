package com.android.harmoniatpi.ui.screens.paymentResultScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
    fun PaymentResultScreen(
    status: String,
    paymentId: String?,
    onContinue: () -> Unit
) {
    val (icon, message, color) = when (status) {
        "approved" -> Triple(Icons.Default.CheckCircle, "¡Suscripción Exitosa!", MaterialTheme.colorScheme.primary)
        "rejected" -> Triple(Icons.Default.Error, "El pago fue rechazado", MaterialTheme.colorScheme.error)
        else -> Triple(Icons.Default.Info, "Estado: $status", MaterialTheme.colorScheme.secondary)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(100.dp))
        Spacer(Modifier.height(16.dp))
        Text(text = message, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        if (paymentId != null) {
            Text(text = "ID: $paymentId", style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(32.dp))
        Button(onClick = onContinue) {
            Text("Volver al inicio")
        }
    }
}