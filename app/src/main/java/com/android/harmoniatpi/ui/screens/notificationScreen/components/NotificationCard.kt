package com.android.harmoniatpi.ui.screens.notificationScreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.ui.screens.notificationScreen.model.NotificationHarmonia

@Composable
fun NotificationCard(
    notificationHarmonia: NotificationHarmonia,
    onClick: (NotificationHarmonia) -> Unit,
    onDeleteClick: (NotificationHarmonia) -> Unit
) {
    var isEllipsized by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = notificationHarmonia.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notificationHarmonia.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar notificación",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onDeleteClick(notificationHarmonia) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = notificationHarmonia.description,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { layoutResult ->
                    isEllipsized = layoutResult.hasVisualOverflow
                }
            )

            if (isEllipsized) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Leer más",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .clickable { onClick(notificationHarmonia) }
                        .padding(top = 4.dp)
                )
            }
        }
    }
}