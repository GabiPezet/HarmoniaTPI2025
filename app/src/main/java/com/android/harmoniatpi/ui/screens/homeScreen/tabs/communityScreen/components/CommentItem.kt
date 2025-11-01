package com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.android.harmoniatpi.domain.model.userPreferences.Comment

@Composable
fun CommentItem(comment: Comment) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar del usuario
        if (comment.photoUrlUser.isNotEmpty()) {
            AsyncImage(
                model = comment.photoUrlUser,
                contentDescription = "Avatar de ${comment.name}",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Avatar",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.width(12.dp))

        // Columna para el texto
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = "${comment.name} ${comment.lastName}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = formatPostDateToHoursAgo(comment.id),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = comment.comment, style = MaterialTheme.typography.bodyMedium)

        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    HorizontalDivider()
}


private fun formatPostDateToHoursAgo(createdAt: String): String {
    return try {
        val postTimestamp = createdAt.toLong()
        val now = System.currentTimeMillis()

        val diffInMillis = now - postTimestamp
        val minutesAgo = diffInMillis / (1000 * 60) // Convertir a minutos
        val hoursAgo = minutesAgo / 60
        val daysAgo = hoursAgo / 24

        when {
            daysAgo >= 1 -> "$daysAgo día" + if (daysAgo > 1) "s" else ""
            hoursAgo >= 1 -> "$hoursAgo hs"
            minutesAgo < 1 -> "Ahora"
            minutesAgo == 1L -> "1 min"
            else -> "$minutesAgo min"
        }
    } catch (_: Exception) {
        "Fecha no disponible"
    }
}