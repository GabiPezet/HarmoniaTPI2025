package com.android.harmoniatpi.ui.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val LightColorScheme = lightColorScheme(
    primary = OcasaColors.Primary,
    onPrimary = OcasaColors.OnPrimary,
    primaryContainer = OcasaColors.SurfaceLight,
    secondary = OcasaColors.Secondary,
    onSecondary = OcasaColors.OnSecondary,
    background = OcasaColors.onSecondaryBackgroundLight,
    error = OcasaColors.Error,
    onBackground = OcasaColors.InfoGreen,
    tertiary = OcasaColors.InfoYellow,
    surface = OcasaColors.FloatingLight,
    onSurface = OcasaColors.OnPrimary,
    inverseSurface = OcasaColors.Primary,
    inverseOnSurface = Color.Black,
    surfaceContainerHighest = OcasaColors.toolBar,
    onSecondaryContainer = OcasaColors.BackgroundLight // PrymaryWhite - PrymaryDark

)
private val DarkColorScheme = darkColorScheme(
    primary = OcasaColors.primaryDark,
    onPrimary = OcasaColors.OnPrimary,
    primaryContainer = OcasaColors.primaryContainerDark,
    secondary = OcasaColors.OnSecondary,
    onSecondary = OcasaColors.Secondary,
    background = OcasaColors.onSecondaryBackgroundDark,
    error = OcasaColors.Error,
    onBackground = OcasaColors.InfoGreen,
    tertiary = OcasaColors.InfoYellow,
    surface = OcasaColors.FloatingDark,
    onSurface = OcasaColors.Primary,
    inverseSurface = Color.Black,
    inverseOnSurface = OcasaColors.Primary,
    surfaceContainerHighest = OcasaColors.toolBar,
    onSecondaryContainer = OcasaColors.BackgroundDark

)

@Composable
fun HarmoniaTPITheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val baseTypography = Typography()
    val typographyStyle = remember {
        typographyProvider(baseTypography)
    }

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Black,
            darkIcons = darkTheme,
            isNavigationBarContrastEnforced = true
        )
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typographyStyle,
        content = content
    )
}