package com.android.harmoniatpi.domain.usecases

import android.util.Log
import com.android.harmoniatpi.data.local.ext.getCurrentDate
import com.android.harmoniatpi.di.util.NotificationHelper
import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.SharedMenuUiState
import com.android.harmoniatpi.ui.screens.notificationScreen.model.NotificationHarmonia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManagerOcasaUseCase @Inject constructor(
    private val notificationHelper: NotificationHelper,
    private val sharedMenuUiState: SharedMenuUiState,
    private val setUserPreferencesUseCase: SetUserPreferencesUseCase
) {

    suspend operator fun invoke(title: String, content: String) {
        val fechaActual = Date().getCurrentDate()

        try {
            notificationHelper.showSimpleNotification(title, content)
        } catch (e: Exception) {
            Log.e("Notification", "Error al mostrar notificación", e)
        }

        val newNoti = NotificationHarmonia(
            id = (0..10000).random(),
            title = title,
            description = content,
            date = fechaActual,
            isNew = true
        )

        sharedMenuUiState.updateState {
            val updatedList = it.notificationsList + newNoti
            it.copy(
                notificationsList = updatedList,
                newNotification = true
            )
        }

        val preferences = UserPreferences(
            userName = sharedMenuUiState.uiState.value.userName,
            userID = sharedMenuUiState.uiState.value.userID,
            userLastName = sharedMenuUiState.uiState.value.userLastName,
            userEmail =  sharedMenuUiState.uiState.value.userEmail,
            userPhotoPath =  sharedMenuUiState.uiState.value.userPhotoPath,
            appTheme = sharedMenuUiState.uiState.value.appTheme,
            notificationList = sharedMenuUiState.uiState.value.notificationsList,
            newNotification = sharedMenuUiState.uiState.value.newNotification,
            instrument = sharedMenuUiState.uiState.value.instrument,
            genres = sharedMenuUiState.uiState.value.genres,
            location = sharedMenuUiState.uiState.value.location,
            rating = sharedMenuUiState.uiState.value.rating
        )

        withContext(Dispatchers.IO) {
            setUserPreferencesUseCase(preferences)
        }
    }
}