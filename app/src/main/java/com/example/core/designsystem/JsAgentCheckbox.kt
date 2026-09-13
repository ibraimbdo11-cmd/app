package com.example.core.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Premium Matrix Custom Square Checkbox
 * Unchecked: Subtle dark square with crisp outline [ □ ]
 * Checked: Matrix green filled square with crisp checkmark [ ✓ ]
 */
@Composable
fun JsAgentCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    size: Dp = 19.dp,
    enabled: Boolean = true
) {
    val checkmarkScale by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkbox_scale"
    )

    val boxColor by animateColorAsState(
        targetValue = when {
            !enabled -> JsAgentColors.SurfaceHighlight.copy(alpha = 0.5f)
            checked -> JsAgentColors.Accent
            else -> JsAgentColors.SurfaceElevated
        },
        animationSpec = tween(durationMillis = 160),
        label = "checkbox_box_color"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled -> JsAgentColors.BorderSubtle
            checked -> JsAgentColors.AccentBright
            else -> JsAgentColors.Border.copy(alpha = 0.8f)
        },
        animationSpec = tween(durationMillis = 160),
        label = "checkbox_border_color"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(48.dp) // Accessibility min touch target
            .then(
                if (onCheckedChange != null && enabled) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Checkbox,
                        onClick = { onCheckedChange(!checked) }
                    )
                } else Modifier
            )
            .semantics {
                stateDescription = if (checked) "Checked" else "Unchecked"
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(4.dp))
                .background(boxColor)
                .border(
                    width = 1.25.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checkmarkScale > 0.05f) {
                Canvas(
                    modifier = Modifier
                        .size(size * 0.72f)
                        .scale(checkmarkScale)
                ) {
                    val w = this.size.width
                    val h = this.size.height
                    val checkPath = Path().apply {
                        moveTo(w * 0.15f, h * 0.52f)
                        lineTo(w * 0.42f, h * 0.82f)
                        lineTo(w * 0.88f, h * 0.20f)
                    }
                    drawPath(
                        path = checkPath,
                        color = JsAgentColors.TextOnAccent,
                        style = Stroke(
                            width = 2.2.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}

/**
 * Standard Control Row with label on the left and custom square checkbox on the right.
 */
@Composable
fun JsAgentControlRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(JsAgentDimens.RadiusSmall))
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = JsAgentTypography.bodyMedium,
            color = if (enabled) JsAgentColors.TextPrimary else JsAgentColors.TextTertiary
        )

        JsAgentCheckbox(
            checked = checked,
            onCheckedChange = if (enabled) onCheckedChange else null,
            enabled = enabled
        )
    }
}
