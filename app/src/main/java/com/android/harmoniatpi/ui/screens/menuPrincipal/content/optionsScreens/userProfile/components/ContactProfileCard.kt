package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.harmoniatpi.R
import com.android.harmoniatpi.domain.model.userPreferences.ContactData
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.MenuUiState
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.viewmodel.DrawerContentViewModel

@Composable
fun ContactProfileCard(
    contactData: ContactData,
    onUpdateContact: (ContactData) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Información de contacto",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            ContactField(label = "WhatsApp",
                value = contactData.whatsapp,
                iconPainter = painterResource(R.drawable.ic_whatsapp_circle_logo)) {
                onUpdateContact(contactData.copy(whatsapp = it))
            }
            ContactField(label = "Instagram",
                value = contactData.instagram,
                iconPainter = painterResource(R.drawable.ic_instagram_circle_logo)) {
                onUpdateContact(contactData.copy(instagram = it))
            }
            ContactField(    label = "Facebook",
                value = contactData.facebook,
                iconPainter = painterResource(R.drawable.ic_facebook_circle_logo)) {
                onUpdateContact(contactData.copy(facebook = it))
            }
            ContactField(label = "TikTok",
                value = contactData.tiktok,
                iconPainter = painterResource(R.drawable.ic_tiktok_logo)) {
                onUpdateContact(contactData.copy(tiktok = it))
            }
            ContactField(    label = "Email",
                value = contactData.contactMail,
                iconPainter = painterResource(R.drawable.ic_google_circle_logo)) {
                onUpdateContact(contactData.copy(contactMail = it))
            }
        }
    }
}

