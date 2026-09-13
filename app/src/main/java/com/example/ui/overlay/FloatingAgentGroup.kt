package com.example.ui.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.core.agent.AgentState

/**
 * Unified Floating Group Composable hosting both the Panel and the Floating Bubble
 * within a single coordinate system.
 *
 * Requirements fulfilled:
 * - Bubble and Panel form a single unified Floating Group.
 * - Bubble is positioned directly in front of and above the top-start corner of the Panel card (zIndex = 30f).
 * - Bubble never hides behind the Panel and cannot drift away.
 * - Single source of truth for isPanelOpen without double-toggle race conditions.
 * - Dragging moves the entire group as a unified entity.
 * - Dragging only initiates from designated Header / Drag Handle or bubble when collapsed.
 */
@Composable
fun FloatingAgentGroup(
    agentState: AgentState,
    isPanelOpen: Boolean,
    onTogglePanel: () -> Unit,
    onGroupDrag: (dx: Float, dy: Float) -> Unit,
    onToggleAgentClick: () -> Unit,
    onToggleFloatingClick: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier.wrapContentSize(),
        contentAlignment = Alignment.TopStart
    ) {
        // 1. Panel Container (Visible when isPanelOpen is true)
        AnimatedVisibility(
            visible = isPanelOpen,
            enter = fadeIn(animationSpec = tween(160)) + scaleIn(initialScale = 0.93f, animationSpec = tween(160)),
            exit = fadeOut(animationSpec = tween(140)) + scaleOut(targetScale = 0.93f, animationSpec = tween(140))
        ) {
            // Generous offset so the Bubble neatly overlaps the top-start corner
            Box(
                modifier = Modifier.padding(top = 18.dp, start = 18.dp)
            ) {
                FloatingPanelContent(
                    agentState = agentState,
                    isVisible = true,
                    onHeaderDrag = onGroupDrag,
                    onToggleAgentClick = onToggleAgentClick,
                    onToggleFloatingClick = onToggleFloatingClick
                )
            }
        }

        // 2. Floating Bubble (Always rendered at Top-Start with high zIndex so it is in front of the Panel)
        Box(
            modifier = Modifier
                .zIndex(30f)
                .pointerInput(isPanelOpen) {
                    detectTapGestures(
                        onTap = {
                            onTogglePanel()
                        }
                    )
                }
                .then(
                    if (!isPanelOpen) {
                        Modifier.pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                onGroupDrag(dragAmount.x, dragAmount.y)
                            }
                        }
                    } else Modifier
                )
        ) {
            FloatingBubbleContent(
                agentStatus = agentState.status,
                isPressed = false
            )
        }
    }
}
