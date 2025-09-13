package com.android.harmoniatpi.ui.screens.loginScreen.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlin.collections.all

fun Context.hasPermissions(permissions: List<String>): Boolean {
    return permissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
}