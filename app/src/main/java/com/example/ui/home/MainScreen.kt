package com.example.ui.home

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.agent.AgentState
import com.example.core.agent.AgentStatus
import com.example.core.designsystem.JsAgentColors
import com.example.core.designsystem.JsAgentDimens
import com.example.core.designsystem.JsAgentLogo
import com.example.core.designsystem.JsAgentTypography

/**
 * Premium Dark Matrix AI Main Screen for JS Agent.
 */
@Composable
fun MainScreen(
    agentState: AgentState,
    hasOverlayPermission: Boolean,
    hasAccessibilityPermission: Boolean,
    onToggleAgent: () -> Unit,
    onToggleFloatingAssistant: (Boolean) -> Unit,
    onRequestOverlayPermission: () -> Unit,
    onRequestAccessibilityPermission: () -> Unit,
    onTriggerInspection: () -> Unit,
    onToggleDebugInspector: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRunning = agentState.status.isRunning

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_glow")
    val pulseGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = if (isRunning) 0.8f else 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JsAgentColors.Background)
            .statusBarsPadding()
            .padding(horizontal = JsAgentDimens.Spacing20),
        verticalArrangement = Arrangement.spacedBy(JsAgentDimens.Spacing20)
    ) {
        // Top Bar & Hero Section
        item {
            Spacer(modifier = Modifier.height(JsAgentDimens.Spacing12))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(JsAgentDimens.Spacing12)
                ) {
                    JsAgentLogo(
                        size = 52.dp,
                        isActive = isRunning,
                        showGlow = true
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = JsAgentTypography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = JsAgentColors.TextPrimary
                        )
                        Text(
                            text = stringResource(R.string.app_subtitle),
                            style = JsAgentTypography.bodyMedium,
                            color = JsAgentColors.TextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(JsAgentColors.SurfaceElevated)
                        .border(1.dp, JsAgentColors.Border, CircleShape)
                        .testTag("main_open_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings_title),
                        tint = JsAgentColors.TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Primary Agent Status Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (isRunning) 14.dp else 4.dp,
                        shape = RoundedCornerShape(JsAgentDimens.RadiusCard),
                        ambientColor = JsAgentColors.AccentGlow,
                        spotColor = if (isRunning) JsAgentColors.Accent else Color.Black
                    )
                    .border(
                        width = if (isRunning) JsAgentDimens.BorderWidthActive else JsAgentDimens.BorderWidthSubtle,
                        color = if (isRunning) JsAgentColors.BorderGlow else JsAgentColors.Border,
                        shape = RoundedCornerShape(JsAgentDimens.RadiusCard)
                    )
                    .testTag("agent_status_card"),
                shape = RoundedCornerShape(JsAgentDimens.RadiusCard),
                colors = CardDefaults.cardColors(
                    containerColor = JsAgentColors.Surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(JsAgentDimens.Spacing20),
                    verticalArrangement = Arrangement.spacedBy(JsAgentDimens.Spacing16)
                ) {
                    // Title and status badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.agent_status_title),
                            style = JsAgentTypography.titleMedium,
                            color = JsAgentColors.TextSecondary
                        )

                        // Status Badge Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(JsAgentDimens.RadiusPill))
                                .background(
                                    if (isRunning) JsAgentColors.AccentMuted else JsAgentColors.SurfaceHighlight
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isRunning) JsAgentColors.Accent else JsAgentColors.BorderSubtle,
                                    shape = RoundedCornerShape(JsAgentDimens.RadiusPill)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isRunning) JsAgentColors.StatusRunning else JsAgentColors.StatusStopped
                                    )
                            )
                            Text(
                                text = if (isRunning) stringResource(R.string.status_running) else stringResource(R.string.status_stopped),
                                style = JsAgentTypography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isRunning) JsAgentColors.AccentBright else JsAgentColors.TextSecondary
                            )
                        }
                    }

                    // Current Task Row
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(JsAgentDimens.RadiusMedium))
                            .background(JsAgentColors.SurfaceElevated)
                            .border(1.dp, JsAgentColors.BorderSubtle, RoundedCornerShape(JsAgentDimens.RadiusMedium))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.current_task_title),
                            style = JsAgentTypography.labelSmall,
                            color = JsAgentColors.TextTertiary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = agentState.activeTask ?: stringResource(R.string.no_active_task),
                            style = JsAgentTypography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (agentState.activeTask != null) JsAgentColors.TextPrimary else JsAgentColors.TextSecondary
                        )
                    }

                    // Start / Stop Agent Primary Button
                    Button(
                        onClick = onToggleAgent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(JsAgentDimens.ButtonHeightStandard)
                            .testTag("main_toggle_agent_button"),
                        shape = RoundedCornerShape(JsAgentDimens.RadiusMedium),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) JsAgentColors.SurfaceElevated else JsAgentColors.Accent,
                            contentColor = if (isRunning) JsAgentColors.Error else JsAgentColors.TextOnAccent
                        ),
                        border = if (isRunning) {
                            androidx.compose.foundation.BorderStroke(1.dp, JsAgentColors.Error.copy(alpha = 0.5f))
                        } else null
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRunning) stringResource(R.string.action_stop_agent) else stringResource(R.string.action_start_agent),
                            style = JsAgentTypography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Test Environment / Demo Challenge Card (Phase 4)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = JsAgentColors.Accent.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(JsAgentDimens.RadiusCard)
                    )
                    .testTag("demo_sandbox_card"),
                shape = RoundedCornerShape(JsAgentDimens.RadiusCard),
                colors = CardDefaults.cardColors(
                    containerColor = JsAgentColors.Surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(JsAgentDimens.Spacing20),
                    verticalArrangement = Arrangement.spacedBy(JsAgentDimens.Spacing12)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(JsAgentColors.Accent.copy(alpha = 0.15f))
                                    .border(1.dp, JsAgentColors.Accent.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Code,
                                    contentDescription = null,
                                    tint = JsAgentColors.AccentBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "بيئة الاختبار التجريبية",
                                    style = JsAgentTypography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = JsAgentColors.TextPrimary
                                )
                                Text(
                                    text = "Interactive Test Sandbox",
                                    style = JsAgentTypography.labelSmall,
                                    color = JsAgentColors.Accent
                                )
                            }
                        }
                    }

                    Text(
                        text = "شاشة اختبار تفاعلية لتجربة الـAgent كاملًا من البداية حتى النهاية (فحص السؤال، التفكير، كتابة الكود، التحقق، والانتقال للسؤال التالي).",
                        style = JsAgentTypography.bodyMedium,
                        color = JsAgentColors.TextSecondary
                    )

                    Button(
                        onClick = onOpenDemo,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(JsAgentDimens.ButtonHeightStandard)
                            .testTag("open_demo_screen_button"),
                        shape = RoundedCornerShape(JsAgentDimens.RadiusMedium),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JsAgentColors.Accent,
                            contentColor = JsAgentColors.Background
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Code,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "فتح شاشة الاختبار / Open Test Sandbox",
                            style = JsAgentTypography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Floating Assistant Section Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = JsAgentDimens.BorderWidthSubtle,
                        color = JsAgentColors.Border,
                        shape = RoundedCornerShape(JsAgentDimens.RadiusCard)
                    )
                    .testTag("floating_assistant_card"),
                shape = RoundedCornerShape(JsAgentDimens.RadiusCard),
                colors = CardDefaults.cardColors(
                    containerColor = JsAgentColors.Surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(JsAgentDimens.Spacing20),
                    verticalArrangement = Arrangement.spacedBy(JsAgentDimens.Spacing16)
                ) {
                    // Header with toggle switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(JsAgentDimens.Spacing12)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(JsAgentColors.SurfaceElevated)
                                    .border(1.dp, JsAgentColors.Border, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = JsAgentColors.Accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = stringResource(R.string.section_floating_assistant),
                                style = JsAgentTypography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = JsAgentColors.TextPrimary
                            )
                        }

                        Switch(
                            checked = agentState.isFloatingAssistantEnabled && hasOverlayPermission,
                            onCheckedChange = { isChecked ->
                                onToggleFloatingAssistant(isChecked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = JsAgentColors.AccentBright,
                                checkedTrackColor = JsAgentColors.AccentMuted,
                                uncheckedThumbColor = JsAgentColors.TextSecondary,
                                uncheckedTrackColor = JsAgentColors.SurfaceHighlight
                            ),
                            modifier = Modifier.testTag("main_floating_assistant_switch")
                        )
                    }

                    // Description
                    Text(
                        text = stringResource(R.string.floating_assistant_desc),
                        style = JsAgentTypography.bodyMedium,
                        color = JsAgentColors.TextSecondary
                    )

                    // Overlay Permission Status Indicator
                    if (!hasOverlayPermission) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(JsAgentDimens.RadiusMedium))
                                .background(JsAgentColors.Warning.copy(alpha = 0.08f))
                                .border(1.dp, JsAgentColors.Warning.copy(alpha = 0.35f), RoundedCornerShape(JsAgentDimens.RadiusMedium))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = JsAgentColors.Warning,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = stringResource(R.string.overlay_permission_required),
                                    style = JsAgentTypography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = JsAgentColors.Warning
                                )
                            }

                            Button(
                                onClick = onRequestOverlayPermission,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(JsAgentDimens.ButtonHeightCompact)
                                    .testTag("grant_overlay_permission_button"),
                                shape = RoundedCornerShape(JsAgentDimens.RadiusSmall),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JsAgentColors.Warning.copy(alpha = 0.2f),
                                    contentColor = JsAgentColors.Warning
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, JsAgentColors.Warning.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.grant_permission),
                                    style = JsAgentTypography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(JsAgentDimens.RadiusMedium))
                                .background(JsAgentColors.Accent.copy(alpha = 0.08f))
                                .border(1.dp, JsAgentColors.Accent.copy(alpha = 0.35f), RoundedCornerShape(JsAgentDimens.RadiusMedium))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = JsAgentColors.Accent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.overlay_ready),
                                style = JsAgentTypography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = JsAgentColors.Accent
                            )
                        }
                    }
                }
            }
        }

        // Accessibility Service Section Card (Phase 2)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = JsAgentDimens.BorderWidthSubtle,
                        color = JsAgentColors.Border,
                        shape = RoundedCornerShape(JsAgentDimens.RadiusCard)
                    )
                    .testTag("accessibility_card"),
                shape = RoundedCornerShape(JsAgentDimens.RadiusCard),
                colors = CardDefaults.cardColors(
                    containerColor = JsAgentColors.Surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(JsAgentDimens.Spacing20),
                    verticalArrangement = Arrangement.spacedBy(JsAgentDimens.Spacing16)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(JsAgentDimens.Spacing12)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(JsAgentColors.SurfaceElevated)
                                    .border(1.dp, JsAgentColors.Border, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.TouchApp,
                                    contentDescription = null,
                                    tint = JsAgentColors.Accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.accessibility_status_title),
                                    style = JsAgentTypography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = JsAgentColors.TextPrimary
                                )
                                Text(
                                    text = if (hasAccessibilityPermission) stringResource(R.string.accessibility_enabled) else stringResource(R.string.accessibility_disabled),
                                    style = JsAgentTypography.labelSmall,
                                    color = if (hasAccessibilityPermission) JsAgentColors.Accent else JsAgentColors.StatusStopped
                                )
                            }
                        }

                        if (hasAccessibilityPermission && isRunning) {
                            OutlinedButton(
                                onClick = onTriggerInspection,
                                modifier = Modifier
                                    .height(JsAgentDimens.ButtonHeightCompact)
                                    .testTag("main_inspect_screen_button"),
                                shape = RoundedCornerShape(JsAgentDimens.RadiusSmall),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = JsAgentColors.Accent
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, JsAgentColors.Accent.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "Inspect",
                                    style = JsAgentTypography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = stringResource(R.string.accessibility_desc),
                        style = JsAgentTypography.bodyMedium,
                        color = JsAgentColors.TextSecondary
                    )

                    if (!hasAccessibilityPermission) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(JsAgentDimens.RadiusMedium))
                                .background(JsAgentColors.Warning.copy(alpha = 0.08f))
                                .border(1.dp, JsAgentColors.Warning.copy(alpha = 0.35f), RoundedCornerShape(JsAgentDimens.RadiusMedium))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = JsAgentColors.Warning,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = stringResource(R.string.accessibility_disabled),
                                    style = JsAgentTypography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = JsAgentColors.Warning
                                )
                            }

                            Button(
                                onClick = onRequestAccessibilityPermission,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(JsAgentDimens.ButtonHeightCompact)
                                    .testTag("grant_accessibility_permission_button"),
                                shape = RoundedCornerShape(JsAgentDimens.RadiusSmall),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JsAgentColors.Warning.copy(alpha = 0.2f),
                                    contentColor = JsAgentColors.Warning
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, JsAgentColors.Warning.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = stringResource(R.string.enable_accessibility),
                                    style = JsAgentTypography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(JsAgentDimens.RadiusMedium))
                                .background(JsAgentColors.Accent.copy(alpha = 0.08f))
                                .border(1.dp, JsAgentColors.Accent.copy(alpha = 0.35f), RoundedCornerShape(JsAgentDimens.RadiusMedium))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = JsAgentColors.Accent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.accessibility_enabled),
                                style = JsAgentTypography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = JsAgentColors.Accent
                            )
                        }
                    }
                }
            }
        }

        // Developer Structural Inspector Section Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = JsAgentDimens.BorderWidthSubtle,
                        color = JsAgentColors.Border,
                        shape = RoundedCornerShape(JsAgentDimens.RadiusCard)
                    )
                    .testTag("developer_inspector_card"),
                shape = RoundedCornerShape(JsAgentDimens.RadiusCard),
                colors = CardDefaults.cardColors(
                    containerColor = JsAgentColors.Surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(JsAgentDimens.Spacing20),
                    verticalArrangement = Arrangement.spacedBy(JsAgentDimens.Spacing12)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.inspector_title),
                            style = JsAgentTypography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = JsAgentColors.TextPrimary
                        )

                        Switch(
                            checked = agentState.isDebugInspectorEnabled,
                            onCheckedChange = onToggleDebugInspector,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = JsAgentColors.AccentBright,
                                checkedTrackColor = JsAgentColors.AccentMuted,
                                uncheckedThumbColor = JsAgentColors.TextSecondary,
                                uncheckedTrackColor = JsAgentColors.SurfaceHighlight
                            ),
                            modifier = Modifier.testTag("main_inspector_toggle_switch")
                        )
                    }

                    Text(
                        text = stringResource(R.string.inspector_toggle_desc),
                        style = JsAgentTypography.bodyMedium,
                        color = JsAgentColors.TextSecondary
                    )

                    if (agentState.isDebugInspectorEnabled) {
                        val analysis = agentState.currentAnalysis
                        if (analysis != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(JsAgentDimens.RadiusSmall))
                                    .background(JsAgentColors.SurfaceElevated)
                                    .border(1.dp, JsAgentColors.BorderSubtle, RoundedCornerShape(JsAgentDimens.RadiusSmall))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Package & Metrics
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = analysis.snapshot.packageName.ifEmpty { "Active Window" },
                                        style = JsAgentTypography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = JsAgentColors.Accent
                                    )
                                    Text(
                                        text = "Nodes: ${analysis.snapshot.totalElementsCount}",
                                        style = JsAgentTypography.labelSmall,
                                        color = JsAgentColors.TextSecondary
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Text("Texts: ${analysis.snapshot.textCount}", style = JsAgentTypography.labelSmall, color = JsAgentColors.TextPrimary)
                                    Text("Inputs: ${analysis.snapshot.inputCount}", style = JsAgentTypography.labelSmall, color = JsAgentColors.TextPrimary)
                                    Text("Actions: ${analysis.snapshot.buttonCount}", style = JsAgentTypography.labelSmall, color = JsAgentColors.TextPrimary)
                                }

                                // Question Candidate
                                if (analysis.primaryQuestionCandidate != null) {
                                    val q = analysis.primaryQuestionCandidate
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(JsAgentColors.Background)
                                            .border(1.dp, JsAgentColors.AccentMuted.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .padding(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Problem Candidate",
                                                style = JsAgentTypography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = JsAgentColors.Accent
                                            )
                                            Text(
                                                text = "Score: ${(q.confidenceScore * 100).toInt()}%",
                                                style = JsAgentTypography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = JsAgentColors.AccentBright
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = q.text,
                                            style = JsAgentTypography.bodySmall,
                                            color = JsAgentColors.TextPrimary,
                                            maxLines = 4
                                        )
                                        if (q.matchingSignals.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Signals: ${q.matchingSignals.take(3).joinToString(", ")}",
                                                style = JsAgentTypography.labelSmall,
                                                color = JsAgentColors.TextTertiary
                                            )
                                        }
                                    }
                                }

                                // Primary Input
                                if (analysis.primaryInputCandidate != null) {
                                    val inCand = analysis.primaryInputCandidate
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(JsAgentColors.Background)
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Ranked Input: ${inCand.input.hint ?: inCand.input.id}",
                                                style = JsAgentTypography.labelSmall,
                                                fontWeight = FontWeight.Medium,
                                                color = JsAgentColors.TextPrimary
                                            )
                                            Text(
                                                text = inCand.reasons.take(2).joinToString(", "),
                                                style = JsAgentTypography.labelSmall,
                                                color = JsAgentColors.TextTertiary
                                            )
                                        }
                                        Text(
                                            text = "Score: ${(inCand.score * 100).toInt()}%",
                                            style = JsAgentTypography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = JsAgentColors.Accent
                                        )
                                    }
                                }

                                // Primary Action
                                if (analysis.primaryActionCandidate != null) {
                                    val actCand = analysis.primaryActionCandidate
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(JsAgentColors.Background)
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Action: \"${actCand.button.label ?: "Submit/Next"}\"",
                                                style = JsAgentTypography.labelSmall,
                                                fontWeight = FontWeight.Medium,
                                                color = JsAgentColors.TextPrimary
                                            )
                                            Text(
                                                text = actCand.reasons.take(2).joinToString(", "),
                                                style = JsAgentTypography.labelSmall,
                                                color = JsAgentColors.TextTertiary
                                            )
                                        }
                                        Text(
                                            text = "Score: ${(actCand.score * 100).toInt()}%",
                                            style = JsAgentTypography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = JsAgentColors.Accent
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = if (isRunning) "Scanning active screen..." else "Start Agent to begin live structural inspection.",
                                style = JsAgentTypography.bodySmall,
                                color = JsAgentColors.TextTertiary
                            )
                        }
                    }
                }
            }
        }

        // Architecture Readiness Roadmap Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = JsAgentDimens.BorderWidthSubtle,
                        color = JsAgentColors.Border,
                        shape = RoundedCornerShape(JsAgentDimens.RadiusCard)
                    ),
                shape = RoundedCornerShape(JsAgentDimens.RadiusCard),
                colors = CardDefaults.cardColors(
                    containerColor = JsAgentColors.Surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(JsAgentDimens.Spacing20),
                    verticalArrangement = Arrangement.spacedBy(JsAgentDimens.Spacing12)
                ) {
                    Text(
                        text = stringResource(R.string.architecture_roadmap_title),
                        style = JsAgentTypography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = JsAgentColors.TextPrimary
                    )

                    ModuleStatusRow(
                        icon = Icons.Default.Layers,
                        title = stringResource(R.string.module_overlay),
                        status = stringResource(R.string.module_overlay_status),
                        isComplete = true
                    )

                    ModuleStatusRow(
                        icon = Icons.Outlined.TouchApp,
                        title = stringResource(R.string.module_accessibility),
                        status = stringResource(R.string.module_accessibility_status),
                        isComplete = true
                    )

                    ModuleStatusRow(
                        icon = Icons.Outlined.Code,
                        title = stringResource(R.string.module_solver),
                        status = stringResource(R.string.module_solver_status),
                        isComplete = true
                    )
                }
            }
            Spacer(modifier = Modifier.height(JsAgentDimens.Spacing24))
        }
    }
}

@Composable
private fun ModuleStatusRow(
    icon: ImageVector,
    title: String,
    status: String,
    isComplete: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(JsAgentDimens.RadiusSmall))
            .background(JsAgentColors.SurfaceElevated)
            .border(1.dp, JsAgentColors.BorderSubtle, RoundedCornerShape(JsAgentDimens.RadiusSmall))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isComplete) JsAgentColors.Accent else JsAgentColors.TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                style = JsAgentTypography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = JsAgentColors.TextPrimary
            )
        }

        Text(
            text = status,
            style = JsAgentTypography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isComplete) JsAgentColors.Accent else JsAgentColors.TextSecondary
        )
    }
}
