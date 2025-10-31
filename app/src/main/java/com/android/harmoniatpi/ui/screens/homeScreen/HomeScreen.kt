package com.android.harmoniatpi.ui.screens.homeScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.android.harmoniatpi.R
import com.android.harmoniatpi.data.local.ext.findActivity
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.ui.components.CircularProgressBar
import com.android.harmoniatpi.ui.components.ShowConfirmationDialog
import com.android.harmoniatpi.ui.core.navigation.bottomNavigationBar.BottomBarItem
import com.android.harmoniatpi.ui.core.navigation.bottomNavigationBar.NavigationBottomWrapper
import com.android.harmoniatpi.ui.screens.homeScreen.viewmodel.HomeScreenViewModel
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.viewmodel.DrawerContentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    openDrawerState: () -> Unit,
    drawerState: DrawerState,
    drawerViewModel: DrawerContentViewModel,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProjectManagementScreen: () -> Unit,
    onNavigateToVersion: (Project) -> Unit,
    viewModel: HomeScreenViewModel = hiltViewModel()
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
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Icon(
                            painter = painterResource(R.drawable.ic_harmonyicon),
                            contentDescription = "Logo de la App",
                            // --- CAMBIO 1: Ajustamos el tamaño ---
                            modifier = Modifier.size(62.dp)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = openDrawerState) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Abrir menú"
                            )
                        }
                    },
                    actions = {
                        BadgedBox (
                            badge = {
                                if (drawerUiState.newNotification) {
                                    Badge()
                                }
                            }
                        ) {
                            IconButton(onClick = onNavigateToNotifications) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notificaciones"
                                )
                            }
                        }
                    },
                    // --- CAMBIO 2: Añadimos el color de fondo ---
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
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
                    drawerState = drawerState,
                    onExitApp = { showExitAppDialog = true },
                    onNavigateToProjectManagementScreen = {onNavigateToProjectManagementScreen()},
                    onNavigateToVersion = onNavigateToVersion
                )
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
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        tonalElevation = 16.dp
    ) {
        val currentScreen = currentDestination?.toString()?.substringAfterLast(".")
        LaunchedEffect(currentScreen) {
            currentScreen?.let { onCurrentScreenChanged(it) }
        }

        items.forEach { item ->
            NavigationBarItem(
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
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily
                    )
                },
                interactionSource = null,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.onBackground,
                    unselectedIconColor = MaterialTheme.colorScheme.onBackground,
                    unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                )
            )
        }

    }
}