package com.android.harmoniatpi.ui.screens.homeScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.android.harmoniatpi.R
import com.android.harmoniatpi.data.local.ext.findActivity
import com.android.harmoniatpi.ui.components.CircularProgressBar
import com.android.harmoniatpi.ui.components.ShowConfirmationDialog
import com.android.harmoniatpi.ui.components.Toolbar
import com.android.harmoniatpi.ui.core.navigation.bottomNavigationBar.BottomBarItem
import com.android.harmoniatpi.ui.core.navigation.bottomNavigationBar.NavigationBottomWrapper
import com.android.harmoniatpi.ui.screens.homeScreen.viewmodel.HomeScreenViewModel
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.viewmodel.DrawerContentViewModel

@Composable
fun HomeScreen(
    openDrawerState: () -> Unit,
    drawerViewModel: DrawerContentViewModel,
    onNavigateToProjectManagement: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    viewModel : HomeScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val itemsTabs = listOf(
        BottomBarItem.CommunityTab,
        BottomBarItem.ProjectsTab
    )
    var showExitAppDialog by rememberSaveable { mutableStateOf(false) }
    val navControllerNavBar = rememberNavController()
    var currentTabName by remember { mutableStateOf("CommunityScreenRoute") }
    val drawerUiState by drawerViewModel.uiState.collectAsState()
    val activity = LocalContext.current.findActivity()

    ShowConfirmationDialog(
        show = showExitAppDialog,
        onDismiss = { showExitAppDialog = false },
        onConfirm = {
            activity.finishAffinity()
        },
        title = stringResource(R.string.show_confirmation_dialog_exit_confirmation_tittle),
        message = stringResource(R.string.show_confirmation_dialog_exit_confirmation_question),
    )

    if (uiState.isLoading) {
        CircularProgressBar("Cargando")
    }else{
        Scaffold(
            topBar = {
                Toolbar(
                    title = when (currentTabName) {
                        "CommunityScreenRoute" -> "Comunidad"
                        "ProjectsScreenRoute" -> "Proyectos"
                        else -> "Comunidad"
                    },
                    onNotificationClick = { onNavigateToNotifications() },
                    hasNotifications = drawerUiState.newNotification,
                    showMenuPrincipal = true,
                    onMenuClick = openDrawerState,
                    isInternetAvailable = true,
                )
            },
            bottomBar = {
                BottomNavigation(
                    items = itemsTabs,
                    navControllerNavBar = navControllerNavBar,
                    onCurrentScreenChanged = { screen -> currentTabName = screen })
            }
        ) { innerScaffoldPadding ->
            Box(
                modifier = Modifier
                    .padding(innerScaffoldPadding)
            ) {
                NavigationBottomWrapper(
                    navControllerNavBar,
                    onExitApp = { showExitAppDialog = true },
                    onNavigateToProjectManagement = { onNavigateToProjectManagement() })
            }

        }

    }

}

@Composable
fun BottomNavigation(
    items: List<BottomBarItem>,
    navControllerNavBar: NavHostController,
    onCurrentScreenChanged: (String) -> Unit
) {
    val navBackStackEntry by navControllerNavBar.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
        tonalElevation = 16.dp
    ) {
        val currentScreen = currentDestination?.toString()?.substringAfterLast(".")
        LaunchedEffect(currentScreen) {
            currentScreen?.let { onCurrentScreenChanged(it) }
        }

        items.forEach { item ->
            NavigationBarItem(
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    unselectedIconColor = MaterialTheme.colorScheme.secondary
                ),
                icon = item.icon,
                onClick = {
                    navControllerNavBar.navigate(route = item.route) {
                        navControllerNavBar.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }

                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                selected = currentScreen == item.route.toString(),
                label = {
                    Text(
                        text = item.titleRes,
                        color = if (currentScreen == item.route.toString()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        fontSize = 10.sp
                    )
                },
                interactionSource = null,
            )
        }

    }
}