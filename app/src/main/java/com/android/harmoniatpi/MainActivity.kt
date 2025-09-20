package com.android.harmoniatpi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.ui.core.NavigationWrapper
import com.android.harmoniatpi.ui.core.theme.HarmoniaTPITheme
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.viewmodel.DrawerContentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val drawerViewModel: DrawerContentViewModel = hiltViewModel()
            val appConfigState by drawerViewModel.uiState.collectAsState()
            HarmoniaTPITheme(darkTheme = appConfigState.appTheme.value) {
                Scaffold { innerPadding ->
                    NavigationWrapper(
                        innerPadding = innerPadding,
                        drawerViewModel = drawerViewModel,
                    )
                }
            }
        }
    }
}
