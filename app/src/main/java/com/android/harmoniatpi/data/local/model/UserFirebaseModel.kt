package com.android.harmoniatpi.data.local.model

import com.android.harmoniatpi.data.database.entities.UserPreferencesEntity
import com.android.harmoniatpi.di.util.JsonUtils
import com.android.harmoniatpi.domain.model.userPreferences.AppTheme

data class UserFirebaseModel(
    val userID: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val userLastName: String = "",
    val userPhotoPath: String = "",
    val userPhotoPathRemote: String = "",
    val appTheme: Boolean = false,
    val notificationList:  String = "",
    val newNotification: Boolean = false,
    val friendsList: String = "",
    val projectsList: String = "",
    val myPostsList: String = "",
    val friendRequestReceived: String = "",
    val friendRequestSent: String = ""
) {
    fun toEntity(): UserPreferencesEntity = UserPreferencesEntity(
        userID = userID,
        userEmail = userEmail,
        userName = userName,
        userLastName = userLastName,
        userPhotoPath = userPhotoPath,
        userPhotoPathRemote = userPhotoPathRemote,
        appTheme = if (appTheme) AppTheme.DARK else AppTheme.LIGHT,
        notificationList = notificationList,
        newNotification = newNotification,
        friendsList = friendsList,
        projectsList = projectsList,
        myPostsList = myPostsList,
        friendRequestReceived = friendRequestReceived,
        friendRequestSent = friendRequestSent
    )
}
