package com.example.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Premium Dark Matrix AI Material 3 ColorScheme mapping
 */
private val DarkMatrixColorScheme = darkColorScheme(
    primary = JsAgentColors.Accent,
    onPrimary = JsAgentColors.TextOnAccent,
    primaryContainer = JsAgentColors.AccentMuted,
    onPrimaryContainer = JsAgentColors.AccentBright,
    secondary = JsAgentColors.Accent,
    onSecondary = JsAgentColors.TextOnAccent,
    secondaryContainer = JsAgentColors.SurfaceElevated,
    onSecondaryContainer = JsAgentColors.TextPrimary,
    tertiary = JsAgentColors.AccentBright,
    onTertiary = JsAgentColors.TextOnAccent,
    background = JsAgentColors.Background,
    onBackground = JsAgentColors.TextPrimary,
    surface = JsAgentColors.Surface,
    onSurface = JsAgentColors.TextPrimary,
    surfaceVariant = JsAgentColors.SurfaceElevated,
    onSurfaceVariant = JsAgentColors.TextSecondary,
    outline = JsAgentColors.Border,
    outlineVariant = JsAgentColors.BorderSubtle,
    error = JsAgentColors.Error,
    onError = JsAgentColors.TextPrimary
)

@Composable
fun JsAgentTheme(
    content: @Composable () -> Unit
) {
    // JS Agent has a strictly dedicated Dark Matrix AI identity
    CompositionLocalProvider(
        LocalJsAgentColors provides JsAgentColors
    ) {
        MaterialTheme(
            colorScheme = DarkMatrixColorScheme,
            typography = JsAgentTypography,
            content = content
        )
    }
}
