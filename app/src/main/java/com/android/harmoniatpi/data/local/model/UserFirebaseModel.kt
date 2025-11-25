package com.android.harmoniatpi.data.local.model

import com.android.harmoniatpi.data.database.entities.UserPreferencesEntity
import com.android.harmoniatpi.domain.model.userPreferences.AppTheme
import com.google.firebase.firestore.PropertyName

data class UserFirebaseModel(
    val userID: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val userLastName: String = "",
    val userPhotoPath: String = "",
    val userPhotoPathRemote: String = "",
    val appTheme: Boolean = false,
    val notificationList: String = "",
    val newNotification: Boolean = false,
    val friendsList: String = "",
    val projectsList: String = "",
    val myPostsList: String = "",
    val friendRequestReceived: String = "",
    val friendRequestSent: String = "",
    val instrument: String = "",
    val genres: String = "",
    val location: String = "",
    val rating: Float = 0.0f,
    val ratingCount: Int = 0,
    val subscriptionId: String? = null,
    @get:PropertyName("isPremium") // Fuerza a leer del campo "isPremium"
    @set:PropertyName("isPremium") // Fuerza a escribir en el campo "isPremium"
    var isPremium : Boolean = false,

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
        friendRequestSent = friendRequestSent,
        instrument = instrument,
        genres = genres,
        location = location,
        rating = rating,
        ratingCount = ratingCount,
        subscriptionId = subscriptionId,
        isPremium = isPremium
    )
}
