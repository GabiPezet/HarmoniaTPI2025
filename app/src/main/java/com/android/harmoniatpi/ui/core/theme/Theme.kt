package com.android.harmoniatpi.ui.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

data class ExtendedColorScheme(
    val colorMarcaPrimario: Color,
    val onColorMarcaPrimario: Color,
    val colorMarcaSecundario: Color,
    val onColorMarcaSecundario: Color,
    val colorMarcaSecundario02: Color,
    val onColorMarcaSecundario02: Color,
    val colorMarcaSecundario03: Color,
    val onColorMarcaSecundario03: Color,
    val infoColor: Color,
    val onInfoColor: Color,
    val successColor: Color,
    val onSuccessColor: Color,
    val surfaceDaw: Color,
)

val LightExtendedScheme = ExtendedColorScheme(
    colorMarcaPrimario = colorMarcaPrimarioLight,
    onColorMarcaPrimario = onColorMarcaPrimarioLight,
    colorMarcaSecundario = colorMarcaSecundarioLight,
    onColorMarcaSecundario = onColorMarcaSecundarioLight,
    colorMarcaSecundario02 = colorMarcaSecundario_02Light,
    onColorMarcaSecundario02 = onColorMarcaSecundario_02Light,
    colorMarcaSecundario03 = colorMarcaSecundario_03Light,
    onColorMarcaSecundario03 = onColorMarcaSecundario_03Light,
    infoColor = infoColorLight,
    onInfoColor = onInfoColorLight,
    successColor = successColorLight,
    onSuccessColor = onSuccessColorLight,
    surfaceDaw = surfaceDawColorLight,
)

val DarkExtendedScheme = ExtendedColorScheme(
    colorMarcaPrimario = colorMarcaPrimarioDark,
    onColorMarcaPrimario = onColorMarcaPrimarioDark,
    colorMarcaSecundario = colorMarcaSecundarioDark,
    onColorMarcaSecundario = onColorMarcaSecundarioDark,
    colorMarcaSecundario02 = colorMarcaSecundario_02Dark,
    onColorMarcaSecundario02 = onColorMarcaSecundario_02Dark,
    colorMarcaSecundario03 = colorMarcaSecundario_03Dark,
    onColorMarcaSecundario03 = onColorMarcaSecundario_03Dark,
    infoColor = infoColorDark,
    onInfoColor = onInfoColorDark,
    successColor = successColorDark,
    onSuccessColor = onSuccessColorDark,
    surfaceDaw = surfaceDawColorDark,
)

// 3. CREA EL COMPOSITION LOCAL
// Este es el "túnel" que pasará tus colores extendidos
private val LocalExtendedColorScheme = staticCompositionLocalOf {
    // Provee valores por defecto para evitar crashes en previews
    LightExtendedScheme
}

private val LightColorScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,


    )
private val DarkColorScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

@Composable
fun HarmoniaTPITheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColorScheme = if (darkTheme) DarkExtendedScheme else LightExtendedScheme

    val baseTypography = Typography()
    val typographyStyle = remember {
        typographyProvider(baseTypography)
    }

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Black,
            darkIcons = false,
            isNavigationBarContrastEnforced = true
        )
    }
    CompositionLocalProvider(LocalExtendedColorScheme provides extendedColorScheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typographyStyle,
            content = content
        )
    }
}

// Esto te permite escribir `HoloTheme.extendedScheme.holoSuccess.container`
object HoloTheme {
    val extendedScheme: ExtendedColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColorScheme.current
}