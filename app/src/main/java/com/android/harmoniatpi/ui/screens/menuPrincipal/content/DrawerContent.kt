package com.android.harmoniatpi.ui.screens.menuPrincipal.content

import android.annotation.SuppressLint
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.harmoniatpi.ui.components.ShowConfirmationDialog
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.MenuUiState
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.OptionsMenu
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.contentMainMenu.ContentMainMenu
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.helpScreen.HelpScreen
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.myPostScreen.MyPostsScreen
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userPreferencesScreen.UserPreferencesScreen
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile.UserDetailProfile
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.viewmodel.DrawerContentViewModel

@SuppressLint("ContextCastToActivity")
@Composable
fun DrawerContent(
    innerPadding: PaddingValues,
    drawerViewModel: DrawerContentViewModel,
    drawerState: DrawerState,
    onCloseDrawer: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onLogOutNavigateToLogin: () -> Unit,
    onNavigateToFriends: () -> Unit,
    navigateToPaymentMarketScreen: () -> Unit,
    navigateToProjectScreen : () -> Unit
) {
    val scrollState = rememberScrollState()
    val uiState by drawerViewModel.uiState.collectAsState()

    var showCloseSessionDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.logOutSuccess) {
        if (uiState.logOutSuccess) {
            onLogOutNavigateToLogin()
            drawerViewModel.resetLogOutSuccess()
        }
    }

    ShowConfirmationDialog(
        modifier = Modifier.testTag("showCloseSessionDialog"),
        show = showCloseSessionDialog,
        onDismiss = { showCloseSessionDialog = false },
        onConfirm = {
            drawerViewModel.updateUserPreferences()
            drawerViewModel.logOutUser()
        },
        title = "Confirmación de cierre de sesión",
        message = "¿Seguro que quieres cerrar sesión?",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        DrawerScreenContent(
            optionsMenu = uiState.optionsMenu,
            drawerViewModel = drawerViewModel,
            uiState = uiState,
            drawerState = drawerState,
            onCloseDrawer = onCloseDrawer,
            onNavigateToNotifications = { onNavigateToNotifications() },
            scrollState = scrollState,
            innerPadding = innerPadding,
            showCloseSessionDialog = { showCloseSessionDialog = true },
            navigateToProjectScreen = navigateToProjectScreen,
            navigateToPaymentMarketScreen = { navigateToPaymentMarketScreen() }
        )
    }
}

@Composable
fun DrawerScreenContent(
    optionsMenu: OptionsMenu,
    drawerViewModel: DrawerContentViewModel,
    uiState: MenuUiState,
    drawerState: DrawerState,
    onCloseDrawer: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    scrollState: ScrollState,
    innerPadding: PaddingValues,
    showCloseSessionDialog: () -> Unit,
    navigateToProjectScreen: () -> Unit,
    navigateToPaymentMarketScreen: () -> Unit
) {
    when (optionsMenu) {
        OptionsMenu.MAIN_CONTENT_SCREEN -> {
            ContentMainMenu(
                drawerViewModel = drawerViewModel,
                uiState = uiState,
                drawerState = drawerState,
                onCloseDrawer = onCloseDrawer,
                scrollState = scrollState,
                innerPadding = innerPadding,
                onNavigateToNotifications = onNavigateToNotifications,
                showCloseSessionDialog = showCloseSessionDialog,
                navigateToPaymentMarketScreen = { navigateToPaymentMarketScreen() },
            )
        }

        OptionsMenu.USER_PREFERENCES_SCREEN -> {
            UserPreferencesScreen(
                viewModel = drawerViewModel,
                uiState = uiState
            )
        }

        OptionsMenu.USER_PROFILE -> {
            UserDetailProfile(
                viewModel = drawerViewModel,
                uiState = uiState,
                innerPadding = innerPadding,
                onNavigateBack = { drawerViewModel.changeOptionsMenu(OptionsMenu.MAIN_CONTENT_SCREEN) },
                onGoToStudio = { navigateToProjectScreen() }
            )
        }

        OptionsMenu.MY_POSTS_SCREEN -> {
            MyPostsScreen(
                viewModel = drawerViewModel
            )
        }

        OptionsMenu.HELP_SCREEN -> {
            HelpScreen(viewModel = drawerViewModel)
        }

    }
}