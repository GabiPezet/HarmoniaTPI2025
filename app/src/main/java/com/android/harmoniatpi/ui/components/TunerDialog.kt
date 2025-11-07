package com.android.harmoniatpi.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.harmoniatpi.R

@Composable
fun TunerDialog(
    note: String,
    onDismiss: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    // Inicia el afinador cuando el diálogo aparece
    // y lo detiene cuando desaparece.
    DisposableEffect(Unit) {
        onStart()
        onDispose {
            onStop()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Afinador") },
        text = {
            Column(
                modifier = Modifier.padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_tuner),
                    contentDescription = "Afinador",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    "Nota Detectada:",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (note.isBlank()) "--" else note,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 72.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}