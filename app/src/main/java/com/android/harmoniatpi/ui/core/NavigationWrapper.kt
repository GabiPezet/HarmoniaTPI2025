package com.android.harmoniatpi.ui.core

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.harmoniatpi.ui.core.navigation.NavigationRoutes.HomeScreenRoute
import com.android.harmoniatpi.ui.core.navigation.NavigationRoutes.LoginScreenRoute
import com.android.harmoniatpi.ui.screens.homeScreen.HomeScreen
import com.android.harmoniatpi.ui.screens.loginScreen.LoginScreen

@Composable
fun NavigationWrapper(innerPadding: PaddingValues) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = LoginScreenRoute) {
        composable<LoginScreenRoute> {
            LoginScreen(
                innerPadding = innerPadding,
                navigateToHome = { navController.navigate(HomeScreenRoute) })
        }
        composable<HomeScreenRoute> {
            HomeScreen(navigateToLogin = { navController.popBackStack() })
        }
    }
}