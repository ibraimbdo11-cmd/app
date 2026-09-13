package com.example.ui.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.agent.AgentState
import com.example.core.agent.AgentStatus
import com.example.core.designsystem.JsAgentColors
import com.example.core.designsystem.JsAgentControlRow
import com.example.core.designsystem.JsAgentDimens
import com.example.core.designsystem.JsAgentLogo
import com.example.core.designsystem.JsAgentTypography

/**
 * Floating Control Panel card displayed beside the floating bubble.
 * Designed as a compact, premium Dark Matrix AI Agent dashboard.
 *
 * Requirements:
 * - NO X / Close button. The Floating Bubble itself toggles this panel.
 * - Draggable exclusively via designated Header / Drag Area.
 * - Custom Matrix Square Checkboxes for controls (Agent [□]/[✓], Floating Panel [□]/[✓]).
 * - Compact layout: Status, Controls, Current Question, Target, Activity, Session Stats.
 */
@Composable
fun FloatingPanelContent(
    agentState: AgentState,
    isVisible: Boolean,
    onHeaderDrag: (dx: Float, dy: Float) -> Unit,
    onToggleAgentClick: () -> Unit,
    onToggleFloatingClick: (Boolean) -> Unit
) {
    val isRunning = agentState.status.isRunning
    val scrollState = rememberScrollState()

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                scaleIn(initialScale = 0.92f, animationSpec = tween(180, easing = FastOutSlowInEasing)),
        exit = fadeOut(animationSpec = tween(150, easing = FastOutSlowInEasing)) +
                scaleOut(targetScale = 0.92f, animationSpec = tween(150, easing = FastOutSlowInEasing))
    ) {
        Box(
            modifier = Modifier
                .width(JsAgentDimens.FloatingPanelWidth)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(JsAgentDimens.RadiusPanel),
                    ambientColor = JsAgentColors.AccentGlow,
                    spotColor = Color.Black
                )
                .clip(RoundedCornerShape(JsAgentDimens.RadiusPanel))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            JsAgentColors.SurfaceElevated,
                            JsAgentColors.Surface,
                            JsAgentColors.Background
                        )
                    )
                )
                .border(
                    width = JsAgentDimens.BorderWidthSubtle,
                    color = if (isRunning) JsAgentColors.BorderGlow else JsAgentColors.Border,
                    shape = RoundedCornerShape(JsAgentDimens.RadiusPanel)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(JsAgentDimens.Spacing12)
            ) {
                // ==========================================
                // 1. DEDICATED HEADER & DRAG AREA
                // Dragging from this section moves the panel
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JsAgentDimens.RadiusSmall))
                        .background(JsAgentColors.SurfaceElevated.copy(alpha = 0.6f))
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                onHeaderDrag(dragAmount.x, dragAmount.y)
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Subtle drag handle pill
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(3.5.dp)
                            .clip(CircleShape)
                            .background(JsAgentColors.Accent.copy(alpha = 0.45f))
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            JsAgentLogo(
                                size = 22.dp,
                                isActive = isRunning,
                                showGlow = false
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = JsAgentTypography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = JsAgentColors.TextPrimary
                                )
                                Text(
                                    text = stringResource(R.string.agent_panel_subtitle),
                                    style = JsAgentTypography.labelSmall.copy(fontSize = 9.sp),
                                    color = JsAgentColors.TextSecondary
                                )
                            }
                        }

                        // Compact Drag Label indicator
                        Text(
                            text = "DRAG",
                            style = JsAgentTypography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                            color = JsAgentColors.Accent.copy(alpha = 0.7f),
                            modifier = Modifier
                                .border(0.75.dp, JsAgentColors.Accent.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable content area for compact display
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ==========================================
                    // 2. STATUS CARD
                    // ==========================================
                    val statusDotColor = when (agentState.status) {
                        AgentStatus.STOPPED, AgentStatus.IDLE -> JsAgentColors.StatusStopped
                        AgentStatus.ERROR -> JsAgentColors.Error
                        AgentStatus.READY -> JsAgentColors.Accent.copy(alpha = 0.7f)
                        else -> JsAgentColors.Accent
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(JsAgentDimens.RadiusSmall))
                            .background(JsAgentColors.Surface)
                            .border(1.dp, JsAgentColors.BorderSubtle, RoundedCornerShape(JsAgentDimens.RadiusSmall))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.agent_status_title),
                            style = JsAgentTypography.labelSmall,
                            color = JsAgentColors.TextSecondary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(statusDotColor)
                            )
                            Text(
                                text = "● ${agentState.status.toDisplayString()}",
                                style = JsAgentTypography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = statusDotColor
                            )
                        }
                    }

                    // ==========================================
                    // 3. CONTROLS (Custom Square Checkboxes)
                    // ==========================================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(JsAgentDimens.RadiusSmall))
                            .background(JsAgentColors.Surface)
                            .border(1.dp, JsAgentColors.BorderSubtle, RoundedCornerShape(JsAgentDimens.RadiusSmall))
                            .padding(vertical = 4.dp)
                    ) {
                        // Main Agent Start/Stop Control
                        JsAgentControlRow(
                            label = stringResource(R.string.control_agent),
                            checked = isRunning,
                            onCheckedChange = { onToggleAgentClick() },
                            testTag = "panel_agent_control_checkbox"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(JsAgentColors.BorderSubtle)
                        )

                        // Floating Panel Enabled Control
                        JsAgentControlRow(
                            label = stringResource(R.string.control_floating_panel),
                            checked = agentState.isFloatingAssistantEnabled,
                            onCheckedChange = { onToggleFloatingClick(it) },
                            testTag = "panel_floating_control_checkbox"
                        )
                    }

                    // ==========================================
                    // 4. CURRENT QUESTION
                    // ==========================================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(JsAgentDimens.RadiusSmall))
                            .background(JsAgentColors.Surface)
                            .border(1.dp, JsAgentColors.BorderSubtle, RoundedCornerShape(JsAgentDimens.RadiusSmall))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.current_question_title),
                            style = JsAgentTypography.labelSmall.copy(fontSize = 10.sp),
                            color = JsAgentColors.TextTertiary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = agentState.currentQuestionSnippet ?: stringResource(R.string.no_active_question),
                            style = JsAgentTypography.bodySmall,
                            color = if (agentState.currentQuestionSnippet != null) JsAgentColors.TextPrimary else JsAgentColors.TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // ==========================================
                    // 5. TARGET SUMMARY (Input & Action)
                    // ==========================================
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(JsAgentDimens.RadiusSmall))
                            .background(JsAgentColors.Surface)
                            .border(1.dp, JsAgentColors.BorderSubtle, RoundedCornerShape(JsAgentDimens.RadiusSmall))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Input status
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${stringResource(R.string.target_input)}: ",
                                style = JsAgentTypography.labelSmall,
                                color = JsAgentColors.TextSecondary
                            )
                            Text(
                                text = if (agentState.hasDetectedInput) "✓ Detected" else "— None",
                                style = JsAgentTypography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (agentState.hasDetectedInput) JsAgentColors.Accent else JsAgentColors.TextTertiary
                            )
                        }

                        // Action button status
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${stringResource(R.string.target_action)}: ",
                                style = JsAgentTypography.labelSmall,
                                color = JsAgentColors.TextSecondary
                            )
                            Text(
                                text = if (agentState.hasDetectedAction) "✓ Detected" else "— None",
                                style = JsAgentTypography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (agentState.hasDetectedAction) JsAgentColors.Accent else JsAgentColors.TextTertiary
                            )
                        }
                    }

                    // ==========================================
                    // 6. ACTIVITY
                    // ==========================================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(JsAgentDimens.RadiusSmall))
                            .background(JsAgentColors.Surface)
                            .border(1.dp, JsAgentColors.BorderSubtle, RoundedCornerShape(JsAgentDimens.RadiusSmall))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.activity_title),
                            style = JsAgentTypography.labelSmall.copy(fontSize = 10.sp),
                            color = JsAgentColors.TextTertiary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = agentState.latestActivity,
                            style = JsAgentTypography.bodySmall,
                            color = JsAgentColors.AccentBright,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // ==========================================
                    // 7. SESSION STATISTICS
                    // ==========================================
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(JsAgentDimens.RadiusSmall))
                            .background(JsAgentColors.SurfaceElevated)
                            .border(1.dp, JsAgentColors.BorderSubtle, RoundedCornerShape(JsAgentDimens.RadiusSmall))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.session_title),
                            style = JsAgentTypography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = JsAgentColors.TextPrimary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "${stringResource(R.string.session_solved)}: ${agentState.sessionSolvedCount}",
                                style = JsAgentTypography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = JsAgentColors.Accent
                            )
                            Text(
                                text = "${stringResource(R.string.session_failed)}: ${agentState.sessionFailedCount}",
                                style = JsAgentTypography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (agentState.sessionFailedCount > 0) JsAgentColors.Error else JsAgentColors.TextTertiary
                            )
                        }
                    }
                }
            }
        }
    }
}
