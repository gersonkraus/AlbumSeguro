package com.familiaaco.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    onPrimary = OnPrimaryColor,
    primaryContainer = Color(0xFF7A3A12),
    onPrimaryContainer = Color(0xFFFFE8D0),
    secondary = SecondaryColor,
    secondaryContainer = Color(0xFF1A3D1E),
    onSecondaryContainer = Color(0xFFD4F5D8),
    tertiary = TertiaryColor,
    background = DarkBgColor,
    surface = SurfaceDarkColor,
    surfaceVariant = Color(0xFF3D2518),
    outline = Color(0xFF9C7A65),
    onSurface = Color(0xFFFFF0E6),
    onSurfaceVariant = Color(0xFFD4A882),
    error = ErrorColor,
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = OnPrimaryColor,
    primaryContainer = PrimaryContainerColor,
    onPrimaryContainer = OnPrimaryContainerColor,
    secondary = SecondaryColor,
    secondaryContainer = SecondaryContainerColor,
    onSecondaryContainer = OnSecondaryContainerColor,
    tertiary = TertiaryColor,
    background = LightBgColor,
    surface = SurfaceLightColor,
    surfaceVariant = SurfaceVariantColor,
    outline = OutlineColor,
    onSurface = OnSurfaceColor,
    onSurfaceVariant = OnSurfaceVariantColor,
    error = ErrorColor,
)

@Composable
fun FamiliaAcolhedoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
