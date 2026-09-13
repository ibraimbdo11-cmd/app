package com.example.core.accessibility

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import com.example.core.accessibility.model.ButtonElement
import com.example.core.accessibility.model.ElementBounds
import com.example.core.accessibility.model.InputElement
import com.example.core.accessibility.service.JsAgentAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

/**
 * High-level manager coordinating the [JsAgentAccessibilityService] connection,
 * accessibility permission checks, resilient node discovery, multi-level input,
 * and smart clicking.
 */
object AccessibilityEngine {

    private var serviceReference: WeakReference<JsAgentAccessibilityService>? = null

    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

    fun onServiceConnected(service: JsAgentAccessibilityService) {
        serviceReference = WeakReference(service)
        _isServiceConnected.value = true
    }

    fun onServiceDisconnected() {
        serviceReference = null
        _isServiceConnected.value = false
    }

    /**
     * Checks if our accessibility service is enabled in Android System Settings.
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            ?: return false

        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val expectedComponent = "${context.packageName}/${JsAgentAccessibilityService::class.java.canonicalName}"
        val shortComponent = "${context.packageName}/.core.accessibility.service.JsAgentAccessibilityService"

        return enabledServices.contains(context.packageName) &&
                (enabledServices.contains(expectedComponent) ||
                 enabledServices.contains(shortComponent) ||
                 enabledServices.contains(JsAgentAccessibilityService::class.java.simpleName))
    }

    /**
     * Launches the system Accessibility settings screen so the user can enable JS Agent.
     */
    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * Triggers an explicit on-demand inspection if the service is actively connected.
     */
    fun requestManualInspection(immediate: Boolean = true): Boolean {
        val service = serviceReference?.get() ?: return false
        service.scheduleInspection(immediate = immediate)
        return true
    }

    /**
     * Retrieves the root node of the active window hierarchy.
     */
    fun getRootInActiveWindow(): AccessibilityNodeInfo? {
        val service = serviceReference?.get() ?: return null
        return try {
            service.rootInActiveWindow
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Finds currently focused input node via findFocus(FOCUS_INPUT).
     */
    fun findFocusedNode(): AccessibilityNodeInfo? {
        val service = serviceReference?.get() ?: return null
        return try {
            service.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Recursively traverses the tree to discover focusable/editable input nodes.
     */
    fun findFocusableInputNode(root: AccessibilityNodeInfo? = null): AccessibilityNodeInfo? {
        val service = serviceReference?.get() ?: return null
        val targetRoot = root ?: try { service.rootInActiveWindow } catch (_: Throwable) { null } ?: return null
        return service.findFocusableInputNode(targetRoot)
    }

    /**
     * Multi-level input strategy: FOCUS -> ACTION_SET_TEXT -> Clipboard PASTE fallback.
     */
    suspend fun executeSetText(input: InputElement?, text: String): Boolean {
        val service = serviceReference?.get() ?: return false
        return service.executeSetText(input, text)
    }

    /**
     * Directly interacts with an input field by element ID, focusing and injecting text.
     */
    suspend fun interactWithInputField(elementId: String, textToInject: String): Boolean {
        val service = serviceReference?.get() ?: return false
        return service.interactWithInputField(elementId, textToInject)
    }

    /**
     * Smart click strategy: ACTION_CLICK -> Clickable parent -> dispatchGesture tap fallback.
     */
    suspend fun clickElement(button: ButtonElement? = null, bounds: ElementBounds? = null): Boolean {
        val service = serviceReference?.get() ?: return false
        return service.clickElement(button, bounds)
    }

    /**
     * Backward-compatible click execution for [ButtonElement].
     */
    suspend fun executeClickAction(button: ButtonElement): Boolean {
        val service = serviceReference?.get() ?: return false
        return service.executeClickAction(button)
    }

    /**
     * Dispatches tap gesture at specified screen coordinates.
     */
    suspend fun dispatchTapGesture(x: Float, y: Float): Boolean {
        val service = serviceReference?.get() ?: return false
        return service.dispatchTapGesture(x, y)
    }
}
