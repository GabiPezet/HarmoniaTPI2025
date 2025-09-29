package com.android.harmoniatpi.domain.model

import com.android.harmoniatpi.data.database.entities.UserPreferencesEntity
import com.android.harmoniatpi.di.util.JsonUtils
import com.android.harmoniatpi.domain.model.userPreferences.AppTheme
import com.android.harmoniatpi.ui.screens.notificationScreen.model.NotificationHarmonia

data class UserPreferences(
    val userID: String,
    val userEmail: String,
    val userName : String = "User",
    val userLastName : String = "LastName",
    val appTheme: AppTheme,
    val notificationList: List<NotificationHarmonia>,
    val newNotification: Boolean
) {
    fun toDataBase(jsonUtils: JsonUtils) =
        UserPreferencesEntity(
            userID = userID,
            userEmail = userEmail,
            userName = userName,
            userLastName = userLastName,
            appTheme = appTheme,
            notificationList = jsonUtils.encodeToJson(notificationList),
            newNotification = newNotification
        )
}
