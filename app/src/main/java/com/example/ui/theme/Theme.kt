package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = MonoWhite,
    onPrimary = MonoBlack,
    primaryContainer = MonoMediumDark,
    onPrimaryContainer = MonoWhite,
    secondary = MonoLightGrey,
    onSecondary = MonoBlack,
    secondaryContainer = MonoDarkGrey,
    onSecondaryContainer = MonoWhite,
    tertiary = MonoLightGrey,
    onTertiary = MonoBlack,
    background = MonoBlack,
    onBackground = MonoWhite,
    surface = MonoDarkGrey,
    onSurface = MonoWhite,
    surfaceVariant = MonoMediumDark,
    onSurfaceVariant = MonoLightGrey,
    outline = MonoBorderDark,
    outlineVariant = MonoSubtleBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = MonoBlack,
    onPrimary = MonoWhite,
    primaryContainer = MonoVeryLightGrey,
    onPrimaryContainer = MonoBlack,
    secondary = MonoGrey,
    onSecondary = MonoWhite,
    secondaryContainer = MonoVeryLightGrey,
    onSecondaryContainer = MonoBlack,
    tertiary = MonoGrey,
    onTertiary = MonoWhite,
    background = MonoWhite,
    onBackground = MonoBlack,
    surface = MonoOffWhite,
    onSurface = MonoBlack,
    surfaceVariant = MonoVeryLightGrey,
    onSurfaceVariant = MonoGrey,
    outline = MonoBorderLight,
    outlineVariant = MonoSubtleBorderLight
)

@Composable
fun BedrockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

