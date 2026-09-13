package com.example.ui.overlay

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.core.agent.AgentStatus
import com.example.core.designsystem.JsAgentColors
import com.example.core.designsystem.JsAgentDimens
import com.example.core.designsystem.JsAgentLogo

/**
 * Floating Bubble composable rendered inside the overlay window.
 * Uses the Master Logo, with state-driven subtle pulse animations matching
 * IDLE, READY/SCANNING, ACTIVE/SOLVING, and ERROR states.
 */
@Composable
fun FloatingBubbleContent(
    agentStatus: AgentStatus,
    isPressed: Boolean = false,
    onClick: () -> Unit = {}
) {
    val isWorking = agentStatus.isWorking
    val isError = agentStatus == AgentStatus.ERROR
    val isReadyOrScanning = agentStatus in listOf(AgentStatus.READY, AgentStatus.SCANNING, AgentStatus.ANALYZING)
    val isIdleOrStopped = agentStatus in listOf(AgentStatus.IDLE, AgentStatus.STOPPED)

    // Pulse duration based on state
    val pulseDuration = when {
        isWorking -> 950
        isReadyOrScanning -> 1500
        isError -> 2200
        else -> 2400
    }

    val infiniteTransition = rememberInfiniteTransition(label = "bubble_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = if (isWorking) 1.14f else if (isReadyOrScanning) 1.08f else 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = pulseDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (isWorking) 0.55f else 0.25f,
        targetValue = if (isWorking) 0.15f else 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = pulseDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = tween(durationMillis = 140),
        label = "bubble_press"
    )

    val primaryRingColor = when {
        isError -> JsAgentColors.Error
        isWorking -> JsAgentColors.AccentBright
        isReadyOrScanning -> JsAgentColors.Accent
        else -> JsAgentColors.Accent.copy(alpha = 0.40f)
    }

    val glowColor = when {
        isError -> JsAgentColors.Error.copy(alpha = pulseAlpha)
        else -> JsAgentColors.Accent.copy(alpha = pulseAlpha)
    }

    Box(
        modifier = Modifier
            .size(JsAgentDimens.FloatingBubbleSize + 8.dp)
            .scale(pressScale),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing aura / glow ring
        Box(
            modifier = Modifier
                .size(JsAgentDimens.FloatingBubbleSize + 6.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            glowColor,
                            Color.Transparent
                        )
                    )
                )
        )

        // Core Dark Surface
        Box(
            modifier = Modifier
                .size(JsAgentDimens.FloatingBubbleSize - 4.dp)
                .shadow(
                    elevation = if (isWorking) 14.dp else 6.dp,
                    shape = CircleShape,
                    ambientColor = primaryRingColor.copy(alpha = 0.3f),
                    spotColor = primaryRingColor
                )
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            JsAgentColors.SurfaceElevated,
                            JsAgentColors.Background
                        )
                    )
                )
                .border(
                    width = if (isWorking) JsAgentDimens.BorderWidthActive else JsAgentDimens.BorderWidthSubtle,
                    color = primaryRingColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            JsAgentLogo(
                size = JsAgentDimens.FloatingBubbleInnerLogoSize,
                isActive = isWorking || isReadyOrScanning,
                showGlow = false
            )
        }
    }
}
