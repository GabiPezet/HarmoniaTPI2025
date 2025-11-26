package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.contentMainMenu

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.harmoniatpi.R
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.MenuUiState
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.OptionsMenu
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.viewmodel.DrawerContentViewModel

@Composable
fun ContentMainMenu(
    drawerViewModel: DrawerContentViewModel,
    uiState: MenuUiState,
    drawerState: DrawerState,
    onCloseDrawer: () -> Unit,
    scrollState: ScrollState,
    innerPadding: PaddingValues,
    onNavigateToNotifications: () -> Unit,
    showCloseSessionDialog: () -> Unit,
    navigateToPaymentMarketScreen: () -> Unit,
    onNavigateToProfile: () -> Unit,
) {

    if (drawerState.isOpen) {
        BackHandler {
            drawerViewModel.updateUserPreferences()
            onCloseDrawer()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize().testTag("CONTENT_MAIN_MENU")
            .padding(innerPadding)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header
        UserProfileCard(
            userName = uiState.userName.ifEmpty { "Nombre vacío o error de red." },
            uiState = uiState,
            onCloseDrawer = {
                drawerViewModel.updateUserPreferences()
                onCloseDrawer()
            }
        )

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline
        )

        AnimatedContent(
            targetState = true,
            transitionSpec = {
                fadeIn(tween(300)).togetherWith(fadeOut(tween(300)))
            },
            label = "BottomSheetTransition"
        ) {
            if (it) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                    ) {

                        MenuOptionItem(
                            testTag = "MenuOptionItemProfile",
                            icon = Icons.Default.Person,
                            text = "Perfil",
                            onClick = onNavigateToProfile,
                            drawable = R.drawable.ic_profile
                        )

                        MenuOptionItem(
                            testTag = "MenuOptionItemNotifications",
                            icon = Icons.Default.Favorite,
                            text = "Notificaciones",
                            onClick = { onNavigateToNotifications() },
                            drawable = R.drawable.ic_notificationsprofile
                        )

                        MenuOptionItem(
                            testTag = "MenuOptionItemSettings",
                            icon = Icons.Default.Settings,
                            text = "Configuración",
                            onClick = { drawerViewModel.changeOptionsMenu(OptionsMenu.USER_PREFERENCES_SCREEN) },
                            drawable = R.drawable.ic_configuracion
                        )

                        MenuOptionItem(
                            testTag = "MenuOptionItemMyPosts",
                            icon = Icons.Filled.Groups,
                            text = "Mis publicaciones",
                            onClick = { drawerViewModel.changeOptionsMenu(OptionsMenu.MY_POSTS_SCREEN) }
                        )

                        MenuOptionItem(
                            testTag = "MenuOptionItemPremium",
                            icon = Icons.Filled.Payment,
                            text = "HoloJam Premium",
                            onClick = { navigateToPaymentMarketScreen() }
                        )

                    }

                    // Sección inferior con "Cerrar sesión"
                    Column {
                        MenuOptionItem(
                            testTag = "MenuOptionItemCloseSession",
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            text = stringResource(R.string.cerrar_sesion),
                            onClick = { showCloseSessionDialog() },
                            drawable = R.drawable.ic_logout
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}


@Composable
private fun UserProfileCard(
    userName: String,
    uiState: MenuUiState,
    onCloseDrawer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                val imagePath = uiState.userPhotoPathRemote.ifBlank {
                    uiState.userPhotoPath
                }

                if (imagePath.isNotBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(imagePath),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(
                                BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                                CircleShape
                            ),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Foto de perfil por defecto",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(80.dp)
                            .border(
                                BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                                CircleShape
                            ),
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = uiState.instrument.ifEmpty { "Completa tu instrumento desde perfil." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Box {
                IconButton(
                    onClick = onCloseDrawer,
                    modifier = Modifier.padding(top = 8.dp, end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Drawer",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuOptionItem(
    testTag: String,
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    drawable: Int? = null
) {
    val iconColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground

    Row(
        modifier = Modifier.testTag(testTag)
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            )
            .padding(vertical = 16.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            if (drawable != null) {
                Icon(
                    painter = painterResource(drawable),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = iconColor
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = iconColor
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )

    }
}