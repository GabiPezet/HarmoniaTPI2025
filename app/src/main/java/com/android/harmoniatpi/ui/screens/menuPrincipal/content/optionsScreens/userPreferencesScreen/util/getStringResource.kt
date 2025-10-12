package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userPreferencesScreen.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun getStringResource(optionTypeString: Int) : String {
    return stringResource(id = optionTypeString)
}