package com.android.harmoniatpi.ui.screens.loginScreen.util

import kotlin.text.trim

fun checkValidPassword(password: String): Boolean = password.trim().length >= 2