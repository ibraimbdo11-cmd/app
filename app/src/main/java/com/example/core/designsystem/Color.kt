package com.example.core.designsystem

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * JS Agent Design System Color Palette
 * Premium Dark Matrix AI Identity
 */
object JsAgentColors {
    // Pure Dark Base Colors
    val Background = Color(0xFF080B09)
    val Surface = Color(0xFF101512)
    val SurfaceElevated = Color(0xFF171E19)
    val SurfaceHighlight = Color(0xFF202A23)
    val SurfaceTranslucent = Color(0xE6101512)

    // Borders
    val Border = Color(0xFF1D2821)
    val BorderSubtle = Color(0xFF151E18)
    val BorderGlow = Color(0x5500E676)
    val BorderActive = Color(0xFF00E676)

    // Primary Matrix Accent (Used moderately for Active/Running/AI)
    val Accent = Color(0xFF00E676)
    val AccentBright = Color(0xFF33FF8A)
    val AccentGlow = Color(0x3300E676)
    val AccentMuted = Color(0xFF0B381C)

    // Text & Content Hierarchy
    val TextPrimary = Color(0xFFF1F5F2)
    val TextSecondary = Color(0xFF90A396)
    val TextTertiary = Color(0xFF5A6F62)
    val TextOnAccent = Color(0xFF06140A)

    // Semantic Status Tokens
    val StatusStopped = Color(0xFF75857B)
    val StatusRunning = Color(0xFF00E676)
    val StatusIdle = Color(0xFF90A396)
    val Success = Color(0xFF00E676)
    val Warning = Color(0xFFFFAB00)
    val Error = Color(0xFFFF5252)

    // Glass & Overlay
    val OverlayScrim = Color(0x99000000)
}

val LocalJsAgentColors = staticCompositionLocalOf { JsAgentColors }
