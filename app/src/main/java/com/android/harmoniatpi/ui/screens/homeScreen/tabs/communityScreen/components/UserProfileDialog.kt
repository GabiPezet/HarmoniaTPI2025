package com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.android.harmoniatpi.domain.model.UserPreferences
import androidx.compose.material3.CircularProgressIndicator
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile.components.CompactSymmetricButtons

@SuppressLint("DefaultLocale")
@Composable
fun UserProfileDialog(
    userPreferences: UserPreferences,
    onDismiss: () -> Unit,
    currentUserData: UserPreferences?,
    isSendingFollowRequest: Boolean,
    onFollowClick: (UserPreferences) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header con botón de cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Perfil de Usuario",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Foto de perfil - VERSIÓN CORREGIDA
                ProfileImageSection(userPreferences = userPreferences)

                Spacer(modifier = Modifier.height(16.dp))

                // Nombre completo
                Text(
                    text = "${userPreferences.userName} ${userPreferences.userLastName}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // Email
                Text(
                    text = userPreferences.userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- AÑADE EL BOTÓN DE SEGUIR ---
                if (currentUserData != null && userPreferences.userID != currentUserData.userID) {

                    // Asumiendo que 'friendsList' y 'friendRequestSent' existen
                    // y que 'Friend' y 'FriendRequestSending' tienen un 'userID'
                    val friendsList = currentUserData.friendsList.map { it.id }
                    val requestsSent = currentUserData.friendRequestSent.map { it.toUserID }

                    val isFriend = userPreferences.userID in friendsList
                    val isRequestSent = userPreferences.userID in requestsSent

                    val followButtonText = when {
                        isFriend -> "Siguiendo"
                        isRequestSent -> "Solicitud enviada"
                        else -> "Seguir"
                    }
                    val isButtonEnabled = !isFriend && !isRequestSent && !isSendingFollowRequest

                    CompactSymmetricButtons(
                        leftLabel = followButtonText,
                        rightLabel = "Compartir perfil",
                        onLeftClick = { onFollowClick(userPreferences) },
                        onRightClick = { /* TODO: Compartir */ },
                        // Pasa el estado de carga al botón izquierdo (Seguir)
                        isLeftLoading = isSendingFollowRequest,
                        // Deshabilita si ya es amigo, ya envió solicitud, o está cargando
                        isLeftEnabled = isButtonEnabled
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                // Información del perfil
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Instrumento
                    ProfileInfoItem(
                        icon = Icons.Default.MusicNote,
                        title = "Instrumento",
                        value = userPreferences.instrument.ifEmpty { "No especificado" }
                    )

                    // Géneros musicales
                    ProfileInfoItem(
                        icon = Icons.AutoMirrored.Filled.PlaylistPlay,
                        title = "Géneros",
                        value = userPreferences.genres.ifEmpty { "No especificados" }
                    )

                    // Ubicación
                    ProfileInfoItem(
                        icon = Icons.Default.LocationOn,
                        title = "Ubicación",
                        value = userPreferences.location.ifEmpty { "No especificada" }
                    )

                    // Rating
                    ProfileInfoItem(
                        icon = Icons.Default.Star,
                        title = "Rating",
                        value = String.format("%.1f", userPreferences.rating)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón de cerrar
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

@Composable
fun ProfileImageSection(userPreferences: UserPreferences) {
    val imageUrl = if (userPreferences.userPhotoPathRemote.isNotEmpty()) {
        userPreferences.userPhotoPathRemote
    } else {
        null
    }

    if (imageUrl != null) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userPreferences.userName.firstOrNull()?.toString() ?: "U",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun ProfileInfoItem(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}