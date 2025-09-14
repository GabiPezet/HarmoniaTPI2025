package com.android.harmoniatpi.ui.screens.loginScreen.util

import android.util.Patterns
import kotlin.text.trim

fun checkUserNick(nick: String): Boolean {
    val email = nick.trim()
    return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
}