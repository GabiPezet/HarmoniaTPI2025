package com.android.harmoniatpi

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AppPackageNameContextTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    /* Testea que el contexto de la app sea el correcto */
    @Test
    fun testAppContext() {
        hiltRule.inject()
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.android.harmoniatpi", appContext.packageName)
    }
}