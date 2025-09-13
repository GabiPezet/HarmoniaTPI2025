package com.android.harmoniatpi.ui.screens.loginScreen.util

import android.content.Context
import android.widget.Toast
import com.android.harmoniatpi.R

fun showPermissionsDeniedMessage(context: Context) {
    Toast.makeText(
        context,
        context.getString(R.string.login_screen_messageNotPermissionGranted),
        Toast.LENGTH_LONG
    ).show()
    openAppSettings(context)
}