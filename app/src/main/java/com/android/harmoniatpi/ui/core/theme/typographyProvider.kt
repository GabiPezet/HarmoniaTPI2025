package com.android.harmoniatpi.ui.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.android.harmoniatpi.R

fun typographyProvider(base: Typography): Typography {
    val interFamily = FontFamily(
        Font(R.font.inter_regular, FontWeight.Normal),
        Font(R.font.inter_medium, FontWeight.Medium),
        Font(R.font.inter_semibold, FontWeight.SemiBold),
        Font(R.font.inter_bold, FontWeight.Bold)
    )

    fun TextStyle.setStyle(weight: FontWeight = FontWeight.Normal) =
        copy(
            fontFamily = interFamily,
            fontWeight = weight
        )
    return Typography(
        displayLarge = base.displayLarge.setStyle(FontWeight.SemiBold),
        displayMedium = base.displayMedium.setStyle(FontWeight.SemiBold),
        displaySmall = base.displaySmall.setStyle(FontWeight.Medium),
        headlineLarge = base.headlineLarge.setStyle(FontWeight.SemiBold),
        headlineMedium = base.headlineMedium.setStyle(FontWeight.Medium),
        headlineSmall = base.headlineSmall.setStyle(FontWeight.Medium),
        titleLarge = base.titleLarge.setStyle(FontWeight.SemiBold),
        titleMedium = base.titleMedium.setStyle(FontWeight.SemiBold),
        titleSmall = base.titleSmall.setStyle(FontWeight.Medium),
        bodyLarge = base.bodyLarge.setStyle(FontWeight.Medium),
        bodyMedium = base.bodyMedium.setStyle(FontWeight.Medium),
        bodySmall = base.bodySmall.setStyle(FontWeight.Medium),
        labelLarge = base.labelLarge.setStyle(FontWeight.Normal),
        labelMedium = base.labelMedium.setStyle(FontWeight.Normal),
        labelSmall = base.labelSmall.setStyle(FontWeight.Normal),
    )
}