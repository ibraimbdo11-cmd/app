package com.example.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.accessibility.AccessibilityEngine
import com.example.core.agent.AgentEngine
import com.example.core.agent.AgentState
import com.example.core.overlay.OverlayManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel governing the Main Screen state and user interactions.
 */
class MainViewModel : ViewModel() {

    val agentState: StateFlow<AgentState> = AgentEngine.state

    private val _hasOverlayPermission = MutableStateFlow(false)
    val hasOverlayPermission: StateFlow<Boolean> = _hasOverlayPermission.asStateFlow()

    private val _hasAccessibilityPermission = MutableStateFlow(false)
    val hasAccessibilityPermission: StateFlow<Boolean> = _hasAccessibilityPermission.asStateFlow()

    fun refreshPermissions(context: Context) {
        val overlayGranted = OverlayManager.canDrawOverlays(context)
        _hasOverlayPermission.value = overlayGranted
        if (!overlayGranted && agentState.value.isFloatingAssistantEnabled) {
            OverlayManager.stopOverlayService(context)
        }

        val accessibilityGranted = AccessibilityEngine.isAccessibilityServiceEnabled(context)
        _hasAccessibilityPermission.value = accessibilityGranted
        AgentEngine.setAccessibilityEnabled(accessibilityGranted)
    }

    fun toggleAgent() {
        AgentEngine.toggleAgent()
    }

    fun toggleFloatingAssistant(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                if (OverlayManager.canDrawOverlays(context)) {
                    OverlayManager.startOverlayService(context)
                } else {
                    OverlayManager.requestOverlayPermission(context)
                }
            } else {
                OverlayManager.stopOverlayService(context)
            }
        }
    }

    fun requestOverlayPermission(context: Context) {
        OverlayManager.requestOverlayPermission(context)
    }

    fun requestAccessibilityPermission(context: Context) {
        AccessibilityEngine.openAccessibilitySettings(context)
    }

    fun triggerScreenInspection() {
        AccessibilityEngine.requestManualInspection(immediate = true)
    }

    fun toggleDebugInspector(enabled: Boolean) {
        AgentEngine.setDebugInspectorEnabled(enabled)
    }
}
