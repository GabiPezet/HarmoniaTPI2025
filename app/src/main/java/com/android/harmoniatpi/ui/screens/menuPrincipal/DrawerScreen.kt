package com.android.harmoniatpi.ui.screens.menuPrincipal

import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable

@Composable
fun DrawerScreen(
    drawerState: DrawerState,
    drawerContent: @Composable () -> Unit,
    screenContent: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = drawerContent,
        gesturesEnabled = false
    ) {
        screenContent()
    }
}