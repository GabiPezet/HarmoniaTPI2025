package com.android.harmoniatpi.ui.core.navigation.bottomNavigationBar

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.R
import com.android.harmoniatpi.ui.core.navigation.BottomBarRoutes

sealed class BottomBarItem {
    abstract val route: BottomBarRoutes
    abstract val titleRes: String
    abstract val icon: @Composable () -> Unit

    data object CommunityTab : BottomBarItem() {
        override val route: BottomBarRoutes
            get() = BottomBarRoutes.CommunityScreenRoute
        override val titleRes: String
            get() = "Comunidad"
        override val icon: @Composable () -> Unit
            get() = {
                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
    }


    data object ProjectsTab : BottomBarItem() {
        override val route: BottomBarRoutes
            get() = BottomBarRoutes.ProjectsScreenRoute
        override val titleRes: String
            get() = "Proyectos"
        override val icon: @Composable () -> Unit
            get() = {
                Icon(
                    painter = painterResource(R.drawable.ic_harmonyicon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
    }
}

