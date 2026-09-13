package com.example.ui.settings

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.designsystem.JsAgentColors
import com.example.core.designsystem.JsAgentDimens
import com.example.core.designsystem.JsAgentTypography

/**
 * Clean, focused Settings Screen for JS Agent.
 */
@Composable
fun SettingsScreen(
    isFloatingEnabled: Boolean,
    hasOverlayPermission: Boolean,
    hasAccessibilityPermission: Boolean,
    isDebugInspectorEnabled: Boolean,
    onToggleFloating: (Boolean) -> Unit,
    onRequestOverlayPermission: () -> Unit,
    onRequestAccessibilityPermission: () -> Unit,
    onToggleDebugInspector: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JsAgentColors.Background)
            .statusBarsPadding()
            .padding(horizontal = JsAgentDimens.Spacing20),
        verticalArrangement = Arrangement.spacedBy(JsAgentDimens.Spacing20)
    ) {
        // Top Bar
        item {
            Spacer(modifier = Modifier.height(JsAgentDimens.Spacing12))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(JsAgentDimens.Spacing12)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(JsAgentColors.SurfaceElevated)
                        .border(1.dp, JsAgentColors.Border, CircleShape)
                        .testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = JsAgentColors.TextPrimary
                    )
                }

                Text(
                    text = stringResource(R.string.settings_title),
                    style = JsAgentTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = JsAgentColors.TextPrimary
                )
            }
        }

        // Appearance Section (Fixed Dark Matrix Identity)
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = JsAgentColors.Accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.appearance_title),
                            style = JsAgentTypography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = JsAgentColors.TextPrimary
                        )
                    }

                    Text(
                        text = stringResource(R.string.appearance_desc),
                        style = JsAgentTypography.bodyMedium,
                        color = JsAgentColors.TextSecondary
                    )
                }
            }
        }

        // Floating Assistant Section
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
                    verticalArrangement = Arrangement.spacedBy(JsAgentDimens.Spacing16)
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
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = JsAgentColors.Accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.section_floating_assistant),
                                style = JsAgentTypography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = JsAgentColors.TextPrimary
                            )
                        }

                        Switch(
                            checked = isFloatingEnabled && hasOverlayPermission,
                            onCheckedChange = onToggleFloating,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = JsAgentColors.AccentBright,
                                checkedTrackColor = JsAgentColors.AccentMuted,
                                uncheckedThumbColor = JsAgentColors.TextSecondary,
                                uncheckedTrackColor = JsAgentColors.SurfaceHighlight
                            ),
                            modifier = Modifier.testTag("settings_floating_assistant_switch")
                        )
                    }

                    // Permission status line
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(JsAgentDimens.RadiusSmall))
                            .background(JsAgentColors.SurfaceElevated)
                            .border(1.dp, JsAgentColors.BorderSubtle, RoundedCornerShape(JsAgentDimens.RadiusSmall))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Overlay Permission",
                            style = JsAgentTypography.bodyMedium,
                            color = JsAgentColors.TextSecondary
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (hasOverlayPermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (hasOverlayPermission) JsAgentColors.Accent else JsAgentColors.Warning,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (hasOverlayPermission) stringResource(R.string.overlay_ready) else stringResource(R.string.overlay_permission_required),
                                style = JsAgentTypography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (hasOverlayPermission) JsAgentColors.Accent else JsAgentColors.Warning
                            )
                        }
                    }
                }
            }
        }

        // Accessibility Service Section (Phase 2)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = JsAgentDimens.BorderWidthSubtle,
                        color = JsAgentColors.Border,
                        shape = RoundedCornerShape(JsAgentDimens.RadiusCard)
                    )
                    .testTag("settings_accessibility_card"),
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.TouchApp,
                                contentDescription = null,
                                tint = JsAgentColors.Accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.accessibility_status_title),
                                style = JsAgentTypography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = JsAgentColors.TextPrimary
                            )
                        }

                        if (!hasAccessibilityPermission) {
                            Button(
                                onClick = onRequestAccessibilityPermission,
                                modifier = Modifier.height(JsAgentDimens.ButtonHeightCompact),
                                shape = RoundedCornerShape(JsAgentDimens.RadiusSmall),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JsAgentColors.Warning.copy(alpha = 0.2f),
                                    contentColor = JsAgentColors.Warning
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, JsAgentColors.Warning.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "Enable",
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

                    // Permission status line
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(JsAgentDimens.RadiusSmall))
                            .background(JsAgentColors.SurfaceElevated)
                            .border(1.dp, JsAgentColors.BorderSubtle, RoundedCornerShape(JsAgentDimens.RadiusSmall))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Service Status",
                            style = JsAgentTypography.bodyMedium,
                            color = JsAgentColors.TextSecondary
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (hasAccessibilityPermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (hasAccessibilityPermission) JsAgentColors.Accent else JsAgentColors.Warning,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (hasAccessibilityPermission) stringResource(R.string.accessibility_enabled) else stringResource(R.string.accessibility_disabled),
                                style = JsAgentTypography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (hasAccessibilityPermission) JsAgentColors.Accent else JsAgentColors.Warning
                            )
                        }
                    }
                }
            }
        }

        // Developer Structural Inspector Toggle Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = JsAgentDimens.BorderWidthSubtle,
                        color = JsAgentColors.Border,
                        shape = RoundedCornerShape(JsAgentDimens.RadiusCard)
                    )
                    .testTag("settings_inspector_card"),
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
                            Icon(
                                imageVector = Icons.Outlined.BugReport,
                                contentDescription = null,
                                tint = JsAgentColors.Accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.inspector_toggle),
                                style = JsAgentTypography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = JsAgentColors.TextPrimary
                            )
                        }

                        Switch(
                            checked = isDebugInspectorEnabled,
                            onCheckedChange = onToggleDebugInspector,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = JsAgentColors.AccentBright,
                                checkedTrackColor = JsAgentColors.AccentMuted,
                                uncheckedThumbColor = JsAgentColors.TextSecondary,
                                uncheckedTrackColor = JsAgentColors.SurfaceHighlight
                            ),
                            modifier = Modifier.testTag("settings_inspector_switch")
                        )
                    }

                    Text(
                        text = stringResource(R.string.inspector_toggle_desc),
                        style = JsAgentTypography.bodyMedium,
                        color = JsAgentColors.TextSecondary
                    )
                }
            }
        }

        // About Section
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = JsAgentColors.Accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.about_title),
                            style = JsAgentTypography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = JsAgentColors.TextPrimary
                        )
                    }

                    Text(
                        text = stringResource(R.string.app_name) + " - " + stringResource(R.string.app_version),
                        style = JsAgentTypography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = JsAgentColors.Accent
                    )

                    Text(
                        text = stringResource(R.string.about_desc),
                        style = JsAgentTypography.bodyMedium,
                        color = JsAgentColors.TextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(JsAgentDimens.Spacing24))
        }
    }
}
