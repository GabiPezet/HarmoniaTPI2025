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
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val container: Color,
    val onContainer: Color
)

data class ExtendedColorScheme(
    val holoColorPrimario: ColorFamily,
    val holoColorSecundario1: ColorFamily,
    val holoColorSecundario2: ColorFamily,
    val holoColorSecundario3: ColorFamily,
    val holoAcento: ColorFamily,
    val holoInfo1: ColorFamily,
    val holoInfo2: ColorFamily,
    val holoInfo3: ColorFamily,
    val holoSuccess: ColorFamily,
    val holoSuccessVariant: ColorFamily
)

private val lightExtendedScheme = ExtendedColorScheme(
    holoColorPrimario = ColorFamily(
        color = holoColorPrimarioLight,
        onColor = onHoloColorPrimarioLight,
        container = holoColorPrimarioContainerLight,
        onContainer = onHoloColorPrimarioContainerLight
    ),
    holoColorSecundario1 = ColorFamily(
        color = holoColorSecundario1Light,
        onColor = onHoloColorSecundario1Light,
        container = holoColorSecundario1ContainerLight,
        onContainer = onHoloColorSecundario1ContainerLight
    ),
    holoColorSecundario2 = ColorFamily(
        color = holoColorSecundario2Light,
        onColor = onHoloColorSecundario2Light,
        container = holoColorSecundario2ContainerLight,
        onContainer = onHoloColorSecundario2ContainerLight
    ),
    holoColorSecundario3 = ColorFamily(
        color = holoColorSecundario3Light,
        onColor = onHoloColorSecundario3Light,
        container = holoColorSecundario3ContainerLight,
        onContainer = onHoloColorSecundario3ContainerLight
    ),
    holoAcento = ColorFamily(
        color = holoAcentoLight,
        onColor = onHoloAcentoLight,
        container = holoAcentoContainerLight,
        onContainer = onHoloAcentoContainerLight
    ),
    holoInfo1 = ColorFamily(
        color = holoInfo1Light,
        onColor = onHoloInfo1Light,
        container = holoInfo1ContainerLight,
        onContainer = onHoloInfo1ContainerLight
    ),
    holoInfo2 = ColorFamily(
        color = holoInfo2Light,
        onColor = onHoloInfo2Light,
        container = holoInfo2ContainerLight,
        onContainer = onHoloInfo2ContainerLight
    ),
    holoInfo3 = ColorFamily(
        color = holoInfo3Light,
        onColor = onHoloInfo3Light,
        container = holoInfo3ContainerLight,
        onContainer = onHoloInfo3ContainerLight
    ),
    holoSuccess = ColorFamily(
        color = holoSuccessLight,
        onColor = onHoloSuccessLight,
        container = holoSuccessContainerLight,
        onContainer = onHoloSuccessContainerLight
    ),
    holoSuccessVariant = ColorFamily(
        color = holoSuccessVariantLight,
        onColor = onHoloSuccessVariantLight,
        container = holoSuccessVariantContainerLight,
        onContainer = onHoloSuccessVariantContainerLight
    )
)

private val darkExtendedScheme = ExtendedColorScheme(
    holoColorPrimario = ColorFamily(
        color = holoColorPrimarioDark,
        onColor = onHoloColorPrimarioDark,
        container = holoColorPrimarioContainerDark,
        onContainer = onHoloColorPrimarioContainerDark
    ),
    holoColorSecundario1 = ColorFamily(
        color = holoColorSecundario1Dark,
        onColor = onHoloColorSecundario1Dark,
        container = holoColorSecundario1ContainerDark,
        onContainer = onHoloColorSecundario1ContainerDark
    ),
    holoColorSecundario2 = ColorFamily(
        color = holoColorSecundario2Dark,
        onColor = onHoloColorSecundario2Dark,
        container = holoColorSecundario2ContainerDark,
        onContainer = onHoloColorSecundario2ContainerDark
    ),
    holoColorSecundario3 = ColorFamily(
        color = holoColorSecundario3Dark,
        onColor = onHoloColorSecundario3Dark,
        container = holoColorSecundario3ContainerDark,
        onContainer = onHoloColorSecundario3ContainerDark
    ),
    holoAcento = ColorFamily(
        color = holoAcentoDark,
        onColor = onHoloAcentoDark,
        container = holoAcentoContainerDark,
        onContainer = onHoloAcentoContainerDark
    ),
    holoInfo1 = ColorFamily(
        color = holoInfo1Dark,
        onColor = onHoloInfo1Dark,
        container = holoInfo1ContainerLight, // OJO: Usaste Light aquí, asumo que era un typo
        onContainer = onHoloInfo1ContainerLight // OJO: Usaste Light aquí, asumo que era un typo
    ),
    holoInfo2 = ColorFamily(
        color = holoInfo2Dark,
        onColor = onHoloInfo2Dark,
        container = holoInfo2ContainerDark,
        onContainer = onHoloInfo2ContainerDark
    ),
    holoInfo3 = ColorFamily(
        color = holoInfo3Dark,
        onColor = onHoloInfo3Dark,
        container = holoInfo3ContainerDark,
        onContainer = onHoloInfo3ContainerDark
    ),
    holoSuccess = ColorFamily(
        color = holoSuccessDark,
        onColor = onHoloSuccessDark,
        container = holoSuccessContainerDark,
        onContainer = onHoloSuccessContainerDark
    ),
    holoSuccessVariant = ColorFamily(
        color = holoSuccessVariantDark,
        onColor = onHoloSuccessVariantDark,
        container = holoSuccessVariantContainerDark,
        onContainer = onHoloSuccessVariantContainerDark
    )
)

// 3. CREA EL COMPOSITION LOCAL
// Este es el "túnel" que pasará tus colores extendidos
private val LocalExtendedColorScheme = staticCompositionLocalOf {
    // Provee valores por defecto para evitar crashes en previews
    lightExtendedScheme
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
    val extendedColorScheme = if (darkTheme) darkExtendedScheme else lightExtendedScheme

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