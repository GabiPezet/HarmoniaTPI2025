package com.android.harmoniatpi.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.harmoniatpi.di.util.JsonUtils
import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.userPreferences.AppTheme
import com.android.harmoniatpi.ui.screens.notificationScreen.model.NotificationHarmonia

@Entity(tableName = "UserPreferencesTable")
data class UserPreferencesEntity(
    @PrimaryKey
    val userID: String,
    @ColumnInfo
    val userEmail: String,
    @ColumnInfo
    val userName: String = "User",
    @ColumnInfo
    val userLastName: String = "LastName",
    @ColumnInfo
    val userPhotoPath: String = "",
    @ColumnInfo
    val appTheme: AppTheme = AppTheme.LIGHT,
    @ColumnInfo
    val notificationList: String = "",
    @ColumnInfo
    val newNotification: Boolean = false
) {
    fun toDomain(jsonUtils: JsonUtils) = UserPreferences(
        userID = userID,
        userEmail = userEmail,
        userName = userName,
        userLastName = userLastName,
        userPhotoPath = userPhotoPath,
        appTheme = appTheme,
        notificationList = if (notificationList.isNotBlank()) {
            jsonUtils.decodeJsonToListObject<NotificationHarmonia>(notificationList)
        } else {
            emptyList()
        },
        newNotification = newNotification
    )

}
