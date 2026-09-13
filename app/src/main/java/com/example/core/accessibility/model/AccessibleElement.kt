package com.example.core.accessibility.model

/**
 * Immutable snapshot of a single node in the Accessibility hierarchy.
 * Decoupled completely from Android's AccessibilityNodeInfo to prevent memory leaks
 * and runtime invalidation exceptions.
 */
data class AccessibleElement(
    val id: String,
    val className: String,
    val packageName: String,
    val text: String? = null,
    val contentDescription: String? = null,
    val viewIdResourceName: String? = null,
    val hintText: String? = null,
    val isClickable: Boolean = false,
    val isEditable: Boolean = false,
    val isEnabled: Boolean = true,
    val isFocusable: Boolean = false,
    val isScrollable: Boolean = false,
    val isPassword: Boolean = false,
    val isVisibleToUser: Boolean = true,
    val bounds: ElementBounds = ElementBounds(),
    val parentIndex: Int? = null,
    val childIndices: List<Int> = emptyList(),
    val depth: Int = 0,
    val supportedActionNames: List<String> = emptyList()
) {
    /**
     * Clean readable primary label or text content.
     */
    val primaryText: String?
        get() {
            val t = text?.trim()
            if (!t.isNullOrEmpty()) return t
            val d = contentDescription?.trim()
            if (!d.isNullOrEmpty()) return d
            val h = hintText?.trim()
            if (!h.isNullOrEmpty()) return h
            return null
        }
}

/**
 * Clean representation of a visible text block (heading, paragraph, instruction).
 */
data class TextElement(
    val id: String,
    val text: String,
    val bounds: ElementBounds,
    val isHeading: Boolean = false,
    val depth: Int = 0,
    val sourceElementIndex: Int
)

/**
 * Normalized representation of an input element (e.g. EditText, web input).
 */
data class InputElement(
    val id: String,
    val currentText: String? = null,
    val hint: String? = null,
    val contentDescription: String? = null,
    val viewId: String? = null,
    val bounds: ElementBounds,
    val isPassword: Boolean = false,
    val isEnabled: Boolean = true,
    val supportedActions: List<String> = emptyList(),
    val surroundingContextTexts: List<String> = emptyList(),
    val sourceElementIndex: Int
)

/**
 * Normalized representation of an actionable button or clickable trigger.
 */
data class ButtonElement(
    val id: String,
    val label: String? = null,
    val contentDescription: String? = null,
    val viewId: String? = null,
    val className: String,
    val isClickable: Boolean = true,
    val isEnabled: Boolean = true,
    val bounds: ElementBounds,
    val supportedActions: List<String> = emptyList(),
    val surroundingContextTexts: List<String> = emptyList(),
    val sourceElementIndex: Int
)
