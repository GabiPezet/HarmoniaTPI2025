package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userPreferencesScreen.model

import com.android.harmoniatpi.R


enum class OptionTypePreference(val title: Int, val items: List<Int>) {
    THEME(
        R.string.dialog_type_tema,
        listOf(
            R.string.dialog_type_tema_claro,
            R.string.dialog_type_tema_oscuro
        )
    )
}
