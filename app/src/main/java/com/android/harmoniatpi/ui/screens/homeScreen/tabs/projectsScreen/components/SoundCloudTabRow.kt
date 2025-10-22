package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.model.ProjectTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundCloudTabRow(
    selectedTab: ProjectTab,
    onTabSelected: (ProjectTab) -> Unit
) {
    val tabs = listOf("Mis Proyectos", "Colaboraciones")
    val selectedIndex = if (selectedTab == ProjectTab.MY_PROJECTS) 0 else 1

    PrimaryTabRow(
        selectedTabIndex = selectedIndex,
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface // Fondo para el sticky header
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedIndex == index,
                onClick = {
                    onTabSelected(if (index == 0) ProjectTab.MY_PROJECTS else ProjectTab.COLLABS)
                },
                text = { Text(title, fontWeight = FontWeight.SemiBold) },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
