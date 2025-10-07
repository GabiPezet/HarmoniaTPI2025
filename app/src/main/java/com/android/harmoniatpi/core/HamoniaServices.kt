package com.android.harmoniatpi.core

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.harmoniatpi.R
import com.android.harmoniatpi.domain.usecases.CheckIsInternetAvailableUseCase
import com.android.harmoniatpi.domain.usecases.NotificationManagerOcasaUseCase
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.SharedMenuUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HamoniaServices : Service() {

    private var wakeLock: PowerManager.WakeLock? = null

    @Inject
    lateinit var sharedMenuUiState: SharedMenuUiState

    @Inject
    lateinit var notificationManger: NotificationManagerOcasaUseCase

    @Inject
    lateinit var checkIsInternetAvailableUseCase: CheckIsInternetAvailableUseCase

    private val serviceScope = CoroutineScope(Job() + Dispatchers.IO)

    private var notificationJob: Job? = null
    private var uiStateObserverJob: Job? = null

    private var showNewNotification: Boolean = false

    override fun onCreate() {
        super.onCreate()
        acquireWakeLock()
        startForegroundService()
        startUiStateObserver()
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationJob?.cancel()
        uiStateObserverJob?.cancel()
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }

    @SuppressLint("WakelockTimeout")
    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as? PowerManager
        requireNotNull(powerManager) { getString(R.string.harmonia_service_powermanager) }
        wakeLock =
            powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                getString(R.string.harmonia_service_ocasa_ocasaservicewakelock)
            )
        wakeLock?.setReferenceCounted(false)

        if (wakeLock?.isHeld == false) {
            wakeLock?.acquire()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundService() {
        val notification =
            NotificationCompat.Builder(this, getString(R.string.harmonia_service_sync_channel))
                .setContentTitle(getString(R.string.harmonia_service_sincronizacion_activa))
                .setContentText(getString(R.string.harmonia_service_actualizando_datos_en_segundo_plano))
                .setSmallIcon(R.drawable.ic_harmonyicon)
                .setOngoing(true)
                .build()

        startForeground(1, notification)
    }

    private fun startUiStateObserver() {
        uiStateObserverJob = CoroutineScope(Dispatchers.IO).launch {
            sharedMenuUiState.uiState.collect { state ->

                if (state.showNewNotification != showNewNotification) {
                    showNewNotification = state.showNewNotification
                    startNotificationJob()
                }
            }
        }
    }

    private fun startNotificationJob() {
        notificationJob?.cancel()

        if (showNewNotification) {
            notificationJob = CoroutineScope(Dispatchers.IO).launch {
                val listOfTitle = listOf(
                    "🎸 Nueva colaboración disponible",
                    "🥁 Comentario en tu proyecto",
                    "🎧 Nuevo track publicado",
                    "🎶 Invitación a colaborar",
                    "🎤 Noticia de la comunidad",
                    "🎹 Proyecto destacado del día"
                )

                val listOfContent = listOf(
                    "Tu amigo Alex subió una nueva base de batería al proyecto 'Groove Jam'.",
                    "Marina comentó tu publicación: '¡Increíble mezcla, amé el bajo!'",
                    "Se publicó un nuevo track en la comunidad: 'Chillwave Nights'. Dale una escucha.",
                    "Sofía te invitó a colaborar en su proyecto 'Indie Vibes'.",
                    "Hay una nueva encuesta en la comunidad: ¿Cuál es tu DAW favorito?",
                    "Tu proyecto 'Jazz Flow' recibió 10 nuevos likes esta semana 🎷."
                )

                val title = listOfTitle.random()
                val content = listOfContent.random()

                notificationManger(title, content)

                sharedMenuUiState.updateState { it.copy(showNewNotification = false) }

                this.cancel()
            }
        } else {
            Log.d("KlyxDevs Notification", "Notificación desactivada")
        }
    }
}