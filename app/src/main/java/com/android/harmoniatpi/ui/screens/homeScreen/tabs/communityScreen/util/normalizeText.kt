package com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.util

import java.text.Normalizer

fun String.normalizeText(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        .lowercase()
}