package com.android.harmoniatpi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.core.HamoniaServices
import com.android.harmoniatpi.ui.core.NavigationWrapper
import com.android.harmoniatpi.ui.core.theme.HarmoniaTPITheme
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.viewmodel.DrawerContentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, HamoniaServices::class.java)
        createSyncNotificationChannel(this)
        setContent {
            val drawerViewModel: DrawerContentViewModel = hiltViewModel()
            val appConfigState by drawerViewModel.uiState.collectAsState()
            HarmoniaTPITheme(darkTheme = appConfigState.appTheme.value) {
                Scaffold { innerPadding ->
                    NavigationWrapper(
                        innerPadding = innerPadding,
                        drawerViewModel = drawerViewModel,
                        startHarmoniaServices = { startHarmoniaServices(intent) },
                        stopHarmoniaServices = { stopHarmoniaServices(intent) }
                    )
                }
            }
        }
    }

    fun startHarmoniaServices(intent: Intent) {
        startForegroundService(intent)
    }

    fun stopHarmoniaServices(intent: Intent) {
        stopService(intent)
    }

    fun createSyncNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            getString(R.string.harmonia_service_sync_channel),
            getString(R.string.harmonia_service_sincronizacion),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.harmonia_service_sincronizacion_automatica_de_datos)
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
