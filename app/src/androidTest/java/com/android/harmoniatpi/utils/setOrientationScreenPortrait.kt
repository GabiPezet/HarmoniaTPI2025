package com.android.harmoniatpi.utils

import android.content.pm.ActivityInfo
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.android.harmoniatpi.MainActivity

fun setOrientationScreenPortrait(composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>) {
    composeTestRule.activityRule.scenario.onActivity {
        it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}