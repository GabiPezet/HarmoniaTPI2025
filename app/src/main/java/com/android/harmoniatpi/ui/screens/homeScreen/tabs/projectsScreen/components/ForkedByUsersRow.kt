package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.harmoniatpi.domain.model.UserPreferences
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import coil.request.ImageRequest
import coil.size.Size

@Composable
fun ForkedByUsersRow(
    // CAMBIO 1: Pasa la lista de usuarios resuelta, no los IDs
    users: List<UserPreferences>
) {
    // CAMBIO 2: Comprueba si la lista de usuarios está vacía
    if (users.isEmpty()) return

    val avatarSize = 36.dp
    val density = LocalDensity.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            // CAMBIO 3: Usa el tamaño de la lista de usuarios
            text = "Forked ${users.size} veces",
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(modifier = Modifier.width(8.dp))

        // CAMBIO 4: Itera directamente sobre la lista de usuarios
        users.take(2).forEach { user -> // 'user' ya no es nullable

            val pxSize = with(density) { avatarSize.roundToPx() }

            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(user.userPhotoPathRemote.ifBlank { "https://picsum.photos/seed/profile/150/150" })
                        .size(Size(pxSize, pxSize))
                        .crossfade(true)
                        .build()
                ),
                contentDescription = "Foto de perfil de ${user.userName} ${user.userLastName}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width((-8).dp))
        }

        // CAMBIO 5: Compara con el tamaño de la lista de usuarios
        if (users.size > 2) {
            Text(
                text = "+${users.size - 2}",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}
