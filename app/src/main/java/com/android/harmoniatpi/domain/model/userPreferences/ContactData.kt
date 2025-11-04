package com.android.harmoniatpi.domain.model.userPreferences

data class ContactData(
    val whatsapp: String = "",
    val instagram: String = "",
    val xAccount: String = "",
    val tiktok: String = "",
    val contactMail: String = ""
)
//TODO: Hablar con Facu para ver si podemos persistir esta info en Firestore o en RTDatabase