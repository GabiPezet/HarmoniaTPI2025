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
    data object ProjectManagementScreenRoute : NavigationRoutes()
    
    @Serializable
    data object NotificationScreenRoute : NavigationRoutes()

    @Serializable
    data class SongVersionsScreenRoute(val projectId: String) : NavigationRoutes()

    @Serializable
    data object FriendsScreenRoute : NavigationRoutes()

}

// BottomNav
@Serializable
sealed class BottomBarRoutes{
    @Serializable
    data object CommunityScreenRoute : BottomBarRoutes()
    @Serializable
    data object RehearsalRoomRoute : BottomBarRoutes()
}