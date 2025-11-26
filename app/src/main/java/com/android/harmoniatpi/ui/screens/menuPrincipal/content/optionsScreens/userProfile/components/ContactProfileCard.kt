package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.domain.model.userPreferences.ContactData

@Composable
fun ContactProfileCard(
    contactData: ContactData,
    onUpdateContact: (ContactData) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var tempContact by remember(contactData) { mutableStateOf(contactData) }
    val scrollState = rememberScrollState()

    // Usamos CardDefaults con colores sutiles
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier
            .padding(20.dp)
            .verticalScroll(scrollState)
        ) {

            // --- Encabezado ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ContactPage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Información de Contacto",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Botón de acción (Editar / Guardar / Cancelar)
                if (!isEditing) {
                    IconButton(onClick = { isEditing = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Row {
                        IconButton(onClick = {
                            tempContact = contactData
                            isEditing = false
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = MaterialTheme.colorScheme.error)
                        }
                        IconButton(onClick = {
                            onUpdateContact(tempContact)
                            isEditing = false
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Guardar", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            // --- Contenido ---
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // WhatsApp (Phone)
                ContactItem(
                    label = "WhatsApp",
                    value = if (isEditing) tempContact.whatsapp else contactData.whatsapp,
                    icon = Icons.Outlined.Phone, // O usa tus drawables aquí
                    isEditing = isEditing,
                    keyboardType = KeyboardType.Phone,
                    onValueChange = { tempContact = tempContact.copy(whatsapp = it) },
                    placeholder = "+54 9 11..."
                )

                // Instagram (Link/Alternate)
                ContactItem(
                    label = "Instagram",
                    value = if (isEditing) tempContact.instagram else contactData.instagram,
                    icon = Icons.Outlined.PhotoCamera,
                    isEditing = isEditing,
                    onValueChange = { tempContact = tempContact.copy(instagram = it) },
                    placeholder = "@usuario"
                )

                // Facebook
                ContactItem(
                    label = "Facebook",
                    value = if (isEditing) tempContact.facebook else contactData.facebook,
                    icon = Icons.Outlined.Public,
                    isEditing = isEditing,
                    onValueChange = { tempContact = tempContact.copy(facebook = it) },
                    placeholder = "/perfil"
                )

                // TikTok
                ContactItem(
                    label = "TikTok",
                    value = if (isEditing) tempContact.tiktok else contactData.tiktok,
                    icon = Icons.Outlined.MusicNote,
                    isEditing = isEditing,
                    onValueChange = { tempContact = tempContact.copy(tiktok = it) },
                    placeholder = "@usuario"
                )

                // Email
                ContactItem(
                    label = "Email",
                    value = if (isEditing) tempContact.contactMail else contactData.contactMail,
                    icon = Icons.Outlined.Email,
                    isEditing = isEditing,
                    keyboardType = KeyboardType.Email,
                    onValueChange = { tempContact = tempContact.copy(contactMail = it) },
                    placeholder = "ejemplo@mail.com"
                )
            }
        }
    }
}

@Composable
private fun ContactItem(
    label: String,
    value: String,
    icon: ImageVector,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    if (isEditing) {
        // MODO EDICIÓN: Icono dentro del TextField (Ahorra espacio)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    } else {
        // MODO LECTURA
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value.ifBlank { "No agregado" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (value.isNotBlank()) FontWeight.Medium else FontWeight.Normal,
                    color = if (value.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

