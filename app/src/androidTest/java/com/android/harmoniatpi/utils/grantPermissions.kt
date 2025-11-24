package com.android.harmoniatpi.utils

import android.Manifest.permission
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry

fun grantPermissions() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val packageName = context.packageName
    val permissions = buildList {
        add(permission.RECORD_AUDIO)
        add(permission.CAMERA)
        add(permission.ACCESS_FINE_LOCATION)
        add(permission.CALL_PHONE)
        add(permission.READ_PHONE_STATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(permission.POST_NOTIFICATIONS)
            add(permission.READ_MEDIA_IMAGES)
            add(permission.READ_MEDIA_VIDEO)
            add(permission.READ_MEDIA_AUDIO)
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            add(permission.READ_EXTERNAL_STORAGE)
            add(permission.WRITE_EXTERNAL_STORAGE)
        } else {
            add(permission.READ_EXTERNAL_STORAGE)
        }
    }
    permissions.forEach { permission ->
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("pm grant $packageName $permission")
            .close()
    }
}

