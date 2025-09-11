package com.android.harmoniatpi.ui.core.navigation.bottomNavigationBar

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.ui.core.navigation.BottomBarRoutes

sealed class BottomBarItem {
    abstract val route: BottomBarRoutes
    abstract val titleRes: String
    abstract val icon: @Composable () -> Unit

    data object Tab1 : BottomBarItem() {
        override val route: BottomBarRoutes
            get() = BottomBarRoutes.Tab1Screen
        override val titleRes: String
            get() = "tab1"
        override val icon: @Composable () -> Unit
            get() = {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
    }

    data object Tab2 : BottomBarItem() {
        override val route: BottomBarRoutes
            get() = BottomBarRoutes.Tab2Screen
        override val titleRes: String
            get() = "tab2"
        override val icon: @Composable () -> Unit
            get() = {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
    }

    data object Tab3 : BottomBarItem() {
        override val route: BottomBarRoutes
            get() = BottomBarRoutes.Tab3Screen
        override val titleRes: String
            get() = "tab3"
        override val icon: @Composable () -> Unit
            get() = {
                Icon(
                    imageVector = Icons.Filled.NotificationsActive,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
    }

    data object Tab4 : BottomBarItem() {
        override val route: BottomBarRoutes
            get() = BottomBarRoutes.Tab4Screen
        override val titleRes: String
            get() = "tab4"
        override val icon: @Composable () -> Unit
            get() = {
                Icon(
                    imageVector = Icons.Filled.CloudDone,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
    }
}

