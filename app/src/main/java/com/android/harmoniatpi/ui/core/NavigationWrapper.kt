package com.android.harmoniatpi.ui.core

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.ui.components.AnimationHorizontalEffect
import com.android.harmoniatpi.ui.core.navigation.NavigationRoutes
import com.android.harmoniatpi.ui.core.navigation.NavigationRoutes.FriendsScreenRoute
import com.android.harmoniatpi.ui.core.navigation.NavigationRoutes.HomeScreenRoute
import com.android.harmoniatpi.ui.core.navigation.NavigationRoutes.LoginScreenRoute
import com.android.harmoniatpi.ui.core.navigation.NavigationRoutes.NotificationScreenRoute
import com.android.harmoniatpi.ui.core.navigation.NavigationRoutes.PaymentMarketScreenRoute
import com.android.harmoniatpi.ui.core.navigation.NavigationRoutes.ProjectManagementScreenRoute
import com.android.harmoniatpi.ui.core.navigation.NavigationRoutes.RegisterScreenRoute
import com.android.harmoniatpi.ui.core.navigation.NavigationRoutes.SongVersionsScreenRoute
import com.android.harmoniatpi.ui.screens.homeScreen.HomeScreen
import com.android.harmoniatpi.ui.screens.loginScreen.LoginScreen
import com.android.harmoniatpi.ui.screens.menuPrincipal.DrawerScreen
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.DrawerContent
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.viewmodel.DrawerContentViewModel
import com.android.harmoniatpi.ui.screens.notificationScreen.NotificationsScreen
import com.android.harmoniatpi.ui.screens.paymentMarketScreen.PaymentMarketScreen
import com.android.harmoniatpi.ui.screens.paymentMarketScreen.viewModel.PaymentMarketViewModel
import com.android.harmoniatpi.ui.screens.paymentResultScreen.PaymentResultScreen
import com.android.harmoniatpi.ui.screens.projectManagementScreen.ProjectManagementScreen
import com.android.harmoniatpi.ui.screens.registerScreen.RegisterScreen
import com.android.harmoniatpi.ui.screens.songVersionsScreen.SongVersionsScreen
import kotlinx.coroutines.launch

@Composable
fun NavigationWrapper(
    innerPadding: PaddingValues,
    drawerViewModel: DrawerContentViewModel,
    startHarmoniaServices: () -> Unit,
    stopHarmoniaServices: () -> Unit,
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    NavHost(navController = navController, startDestination = LoginScreenRoute) {
        composable<LoginScreenRoute> {
            LoginScreen(
                navigateToHome = {
                    drawerViewModel.start()
                    startHarmoniaServices()
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
                        onNavigateToNotifications = {
                            navController.navigate(NotificationScreenRoute)
                        },
                        onLogOutNavigateToLogin = {
                            coroutineScope.launch { drawerState.close() }
                            stopHarmoniaServices()
                            navController.navigate(LoginScreenRoute) {
                                popUpTo(LoginScreenRoute) {
                                    inclusive = true
                                }
                            }
                        },
                        onNavigateToFriends = { navController.navigate(FriendsScreenRoute) },
                        navigateToPaymentMarketScreen = {
                            navController.navigate(
                                PaymentMarketScreenRoute
                            )
                        },
                    )

                }, screenContent = {
                    HomeScreen(
                        openDrawerState = {
                            coroutineScope.launch { drawerState.open() }
                        },
                        drawerState = drawerState,
                        drawerViewModel = drawerViewModel,
                        onNavigateToNotifications = { navController.navigate(NotificationScreenRoute) },
                        onNavigateToProjectManagementScreen = {
                            navController.navigate(
                                ProjectManagementScreenRoute
                            )
                        },
                        onNavigateToVersion = { project: Project ->
                            navController.navigate(SongVersionsScreenRoute(projectId = project.id))
                        }
                    )
                }
            )
        }


        composable<ProjectManagementScreenRoute> {
            AnimationHorizontalEffect(onBackNavigation = { navController.popBackStack() }) {
                ProjectManagementScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onNavigateToPremium = { navController.navigate(PaymentMarketScreenRoute) }
                )
            }
        }
        composable<NotificationScreenRoute> {
            AnimationHorizontalEffect {
                NotificationsScreen(onBack = { navController.popBackStack() })
            }
        }

        composable<SongVersionsScreenRoute> {
            SongVersionsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable<FriendsScreenRoute> {
            SongVersionsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable<PaymentMarketScreenRoute> {
            PaymentMarketScreen(
                onNavigateBack = { navController.popBackStack() },
                onPaymentReturn = { deepLinkUri ->
                    navController.navigate(deepLinkUri)
                }
            )
        }


        composable<NavigationRoutes.PaymentResultScreenRoute>(
            deepLinks = listOf(
                navDeepLink {
                    // El patrón mapea 'collection_status' de la URL externa al campo 'status' de tu data class
                    uriPattern = "harmoniatpi://payment_return?collection_status={status}&payment_id={payment_id}&preapproval_id={preapproval_id}"
                }
            )
        ) { backStackEntry ->

            // 1. Recuperamos el objeto ruta completo de forma segura
            val route = backStackEntry.toRoute<NavigationRoutes.PaymentResultScreenRoute>()

            val rawStatus = route.status
            val paymentId = route.payment_id
            val preapprovalId = route.preapproval_id

            // 2. Lógica de "Preferencia de Suscripción"
            val finalStatus = when {
                !rawStatus.isNullOrBlank() && rawStatus != "null" -> rawStatus
                !preapprovalId.isNullOrBlank() -> "approved"
                else -> "unknown"
            }

            // Para mostrar en pantalla, usamos el ID que haya llegado
            val finalId = route.payment_id ?: route.preapproval_id

            val paymentViewModel: PaymentMarketViewModel = hiltViewModel()

            // 3. Usamos el estado calculado
            LaunchedEffect(finalStatus) {
                paymentViewModel.checkPaymentStatus(finalStatus, finalId)
            }

            PaymentResultScreen(
                status = finalStatus,
                paymentId = finalId,
                onContinue = {
                    navController.navigate(NavigationRoutes.HomeScreenRoute) {
                        popUpTo(NavigationRoutes.HomeScreenRoute) { inclusive = true }
                    }
                }
            )
        }
    }
}