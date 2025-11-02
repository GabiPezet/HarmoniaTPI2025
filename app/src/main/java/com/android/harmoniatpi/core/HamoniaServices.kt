package com.android.harmoniatpi.core

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.android.harmoniatpi.R
import com.android.harmoniatpi.domain.usecases.CheckIsInternetAvailableUseCase
import com.android.harmoniatpi.domain.usecases.NotificationManagerOcasaUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.GetAllPostFromFirebaseDataBaseUseCase
import com.android.harmoniatpi.domain.usecases.roomUseCases.GetMyPostFromDataBaseUseCase
import com.android.harmoniatpi.domain.usecases.roomUseCases.UpdateMyPostFromDataBaseUseCase
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.SharedMenuUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.sample
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

    @Inject
    lateinit var getMyPostFromDataBaseUseCase: GetMyPostFromDataBaseUseCase

    @Inject
    lateinit var updateMyPostFromDataBaseUseCase: UpdateMyPostFromDataBaseUseCase

    @Inject
    lateinit var getAllPostFromFirebaseDataBaseUseCase: GetAllPostFromFirebaseDataBaseUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var postObserverJob: Job? = null
    private var firebaseCollectorJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        acquireWakeLock()
        startForegroundService()
        startPostObserver()
        startFirebaseCollector()
    }

    override fun onDestroy() {
        super.onDestroy()
        postObserverJob?.cancel()
        firebaseCollectorJob?.cancel()
        serviceScope.cancel()
        wakeLock?.takeIf { it.isHeld }?.release()
    }

    @SuppressLint("WakelockTimeout")
    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as? PowerManager
        requireNotNull(powerManager) { getString(R.string.harmonia_service_powermanager) }

        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            getString(R.string.harmonia_service_ocasa_ocasaservicewakelock)
        ).apply {
            setReferenceCounted(false)
            if (!isHeld) acquire()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(
            this,
            getString(R.string.harmonia_service_sync_channel)
        )
            .setContentTitle(getString(R.string.harmonia_service_sincronizacion_activa))
            .setContentText(getString(R.string.harmonia_service_actualizando_datos_en_segundo_plano))
            .setSmallIcon(R.drawable.ic_iconserviceapp)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }


    @OptIn(FlowPreview::class)
    private fun startPostObserver() {
        postObserverJob?.cancel()
        postObserverJob = serviceScope.launch {
            getMyPostFromDataBaseUseCase().distinctUntilChanged().sample(10_000)
                .collect { posts ->
                    posts.forEach { post ->
                        var shouldUpdate = false

                        // --- Nuevo Clone ---
                        if (post.hasNewClone) {
                            val title = "¡Tienes una nueva colaboración!"
                            val content =
                                "Tu projecto '${post.title}' ha recibido nueva clonación."
                            notificationManger(title, content)
                            shouldUpdate = true
                        }

                        // --- Nuevo LIKE ---
                        if (post.hasNewLike) {
                            val title = "👍 ¡Tienes un nuevo like!"
                            val content =
                                "Tu publicación '${post.title}' recibió un nuevo me gusta."
                            notificationManger(title, content)
                            shouldUpdate = true
                        }

                        // --- Nuevo COMENTARIO ---
                        if (post.hasNewComment && post.comments.isNotEmpty()) {
                            val lastComment = post.comments.last()
                            val title = " ${lastComment.name} ${lastComment.lastName}"
                            val content = "Comentó en '${post.title}': \"${lastComment.comment}\""
                            notificationManger(title, content)
                            shouldUpdate = true
                        }

                        // --- Evitar update innecesario ---
                        if (shouldUpdate) {
                            val updatedPost = post.copy(
                                hasNewLike = false,
                                hasNewComment = false,
                                hasNewClone = false
                            )
                            updateMyPostFromDataBaseUseCase(updatedPost)
                        }
                    }
                }
        }
    }

    private fun startFirebaseCollector() {
        firebaseCollectorJob?.cancel()
        firebaseCollectorJob = serviceScope.launch {
            getAllPostFromFirebaseDataBaseUseCase()
                .collect {}
        }
    }

}
