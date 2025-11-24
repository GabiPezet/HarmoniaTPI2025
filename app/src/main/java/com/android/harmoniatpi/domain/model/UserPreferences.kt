package com.android.harmoniatpi.domain.model

import com.android.harmoniatpi.data.database.entities.UserPreferencesEntity
import com.android.harmoniatpi.di.util.JsonUtils
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.domain.model.userPreferences.AppTheme
import com.android.harmoniatpi.domain.model.userPreferences.Friend
import com.android.harmoniatpi.domain.model.userPreferences.FriendRequestReceived
import com.android.harmoniatpi.domain.model.userPreferences.FriendRequestSending
import com.android.harmoniatpi.domain.model.userPreferences.Post
import com.android.harmoniatpi.ui.screens.notificationScreen.model.NotificationHarmonia

data class UserPreferences(
    val userID: String,
    val userEmail: String,
    val userPhotoPath: String,
    val userPhotoPathRemote: String = "",
    val userName: String = "User",
    val userLastName: String = "LastName",
    val appTheme: AppTheme,
    val notificationList: List<NotificationHarmonia>,
    val newNotification: Boolean,
    val instrument: String,
    val genres: String,
    val location: String,
    val rating: Float,
    val friendsList: List<Friend> = emptyList(),
    val projectsList: List<Project> = emptyList(),
    val myPostsList: List<Post> = emptyList(),
    val friendRequestReceived: List<FriendRequestReceived> = emptyList(),
    val friendRequestSent: List<FriendRequestSending> = emptyList(),
    val subscriptionId: String? = null,
    val isPremium : Boolean = false
) {
    fun toDataBase(jsonUtils: JsonUtils) =
        UserPreferencesEntity(
            userID = userID,
            userEmail = userEmail,
            userName = userName,
            userLastName = userLastName,
            userPhotoPath = userPhotoPath,
            userPhotoPathRemote = userPhotoPathRemote,
            appTheme = appTheme,
            notificationList = jsonUtils.encodeToJson(notificationList),
            newNotification = newNotification,
            instrument = instrument,
            genres = genres,
            location = location,
            rating = rating,
            friendsList = jsonUtils.encodeToJson(friendsList),
            projectsList = jsonUtils.encodeToJson(projectsList),
            myPostsList = jsonUtils.encodeToJson(myPostsList),
            friendRequestReceived = jsonUtils.encodeToJson(friendRequestReceived),
            friendRequestSent = jsonUtils.encodeToJson(friendRequestSent),
            subscriptionId = subscriptionId,
            isPremium = isPremium
        )
}
