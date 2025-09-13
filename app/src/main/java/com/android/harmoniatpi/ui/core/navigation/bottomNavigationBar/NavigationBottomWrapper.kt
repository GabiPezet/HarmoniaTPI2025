package com.android.harmoniatpi.ui.core.navigation.bottomNavigationBar

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.android.harmoniatpi.ui.core.navigation.BottomBarRoutes
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.Tab1Screen
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.Tab2Screen
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.Tab3Screen
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.rehearsalRoomScreen.RehearsalRoomScreen

@Composable
fun NavigationBottomWrapper(
    navController: NavHostController,
    navigateToHome: () -> Unit
) {
    NavHost(navController = navController, startDestination = BottomBarRoutes.Tab1Screen) {
        composable<BottomBarRoutes.Tab1Screen> { Tab1Screen(navigateToLogin = navigateToHome) }
        composable<BottomBarRoutes.Tab2Screen> { Tab2Screen() }
        composable<BottomBarRoutes.Tab3Screen> { Tab3Screen() }
        composable<BottomBarRoutes.RehearsalRoomRoute> { RehearsalRoomScreen() }
    }
}