package com.android.harmoniatpi.ui.core.navigation.bottomNavigationBar

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.android.harmoniatpi.ui.core.navigation.BottomBarRoutes
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.CommunityScreen
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.ProjectsScreen

@Composable
fun NavigationBottomWrapper(
    navController: NavHostController,
    drawerState: DrawerState,
    onExitApp: () -> Unit,
    onNavigateToProjectManagement: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = BottomBarRoutes.CommunityScreenRoute
    ) {
        composable<BottomBarRoutes.CommunityScreenRoute> { CommunityScreen(onExitApp = onExitApp,drawerState = drawerState) }
        composable<BottomBarRoutes.ProjectsScreenRoute> {
            ProjectsScreen(
                onNavigateToProjectManagement = { onNavigateToProjectManagement() } //solo para prueba
            )
        }

    }
}