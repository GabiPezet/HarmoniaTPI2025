package com.android.harmoniatpi.ui.core

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.harmoniatpi.ui.core.navigation.NavigationRoutes.HomeScreenRoute
import com.android.harmoniatpi.ui.core.navigation.NavigationRoutes.LoginScreenRoute
import com.android.harmoniatpi.ui.core.navigation.NavigationRoutes.RegisterScreenRoute
import com.android.harmoniatpi.ui.screens.homeScreen.HomeScreen
import com.android.harmoniatpi.ui.screens.loginScreen.LoginScreen
import com.android.harmoniatpi.ui.screens.registerScreen.RegisterScreen

@Composable
fun NavigationWrapper(innerPadding: PaddingValues) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = LoginScreenRoute) {
        composable<LoginScreenRoute> {
            LoginScreen(
                innerPadding = innerPadding,
                navigateToHome = { navController.navigate(HomeScreenRoute) },
                navigateToRegister = { navController.navigate(RegisterScreenRoute) })

        }
        composable<RegisterScreenRoute> {
            RegisterScreen(onBackToLogin = { navController.popBackStack() })
        }
        composable<HomeScreenRoute> {
            HomeScreen(navigateToLogin = { navController.popBackStack() })
        }
    }
}