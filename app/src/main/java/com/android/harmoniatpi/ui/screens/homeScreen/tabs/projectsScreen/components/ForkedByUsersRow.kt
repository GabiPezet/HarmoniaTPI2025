package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun ForkedByUsersRow(forkedByUserIds: List<String>) {
    if (forkedByUserIds.isEmpty()) return // No mostrar nada si nadie ha forkeado

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Forked ${forkedByUserIds.size} veces",
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(modifier = Modifier.width(8.dp))

        // Muestra hasta 3 avatares
        forkedByUserIds.take(3).forEach { userId ->
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.width((-8).dp)) // Para solaparlos
        }

        // Si hay más de 3, muestra "+N"
        if (forkedByUserIds.size > 3) {
            Text(
                text = "+${forkedByUserIds.size - 3}",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}