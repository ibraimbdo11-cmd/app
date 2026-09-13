package com.example.core.designsystem

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * JS Agent Vector Logo Composable
 * Symbolizes JavaScript + AI Agent + Automation.
 * Highly scalable from 20dp up to 120dp.
 */
@Composable
fun JsAgentLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isActive: Boolean = false,
    showGlow: Boolean = true,
    accentColor: Color = JsAgentColors.Accent,
    baseColor: Color = Color.White
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_glow_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = if (isActive) 0.65f else 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "JS Agent Logo" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val center = Offset(w / 2f, h / 2f)
            val strokeWidth = (w * 0.065f).coerceAtLeast(1.5f)

            // Outer Soft Ambient Glow
            if (showGlow) {
                drawCircle(
                    color = accentColor.copy(alpha = pulseAlpha * 0.4f),
                    radius = w * 0.48f
                )
            }

            // Outer Tech Arc / Orbit
            drawArc(
                color = accentColor.copy(alpha = if (isActive) 0.9f else 0.45f),
                startAngle = -40f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = Offset(w * 0.12f, h * 0.12f),
                size = Size(w * 0.76f, h * 0.76f),
                style = Stroke(width = strokeWidth * 0.65f, cap = StrokeCap.Round)
            )

            // AI Pulse Core Node (Top-right of tech orbit)
            val nodeAngle = Math.toRadians(-40.0)
            val orbitRadius = w * 0.38f
            val nodeX = center.x + (orbitRadius * Math.cos(nodeAngle)).toFloat()
            val nodeY = center.y + (orbitRadius * Math.sin(nodeAngle)).toFloat()

            // AI Node Glow & Dot
            drawCircle(
                color = accentColor.copy(alpha = pulseAlpha),
                radius = strokeWidth * 1.5f,
                center = Offset(nodeX, nodeY)
            )
            drawCircle(
                color = if (isActive) JsAgentColors.AccentBright else accentColor,
                radius = strokeWidth * 0.8f,
                center = Offset(nodeX, nodeY)
            )

            // Stylized Modern JS Glyph
            // J Letter
            val jPath = Path().apply {
                moveTo(w * 0.44f, h * 0.38f)
                lineTo(w * 0.44f, h * 0.58f)
                cubicTo(
                    w * 0.44f, h * 0.68f,
                    w * 0.33f, h * 0.68f,
                    w * 0.33f, h * 0.60f
                )
            }
            drawPath(
                path = jPath,
                color = baseColor,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // S Letter (Cyber Angular)
            val sPath = Path().apply {
                moveTo(w * 0.67f, h * 0.41f)
                lineTo(w * 0.54f, h * 0.41f)
                cubicTo(
                    w * 0.50f, h * 0.41f,
                    w * 0.50f, h * 0.50f,
                    w * 0.57f, h * 0.50f
                )
                cubicTo(
                    w * 0.67f, h * 0.50f,
                    w * 0.67f, h * 0.63f,
                    w * 0.58f, h * 0.63f
                )
                lineTo(w * 0.50f, h * 0.63f)
            }
            drawPath(
                path = sPath,
                color = accentColor,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Subtle Automation Tech Brackets (Terminal vibe < >)
            val bracketPath = Path().apply {
                // Left bracket <
                moveTo(w * 0.22f, h * 0.46f)
                lineTo(w * 0.17f, h * 0.51f)
                lineTo(w * 0.22f, h * 0.56f)

                // Right bracket >
                moveTo(w * 0.78f, h * 0.46f)
                lineTo(w * 0.83f, h * 0.51f)
                lineTo(w * 0.78f, h * 0.56f)
            }
            drawPath(
                path = bracketPath,
                color = accentColor.copy(alpha = 0.5f),
                style = Stroke(
                    width = strokeWidth * 0.5f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
