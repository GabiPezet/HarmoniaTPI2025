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
}

// BottomNav
@Serializable
sealed class BottomBarRoutes{
    @Serializable
    data object Tab1Screen : BottomBarRoutes()
    @Serializable
    data object Tab2Screen : BottomBarRoutes()
    @Serializable
    data object Tab3Screen : BottomBarRoutes()
    @Serializable
    data object RehearsalRoomRoute : BottomBarRoutes()
}