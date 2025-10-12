package com.android.harmoniatpi.ui.core

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.harmoniatpi.ui.core.navigation.NavigationRoutes
import com.android.harmoniatpi.ui.core.navigation.NavigationRoutes.HomeScreenRoute
import com.android.harmoniatpi.ui.core.navigation.NavigationRoutes.LoginScreenRoute
import com.android.harmoniatpi.ui.core.navigation.NavigationRoutes.RegisterScreenRoute
import com.android.harmoniatpi.ui.screens.collabScreen.CollabScreen
import com.android.harmoniatpi.ui.screens.createProjectScreen.CreateProjectScreen
import com.android.harmoniatpi.ui.screens.homeScreen.HomeScreen
import com.android.harmoniatpi.ui.screens.loginScreen.LoginScreen
import com.android.harmoniatpi.ui.screens.menuPrincipal.DrawerScreen
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.DrawerContent
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.viewmodel.DrawerContentViewModel
import com.android.harmoniatpi.ui.screens.projectManagementScreen.ProjectManagementScreen
import com.android.harmoniatpi.ui.screens.recordingScreen.RecordingScreen
import com.android.harmoniatpi.ui.screens.registerScreen.RegisterScreen
import kotlinx.coroutines.launch

@Composable
fun NavigationWrapper(innerPadding: PaddingValues, drawerViewModel: DrawerContentViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    NavHost(navController = navController, startDestination = LoginScreenRoute) {
        composable<LoginScreenRoute> {
            LoginScreen(
                navigateToHome = {
                    drawerViewModel.start()
                    navController.navigate(HomeScreenRoute)
                },
                navigateToRegister = { navController.navigate(RegisterScreenRoute) })

        }
        composable<RegisterScreenRoute> {
            RegisterScreen(onBackToLogin = { navController.popBackStack() })
        }
        composable<HomeScreenRoute> {
            DrawerScreen(
                drawerState = drawerState,
                drawerContent = {
                    DrawerContent(
                        innerPadding,
                        drawerViewModel,
                        drawerState,
                        onCloseDrawer = {
                            coroutineScope.launch { drawerState.close() }
                        },
                        onLogOutNavigateToLogin = {
                            coroutineScope.launch { drawerState.close() }
                            navController.navigate(LoginScreenRoute) {
                                popUpTo(LoginScreenRoute) {
                                    inclusive = true
                                }
                            }
                        }
                    )

                }, screenContent = {
                    HomeScreen(
                        openDrawerState = {
                            coroutineScope.launch { drawerState.open() }
                        },
                        drawerViewModel = drawerViewModel,
                        onNavigateToCreateProjet = { navController.navigate(NavigationRoutes.CreateProjectScreenRoute) }
                    )

                }
            )

        }

        composable<NavigationRoutes.ProjectManagementScreenRoute> {
            ProjectManagementScreen(
                onNavigateToRecording = {
                    navController.navigate(NavigationRoutes.RecordingScreenRoute)
                },
                onNavigateToCollab = {
                    navController.navigate(NavigationRoutes.CollabScreenRoute)
                }
            )
        }

        // pantalla grabacion
        composable<NavigationRoutes.RecordingScreenRoute> { RecordingScreen() }

        composable<NavigationRoutes.CollabScreenRoute> { CollabScreen() }


        composable<NavigationRoutes.CreateProjectScreenRoute> {
            CreateProjectScreen(onBack={ navController.popBackStack() })
        }
    }
}