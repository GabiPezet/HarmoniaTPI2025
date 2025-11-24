package com.android.harmoniatpi.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.harmoniatpi.data.local.model.UserFirebaseModel
import com.android.harmoniatpi.di.util.JsonUtils
import com.android.harmoniatpi.domain.model.userPreferences.Friend
import com.android.harmoniatpi.domain.model.userPreferences.FriendRequestReceived
import com.android.harmoniatpi.domain.model.userPreferences.FriendRequestSending
import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.domain.model.userPreferences.AppTheme
import com.android.harmoniatpi.domain.model.userPreferences.Post
import com.android.harmoniatpi.ui.screens.notificationScreen.model.NotificationHarmonia

@Entity(tableName = "UserPreferencesTable")
data class UserPreferencesEntity(
    @PrimaryKey
    val userID: String,
    @ColumnInfo
    val userEmail: String,
    @ColumnInfo
    val userName: String,
    @ColumnInfo
    val userLastName: String,
    @ColumnInfo
    val userPhotoPath: String = "",
    @ColumnInfo
    val userPhotoPathRemote: String = "",
    @ColumnInfo
    val appTheme: AppTheme = AppTheme.LIGHT,
    @ColumnInfo
    val notificationList: String = "",
    @ColumnInfo
    val newNotification: Boolean = false,
    @ColumnInfo
    val instrument: String = "",
    @ColumnInfo
    val genres: String = "",
    @ColumnInfo
    val location: String = "",
    @ColumnInfo
    val rating: Float = 0.0f,
    val friendsList: String = "",
    @ColumnInfo
    val projectsList: String = "",
    @ColumnInfo
    val myPostsList: String = "",
    @ColumnInfo
    val friendRequestReceived: String = "",
    @ColumnInfo
    val friendRequestSent: String = "",
    @ColumnInfo
    val subscriptionId: String? = null,
    @ColumnInfo
    val isPremium : Boolean = false,
    @ColumnInfo
    val ratingCount: Int = 0
    ) {
    fun toDomain(jsonUtils: JsonUtils) = UserPreferences(
        userID = userID,
        userEmail = userEmail,
        userName = userName,
        userLastName = userLastName,
        userPhotoPath = userPhotoPath,
        userPhotoPathRemote = userPhotoPathRemote,
        appTheme = appTheme,
        notificationList = if (notificationList.isNotBlank()) {
            jsonUtils.decodeJsonToListObject<NotificationHarmonia>(notificationList)
        } else {
            emptyList()
        },
        newNotification = newNotification,
        instrument = instrument,
        genres = genres,
        location = location,
        rating = rating,

        friendsList = if (friendsList.isNotBlank()) {
            jsonUtils.decodeJsonToListObject<Friend>(friendsList)
        } else {
            emptyList()
        },
        projectsList = if (projectsList.isNotBlank()) {
            jsonUtils.decodeJsonToListObject<Project>(projectsList)
        } else {
            emptyList()
        },
        myPostsList = if (myPostsList.isNotBlank()) {
            jsonUtils.decodeJsonToListObject<Post>(myPostsList)
        } else {
            emptyList()
        },
        friendRequestReceived = if (friendRequestReceived.isNotBlank()) {
            jsonUtils.decodeJsonToListObject<FriendRequestReceived>(friendRequestReceived)
        } else {
            emptyList()
        },
        friendRequestSent = if (friendRequestSent.isNotBlank()) {
            jsonUtils.decodeJsonToListObject<FriendRequestSending>(friendRequestSent)
        } else {
            emptyList()
        },
        ratingCount = ratingCount,
        subscriptionId = subscriptionId,
        isPremium = isPremium
    )

    fun toFirebaseModel() = UserFirebaseModel(
        userID = userID,
        userEmail = userEmail,
        userName = userName,
        userLastName = userLastName,
        userPhotoPath = userPhotoPath,
        userPhotoPathRemote = userPhotoPathRemote,
        appTheme = appTheme.value,
        notificationList = notificationList,
        newNotification = newNotification,
        friendsList = friendsList,
        projectsList = projectsList,
        myPostsList = myPostsList,
        friendRequestReceived = friendRequestReceived,
        friendRequestSent = friendRequestSent,
        instrument = this.instrument,
        genres = this.genres,
        location = this.location,
        rating = this.rating,
        ratingCount = ratingCount,
        subscriptionId = subscriptionId,
        isPremium = isPremium
    )

}