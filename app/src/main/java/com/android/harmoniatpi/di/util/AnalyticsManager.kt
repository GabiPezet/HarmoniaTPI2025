package com.android.harmoniatpi.di.util

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import javax.inject.Inject

class AnalyticsManager @Inject constructor(private val analytics: FirebaseAnalytics) {
    fun loginUser(method: String) {
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
            param(FirebaseAnalytics.Param.METHOD, method)
            param(FirebaseAnalytics.Param.SUCCESS, 1L)
        }
    }

    fun openMapScreen(source: String) {
        analytics.logEvent("open_map_screen") {
            param("source", source)
            param("timestamp", System.currentTimeMillis())
        }
    }
}
