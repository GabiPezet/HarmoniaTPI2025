package com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage

@Composable
fun PostProfileImage(
    imageUrl: String,
    userName: String,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    if (imageUrl.isNotBlank()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Foto de perfil",
            modifier = modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userName.firstOrNull()?.toString()?.uppercase() ?: "U",
                style = MaterialTheme.typography.titleMedium, // Ajustado para tamaño pequeño
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
    }
}