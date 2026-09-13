package com.example.core.accessibility.parser

import android.graphics.Rect
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import com.example.core.accessibility.model.AccessibleElement
import com.example.core.accessibility.model.ButtonElement
import com.example.core.accessibility.model.ElementBounds
import com.example.core.accessibility.model.InputElement
import com.example.core.accessibility.model.PageSnapshot
import com.example.core.accessibility.model.TextElement
import java.security.MessageDigest

/**
 * Thread-safe, non-leaking parser for Android's [AccessibilityNodeInfo] hierarchy.
 *
 * Enforces:
 * 1. Safe extraction with strict bounds checking and crash protection against detached nodes.
 * 2. Immediate copying into immutable models without retaining native Node references.
 * 3. Text normalization and deduplication across parent-child hierarchies.
 * 4. Identification of candidate inputs, buttons, and semantic text elements.
 */
object AccessibilityTreeParser {

    private const val MAX_DEPTH = 32
    private const val MAX_TOTAL_NODES = 800

    fun parse(rootNode: AccessibilityNodeInfo?): PageSnapshot {
        if (rootNode == null) {
            return PageSnapshot()
        }

        val rawElements = ArrayList<AccessibleElement>(128)
        val tempRect = Rect()

        try {
            traverseNode(
                node = rootNode,
                parentIndex = null,
                depth = 0,
                accumulated = rawElements,
                tempRect = tempRect
            )
        } catch (e: Throwable) {
            // Guard against edge-case exceptions from detached AccessibilityNodeInfo
        }

        val packageName = rawElements.firstOrNull()?.packageName.orEmpty()
        val visibleTexts = extractDeduplicatedTexts(rawElements)
        val inputs = extractInputElements(rawElements)
        val buttons = extractButtonElements(rawElements)
        val fingerprint = computeFingerprint(packageName, visibleTexts, inputs, buttons)

        return PageSnapshot(
            timestamp = System.currentTimeMillis(),
            packageName = packageName,
            windowTitle = null,
            elements = rawElements,
            visibleTexts = visibleTexts,
            inputElements = inputs,
            buttonElements = buttons,
            fingerprint = fingerprint
        )
    }

    private fun traverseNode(
        node: AccessibilityNodeInfo,
        parentIndex: Int?,
        depth: Int,
        accumulated: ArrayList<AccessibleElement>,
        tempRect: Rect
    ) {
        if (depth > MAX_DEPTH || accumulated.size >= MAX_TOTAL_NODES) {
            return
        }

        val currentIndex = accumulated.size
        val safeBounds = try {
            node.getBoundsInScreen(tempRect)
            ElementBounds(tempRect.left, tempRect.top, tempRect.right, tempRect.bottom)
        } catch (_: Throwable) {
            ElementBounds()
        }

        val safeText = try { node.text?.toString() } catch (_: Throwable) { null }
        val safeContentDescription = try { node.contentDescription?.toString() } catch (_: Throwable) { null }
        val safeViewId = try { node.viewIdResourceName } catch (_: Throwable) { null }
        val safeHintText = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                node.hintText?.toString()
            } else null
        } catch (_: Throwable) { null }

        val safeClassName = try { node.className?.toString().orEmpty() } catch (_: Throwable) { "" }
        val safePackageName = try { node.packageName?.toString().orEmpty() } catch (_: Throwable) { "" }

        val safeIsClickable = try { node.isClickable } catch (_: Throwable) { false }
        val safeIsEditable = try { node.isEditable } catch (_: Throwable) { false }
        val safeIsEnabled = try { node.isEnabled } catch (_: Throwable) { true }
        val safeIsFocusable = try { node.isFocusable } catch (_: Throwable) { false }
        val safeIsScrollable = try { node.isScrollable } catch (_: Throwable) { false }
        val safeIsPassword = try { node.isPassword } catch (_: Throwable) { false }
        val safeIsVisible = try { node.isVisibleToUser } catch (_: Throwable) { true }

        val safeActions = try {
            node.actionList.mapNotNull { action ->
                when (action.id) {
                    AccessibilityNodeInfo.ACTION_CLICK -> "ACTION_CLICK"
                    AccessibilityNodeInfo.ACTION_SET_TEXT -> "ACTION_SET_TEXT"
                    AccessibilityNodeInfo.ACTION_FOCUS -> "ACTION_FOCUS"
                    AccessibilityNodeInfo.ACTION_SCROLL_FORWARD -> "ACTION_SCROLL_FORWARD"
                    AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD -> "ACTION_SCROLL_BACKWARD"
                    else -> action.label?.toString()
                }
            }
        } catch (_: Throwable) {
            emptyList()
        }

        val childIndices = mutableListOf<Int>()

        val element = AccessibleElement(
            id = "node_$currentIndex",
            className = safeClassName,
            packageName = safePackageName,
            text = safeText,
            contentDescription = safeContentDescription,
            viewIdResourceName = safeViewId,
            hintText = safeHintText,
            isClickable = safeIsClickable,
            isEditable = safeIsEditable,
            isEnabled = safeIsEnabled,
            isFocusable = safeIsFocusable,
            isScrollable = safeIsScrollable,
            isPassword = safeIsPassword,
            isVisibleToUser = safeIsVisible,
            bounds = safeBounds,
            parentIndex = parentIndex,
            childIndices = childIndices,
            depth = depth,
            supportedActionNames = safeActions
        )
        accumulated.add(element)

        val childCount = try { node.childCount } catch (_: Throwable) { 0 }
        for (i in 0 until childCount) {
            if (accumulated.size >= MAX_TOTAL_NODES) break
            val childNode = try { node.getChild(i) } catch (_: Throwable) { null }
            if (childNode != null) {
                childIndices.add(accumulated.size)
                traverseNode(
                    node = childNode,
                    parentIndex = currentIndex,
                    depth = depth + 1,
                    accumulated = accumulated,
                    tempRect = tempRect
                )
                // Note: Do not call childNode.recycle() on modern Android API 30+
            }
        }
    }

    /**
     * Extracts meaningful visible texts, deduplicating parent container concatenations
     * and repeated sibling fragments.
     */
    fun extractDeduplicatedTexts(elements: List<AccessibleElement>): List<TextElement> {
        val result = mutableListOf<TextElement>()
        val seenTexts = HashSet<String>()

        for ((index, element) in elements.withIndex()) {
            if (!element.isVisibleToUser && !element.bounds.isEmpty) continue

            val primary = normalizeText(element.primaryText) ?: continue
            if (primary.length < 2) continue

            // If this element has children and its text is exactly equal to one of its children,
            // skip the parent copy in favor of the leaf node.
            val hasChildWithSameText = element.childIndices.any { childIdx ->
                if (childIdx < elements.size) {
                    normalizeText(elements[childIdx].primaryText) == primary
                } else false
            }
            if (hasChildWithSameText) continue

            // Deduplicate exact matches already seen with identical text
            if (seenTexts.add(primary)) {
                val isHeading = element.className.contains("Heading", ignoreCase = true) ||
                        primary.length < 60 && element.depth <= 4 && element.bounds.height > 24

                result.add(
                    TextElement(
                        id = element.id,
                        text = primary,
                        bounds = element.bounds,
                        isHeading = isHeading,
                        depth = element.depth,
                        sourceElementIndex = index
                    )
                )
            }
        }

        return result
    }

    /**
     * Identifies candidate input fields without assuming any website-specific layout.
     */
    fun extractInputElements(elements: List<AccessibleElement>): List<InputElement> {
        val inputs = mutableListOf<InputElement>()

        for ((index, element) in elements.withIndex()) {
            val isEditable = element.isEditable ||
                    element.className.contains("EditText", ignoreCase = true) ||
                    element.supportedActionNames.contains("ACTION_SET_TEXT")

            if (isEditable) {
                // Collect surrounding context texts from parent and direct siblings
                val surrounding = mutableListOf<String>()
                element.parentIndex?.let { pIdx ->
                    if (pIdx < elements.size) {
                        elements[pIdx].primaryText?.let { surrounding.add(it) }
                        elements[pIdx].childIndices.forEach { sibIdx ->
                            if (sibIdx != index && sibIdx < elements.size) {
                                elements[sibIdx].primaryText?.let { surrounding.add(it) }
                            }
                        }
                    }
                }

                inputs.add(
                    InputElement(
                        id = element.id,
                        currentText = element.text,
                        hint = element.hintText,
                        contentDescription = element.contentDescription,
                        viewId = element.viewIdResourceName,
                        bounds = element.bounds,
                        isPassword = element.isPassword,
                        isEnabled = element.isEnabled,
                        supportedActions = element.supportedActionNames,
                        surroundingContextTexts = surrounding.distinct().take(4),
                        sourceElementIndex = index
                    )
                )
            }
        }

        return inputs
    }

    /**
     * Identifies buttons and clickable action triggers.
     */
    fun extractButtonElements(elements: List<AccessibleElement>): List<ButtonElement> {
        val buttons = mutableListOf<ButtonElement>()

        for ((index, element) in elements.withIndex()) {
            val isButtonClass = element.className.contains("Button", ignoreCase = true)
            val isClickable = element.isClickable || element.supportedActionNames.contains("ACTION_CLICK") || isButtonClass

            // Filter out root containers or massive screen-covering views that have clickable flags
            val isLikelyContainer = element.childIndices.size > 8 ||
                    (element.bounds.width > 900 && element.bounds.height > 1200)

            if (isClickable && !isLikelyContainer && element.isVisibleToUser) {
                val label = element.primaryText

                // Collect surrounding context texts
                val surrounding = mutableListOf<String>()
                element.parentIndex?.let { pIdx ->
                    if (pIdx < elements.size) {
                        elements[pIdx].primaryText?.let { surrounding.add(it) }
                    }
                }

                buttons.add(
                    ButtonElement(
                        id = element.id,
                        label = label,
                        contentDescription = element.contentDescription,
                        viewId = element.viewIdResourceName,
                        className = element.className,
                        isClickable = element.isClickable,
                        isEnabled = element.isEnabled,
                        bounds = element.bounds,
                        supportedActions = element.supportedActionNames,
                        surroundingContextTexts = surrounding,
                        sourceElementIndex = index
                    )
                )
            }
        }

        return buttons
    }

    /**
     * Normalizes text whitespace and trims empty characters.
     */
    fun normalizeText(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val cleaned = raw.replace(Regex("\\s+"), " ").trim()
        return if (cleaned.isNotEmpty()) cleaned else null
    }

    /**
     * Generates a fast MD5 hash of primary textual elements and bounds for snapshot deduplication.
     */
    private fun computeFingerprint(
        pkg: String,
        texts: List<TextElement>,
        inputs: List<InputElement>,
        buttons: List<ButtonElement>
    ): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val sb = StringBuilder(pkg)
            sb.append("|").append(texts.size).append("|").append(inputs.size).append("|").append(buttons.size)
            texts.take(8).forEach { sb.append(";").append(it.text.take(30)) }
            inputs.forEach { sb.append(";").append(it.id).append(it.bounds.toString()) }
            buttons.take(6).forEach { sb.append(";").append(it.label.orEmpty()) }

            val digest = md.digest(sb.toString().toByteArray())
            digest.joinToString("") { "%02x".format(it) }
        } catch (_: Throwable) {
            "${pkg}_${texts.size}_${inputs.size}_${buttons.size}"
        }
    }
}
