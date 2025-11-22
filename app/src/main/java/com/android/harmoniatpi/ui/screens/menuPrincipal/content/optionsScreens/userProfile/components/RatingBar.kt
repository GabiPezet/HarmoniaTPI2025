package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RatingBar(
    modifier: Modifier = Modifier,
    rating: Float,
    starColor: Color = MaterialTheme.colorScheme.primary,
    emptyStarColor: Color = MaterialTheme.colorScheme.outline
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        (1..5).forEach { starIndex ->
            val (icon, tint) = when {
                // Estrella llena
                rating >= starIndex -> {
                    Icons.Filled.Star to starColor
                }
                // Media estrella
                rating >= starIndex - 0.5f -> {
                    Icons.AutoMirrored.Filled.StarHalf to starColor
                }
                // Estrella vacía
                else -> {
                    Icons.Outlined.Star to emptyStarColor
                }
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${(rating / 5 * 100).toInt()}%",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
    }
}