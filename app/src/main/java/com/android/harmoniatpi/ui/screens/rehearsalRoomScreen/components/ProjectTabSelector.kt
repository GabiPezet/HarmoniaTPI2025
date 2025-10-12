package com.android.harmoniatpi.ui.screens.rehearsalRoomScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.ui.screens.rehearsalRoomScreen.model.ProjectTab


@Composable
fun ProjectTabSelector(
    selectedTab: ProjectTab,
    onTabSelected: (ProjectTab) -> Unit,
    tabHeight: Dp = 40.dp,
    cornerRadius: Dp = 15.dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TabButton(
            "Mis Proyectos",
            selectedTab == ProjectTab.MY_PROJECTS,
            onClick = { onTabSelected(ProjectTab.MY_PROJECTS) },
            modifier = Modifier
                .weight(1f)
                .height(tabHeight),
            cornerRadius = cornerRadius
        )
        Spacer(modifier = Modifier.width(8.dp))
        TabButton(
            "Colaboraciones",
            selectedTab == ProjectTab.COLLABS,
            onClick = { onTabSelected(ProjectTab.COLLABS) },
            modifier = Modifier
                .weight(1f)
                .height(tabHeight),
            cornerRadius = cornerRadius
        )
    }
}