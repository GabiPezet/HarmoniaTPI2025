package com.android.harmoniatpi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import com.android.harmoniatpi.ui.core.NavigationWrapper
import com.android.harmoniatpi.ui.core.theme.HarmoniaTPITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HarmoniaTPITheme {
                Scaffold { innerPadding ->
                    NavigationWrapper(innerPadding = innerPadding)
                }
            }
        }
    }
}
