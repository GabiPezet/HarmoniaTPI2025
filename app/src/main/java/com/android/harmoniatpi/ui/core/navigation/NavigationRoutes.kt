package com.android.harmoniatpi.ui.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class NavigationRoutes() {
    @Serializable
    data object LoginScreenRoute : NavigationRoutes()

    @Serializable
    data object RegisterScreenRoute : NavigationRoutes()

    @Serializable
    data object HomeScreenRoute : NavigationRoutes()

    @Serializable
    data object RecordingScreenRoute : NavigationRoutes()

    @Serializable
    data object ProjectManagementScreenRoute : NavigationRoutes()

    @Serializable
    data object CollabScreenRoute : NavigationRoutes()

    @Serializable
    data object NotificationScreenRoute : NavigationRoutes()

}

// BottomNav
@Serializable
sealed class BottomBarRoutes{
    @Serializable
    data object CommunityScreenRoute : BottomBarRoutes()
    @Serializable
    data object ProjectsScreenRoute : BottomBarRoutes()
}