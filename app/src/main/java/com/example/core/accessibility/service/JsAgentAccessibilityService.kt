package com.example.core.accessibility.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.core.accessibility.AccessibilityEngine
import com.example.core.accessibility.analyzer.PageAnalyzer
import com.example.core.accessibility.model.ButtonElement
import com.example.core.accessibility.model.ElementBounds
import com.example.core.accessibility.model.InputElement
import com.example.core.accessibility.model.PageAnalysisResult
import com.example.core.accessibility.model.PageSnapshot
import com.example.core.accessibility.parser.AccessibilityTreeParser
import com.example.core.agent.AgentEngine
import com.example.core.agent.AgentStatus
import com.example.core.agent.AnalysisStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Production Android Accessibility Service for JS Agent.
 *
 * Responsibilities:
 * 1. Safely monitors authorized screen updates when the Agent is in [AgentStatus.isRunning] state.
 * 2. Enforces throttling and fingerprint deduplication to avoid redundant analysis.
 * 3. Extracts structured data via [AccessibilityTreeParser] and analyzes it via [PageAnalyzer].
 * 4. Multi-level input strategy: ACTION_FOCUS -> ACTION_SET_TEXT -> Clipboard + ACTION_PASTE.
 * 5. Smart clicking: ACTION_CLICK -> Clickable Parent -> dispatchGesture screen tap fallback.
 * 6. Resilient input node discovery (findFocusableInputNode) for complex layouts and WebViews.
 */
class JsAgentAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var debounceJob: Job? = null
    private var lastAnalyzedFingerprint: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        AccessibilityEngine.onServiceConnected(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Strict rule: Only inspect when Agent is active
        val currentAgentStatus = AgentEngine.state.value.status
        if (!currentAgentStatus.isRunning) {
            return
        }

        // Throttle and debounce frequent window/content changes
        val eventType = event.eventType
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            scheduleInspection()
        }
    }

    override fun onInterrupt() {
        debounceJob?.cancel()
    }

    override fun onDestroy() {
        AccessibilityEngine.onServiceDisconnected()
        serviceScope.cancel()
        super.onDestroy()
    }

    /**
     * Schedules a throttled inspection of the active window hierarchy.
     */
    fun scheduleInspection(immediate: Boolean = false) {
        debounceJob?.cancel()
        debounceJob = serviceScope.launch {
            if (!immediate) {
                delay(DEBOUNCE_DELAY_MS)
            }

            // Verify status again after debounce
            if (!AgentEngine.state.value.status.isRunning) {
                return@launch
            }

            AgentEngine.setAnalysisStatus(AnalysisStatus.SCANNING)

            val rootNode: AccessibilityNodeInfo? = try {
                rootInActiveWindow
            } catch (_: Throwable) {
                null
            }

            if (rootNode == null) {
                AgentEngine.setAnalysisStatus(
                    AnalysisStatus.UNAVAILABLE,
                    errorMessage = "Unable to inspect active window content"
                )
                return@launch
            }

            // Parse and analyze on background worker thread
            val analysisResult = withContext(Dispatchers.Default) {
                try {
                    val snapshot = AccessibilityTreeParser.parse(rootNode)

                    // Skip re-analysis if fingerprint has not changed, unless immediate
                    if (snapshot.fingerprint == lastAnalyzedFingerprint && !immediate) {
                        return@withContext null
                    }

                    lastAnalyzedFingerprint = snapshot.fingerprint
                    PageAnalyzer.analyze(snapshot)
                } catch (e: Throwable) {
                    PageAnalysisResult(
                        snapshot = PageSnapshot(),
                        statusMessage = "Analysis error: ${e.message ?: "unknown"}"
                    )
                }
            }

            if (analysisResult != null) {
                AgentEngine.onPageAnalyzed(analysisResult)
            } else {
                AgentEngine.setAnalysisStatus(AnalysisStatus.ANALYZED)
            }
        }
    }

    /**
     * Recursively traverses the view hierarchy to discover focusable/editable input nodes,
     * specially handling complex hierarchies, Jetpack Compose, and WebViews.
     */
    fun findFocusableInputNode(root: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (root == null) return null

        if (isInputCandidateNode(root)) {
            return root
        }

        val childCount = try { root.childCount } catch (_: Throwable) { 0 }
        for (i in 0 until childCount) {
            val child = try { root.getChild(i) } catch (_: Throwable) { null } ?: continue
            val found = findFocusableInputNode(child)
            if (found != null) return found
        }
        return null
    }

    private fun isInputCandidateNode(node: AccessibilityNodeInfo): Boolean {
        val className = node.className?.toString().orEmpty()
        val isEditable = try { node.isEditable } catch (_: Throwable) { false }
        val isFocusable = try { node.isFocusable } catch (_: Throwable) { false }

        val hasSetTextAction = try {
            node.actionList.any { it.id == AccessibilityNodeInfo.ACTION_SET_TEXT }
        } catch (_: Throwable) { false }

        val hasPasteAction = try {
            node.actionList.any { it.id == AccessibilityNodeInfo.ACTION_PASTE }
        } catch (_: Throwable) { false }

        if (isEditable || hasSetTextAction || hasPasteAction) return true
        if (className.contains("EditText", ignoreCase = true)) return true
        if (isFocusable && (className.contains("TextField", ignoreCase = true) || className.contains("Editor", ignoreCase = true))) return true
        
        // Support WebView input fields
        if (className.contains("WebView", ignoreCase = true) || className.contains("webkit", ignoreCase = true)) {
            if (isFocusable && (hasSetTextAction || hasPasteAction || isEditable)) return true
        }

        return false
    }

    /**
     * Executes resilient multi-level text injection:
     * 1. Resolve node via target or fallback (findFocus / findFocusableInputNode).
     * 2. Focus the field with ACTION_FOCUS.
     * 3. Level 1: Attempt ACTION_SET_TEXT.
     * 4. Level 2 (Fallback): Copy to ClipboardManager and send ACTION_PASTE.
     */
    suspend fun executeSetText(input: InputElement?, text: String): Boolean = withContext(Dispatchers.Main) {
        val rootNode = try { rootInActiveWindow } catch (_: Throwable) { null } ?: return@withContext false

        // Multi-level target input resolution
        var node: AccessibilityNodeInfo? = null
        if (input != null) {
            node = findMatchingInputNode(rootNode, input)
        }

        // Fallback 1: currently focused input
        if (node == null) {
            node = try { findFocus(AccessibilityNodeInfo.FOCUS_INPUT) } catch (_: Throwable) { null }
        }

        // Fallback 2: recursive search for any focusable/editable input in tree
        if (node == null) {
            node = findFocusableInputNode(rootNode)
        }

        if (node == null) {
            return@withContext false
        }

        return@withContext injectTextIntoNode(node, text)
    }

    /**
     * Interacts with an input field by element ID or fallback node, focusing and injecting text.
     */
    suspend fun interactWithInputField(elementId: String, textToInject: String): Boolean = withContext(Dispatchers.Main) {
        val rootNode = try { rootInActiveWindow } catch (_: Throwable) { null } ?: return@withContext false

        var node: AccessibilityNodeInfo? = null

        // 1. Try finding by viewId / elementId
        if (elementId.isNotBlank()) {
            val byId = try { rootNode.findAccessibilityNodeInfosByViewId(elementId) } catch (_: Throwable) { null }
            node = byId?.firstOrNull { isInputCandidateNode(it) }
        }

        // 2. Fallback: find focused input node
        if (node == null) {
            node = try { findFocus(AccessibilityNodeInfo.FOCUS_INPUT) } catch (_: Throwable) { null }
        }

        // 3. Fallback: find focusable input node anywhere in hierarchy
        if (node == null) {
            node = findFocusableInputNode(rootNode)
        }

        if (node == null) {
            return@withContext false
        }

        return@withContext injectTextIntoNode(node, textToInject)
    }

    private suspend fun injectTextIntoNode(node: AccessibilityNodeInfo, text: String): Boolean {
        // Step 1: Focus field
        try {
            node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        } catch (_: Throwable) {}

        delay(40)

        // Level 1: Attempt ACTION_SET_TEXT
        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }

        var success = try {
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        } catch (_: Throwable) {
            false
        }

        // Level 2 (Fallback): Clipboard + ACTION_PASTE
        if (!success) {
            delay(50)
            try {
                node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            } catch (_: Throwable) {}
            success = pasteViaClipboard(node, text)
        }

        // Verification & leniency check
        delay(80)
        val currentText = try { node.text?.toString() } catch (_: Throwable) { null }.orEmpty()
        val isVerified = success || currentText.isNotBlank() || node.isFocused

        return isVerified
    }

    private fun pasteViaClipboard(node: AccessibilityNodeInfo, text: String): Boolean {
        return try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return false
            val clip = ClipData.newPlainText("js_agent_code", text)
            clipboard.setPrimaryClip(clip)
            node.performAction(AccessibilityNodeInfo.ACTION_PASTE)
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Smart Click implementation:
     * 1. Attempts direct ACTION_CLICK on target element.
     * 2. If not clickable or fails, traverses parents for a clickable ancestor.
     * 3. Fallback: Dispatches gesture tap via screen coordinates.
     */
    suspend fun clickElement(button: ButtonElement? = null, bounds: ElementBounds? = null): Boolean = withContext(Dispatchers.Main) {
        val rootNode = try { rootInActiveWindow } catch (_: Throwable) { null } ?: return@withContext false
        val node = if (button != null) findMatchingButtonNode(rootNode, button) else null

        // Strategy 1: Direct ACTION_CLICK on node
        if (node != null && node.isEnabled && node.isClickable) {
            val clicked = try {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            } catch (_: Throwable) {
                false
            }
            if (clicked) return@withContext true
        }

        // Strategy 2: Upward parent traversal for clickable ancestor
        var parent = try { node?.parent } catch (_: Throwable) { null }
        while (parent != null) {
            if (parent.isClickable && parent.isEnabled) {
                val parentClicked = try {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                } catch (_: Throwable) {
                    false
                }
                if (parentClicked) return@withContext true
            }
            parent = try { parent.parent } catch (_: Throwable) { null }
        }

        // Strategy 3: Fallback to dispatchGesture tap using center coordinates
        val targetBounds = bounds ?: button?.bounds
        if (targetBounds != null) {
            val centerX = ((targetBounds.left + targetBounds.right) / 2).toFloat()
            val centerY = ((targetBounds.top + targetBounds.bottom) / 2).toFloat()
            if (centerX > 0 && centerY > 0) {
                return@withContext dispatchTapGesture(centerX, centerY)
            }
        } else if (node != null) {
            val rect = Rect()
            node.getBoundsInScreen(rect)
            if (!rect.isEmpty) {
                val centerX = rect.centerX().toFloat()
                val centerY = rect.centerY().toFloat()
                return@withContext dispatchTapGesture(centerX, centerY)
            }
        }

        false
    }

    /**
     * Dispatches a tap gesture at the specified screen coordinates using Android Accessibility Gesture API.
     */
    suspend fun dispatchTapGesture(x: Float, y: Float): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return false

        return suspendCancellableCoroutine { continuation ->
            val path = Path().apply {
                moveTo(x, y)
            }
            val stroke = GestureDescription.StrokeDescription(path, 0, 60)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()

            val callback = object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    if (continuation.isActive) continuation.resume(true)
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    if (continuation.isActive) continuation.resume(false)
                }
            }

            val dispatched = try {
                dispatchGesture(gesture, callback, null)
            } catch (_: Throwable) {
                false
            }

            if (!dispatched && continuation.isActive) {
                continuation.resume(false)
            }
        }
    }

    /**
     * Backward-compatible alias delegating to [clickElement].
     */
    suspend fun executeClickAction(button: ButtonElement): Boolean {
        return clickElement(button = button, bounds = button.bounds)
    }

    private fun findMatchingInputNode(root: AccessibilityNodeInfo, target: InputElement): AccessibilityNodeInfo? {
        // 1. Match by view ID if available
        if (!target.viewId.isNullOrBlank()) {
            val matching = try { root.findAccessibilityNodeInfosByViewId(target.viewId) } catch (_: Throwable) { null }
            val first = matching?.firstOrNull { isInputCandidateNode(it) }
            if (first != null) return first
        }

        val targetRect = Rect(target.bounds.left, target.bounds.top, target.bounds.right, target.bounds.bottom)
        val tempRect = Rect()

        // 2. Match by bounds proximity & editable/input candidate flag
        val boundsMatch = findNodeRecursive(root) { n ->
            try {
                n.getBoundsInScreen(tempRect)
                val diffLeft = Math.abs(tempRect.left - targetRect.left)
                val diffTop = Math.abs(tempRect.top - targetRect.top)
                val isClose = diffLeft < 70 && diffTop < 70
                val isOverlap = Rect.intersects(tempRect, targetRect)
                (isClose || isOverlap) && isInputCandidateNode(n)
            } catch (_: Throwable) {
                false
            }
        }
        if (boundsMatch != null) return boundsMatch

        // 3. Fallback: match any focusable input candidate
        return findFocusableInputNode(root)
    }

    private fun findMatchingButtonNode(root: AccessibilityNodeInfo, target: ButtonElement): AccessibilityNodeInfo? {
        // 1. Match by View ID if available
        if (!target.viewId.isNullOrBlank()) {
            val matching = try { root.findAccessibilityNodeInfosByViewId(target.viewId) } catch (_: Throwable) { null }
            val first = matching?.firstOrNull { it.isClickable }
            if (first != null) return first
        }

        // 2. Semantic text/label matching (vital for Jetpack Compose & web buttons)
        val targetLabel = target.label?.trim()
        if (!targetLabel.isNullOrEmpty()) {
            val textMatch = findNodeRecursive(root) { n ->
                val nText = n.text?.toString()?.trim()
                val nDesc = n.contentDescription?.toString()?.trim()
                val matchesLabel = (nText?.contains(targetLabel, ignoreCase = true) == true) ||
                        (nDesc?.contains(targetLabel, ignoreCase = true) == true)
                matchesLabel && (n.isClickable || n.className?.contains("Button", ignoreCase = true) == true)
            }
            if (textMatch != null) return textMatch
        }

        // 3. Match by bounds proximity
        val targetRect = Rect(target.bounds.left, target.bounds.top, target.bounds.right, target.bounds.bottom)
        val tempRect = Rect()

        return findNodeRecursive(root) { n ->
            try {
                n.getBoundsInScreen(tempRect)
                val diffLeft = Math.abs(tempRect.left - targetRect.left)
                val diffTop = Math.abs(tempRect.top - targetRect.top)
                val isClose = diffLeft < 70 && diffTop < 70
                val isOverlap = Rect.intersects(tempRect, targetRect)
                (isClose || isOverlap) && (n.isClickable || n.className?.contains("Button", ignoreCase = true) == true)
            } catch (_: Throwable) {
                false
            }
        }
    }

    private fun findNodeRecursive(node: AccessibilityNodeInfo, predicate: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo? {
        if (predicate(node)) return node
        val count = try { node.childCount } catch (_: Throwable) { 0 }
        for (i in 0 until count) {
            val child = try { node.getChild(i) } catch (_: Throwable) { null } ?: continue
            val found = findNodeRecursive(child, predicate)
            if (found != null) return found
        }
        return null
    }

    companion object {
        private const val DEBOUNCE_DELAY_MS = 350L
    }
}
