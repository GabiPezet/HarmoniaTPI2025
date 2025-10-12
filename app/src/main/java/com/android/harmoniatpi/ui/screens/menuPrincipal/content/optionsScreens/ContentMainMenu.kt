package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    showCloseSessionDialog: () -> Unit
) {

    if (drawerState.isOpen) {
        BackHandler {
            drawerViewModel.updateUserPreferences()
            onCloseDrawer()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(MaterialTheme.colorScheme.onSecondaryContainer)
    ) {
        // Header
        UserProfileCard(
            userName = uiState.userEmail.ifEmpty { "Pepe ArgEnTo" },
            userId = uiState.userID,
            onCloseDrawer = {
                drawerViewModel.updateUserPreferences()
                onCloseDrawer()
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedContent(
            targetState = true,
            transitionSpec = {
                fadeIn(tween(300)).togetherWith(fadeOut(tween(300)))
            },
            label = "BottomSheetTransition"
        ) {
            if (it) {
                // Contenedor principal
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    // Sección superior con las opciones principales
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                    ) {

                        MenuOptionItem(
                            icon = Icons.Default.Settings,
                            text = "Preferencias",
                            onClick = { drawerViewModel.changeOptionsMenu(OptionsMenu.USER_PREFERENCES_SCREEN) },
                            drawable = R.drawable.settings_24dp
                        )


                    }

                    // Sección inferior con "Cerrar sesión"
                    Column {
                        MenuOptionItem(
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            text = stringResource(R.string.cerrar_sesion),
                            onClick = { showCloseSessionDialog() }
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
    userId: String,
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
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = MaterialTheme.colorScheme.background.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "User Profile",
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "ID: $userId",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
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
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.primaryContainer
        )
    }
}

@Composable
private fun MenuOptionItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    drawable: Int? = null
) {
    val iconColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.secondary

    Row(
        modifier = Modifier
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
            color = textColor,
            modifier = Modifier.weight(1f)
        )

    }
}