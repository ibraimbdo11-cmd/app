package com.example.core.accessibility.model

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Rectangular spatial coordinates of an element on screen.
 * Provides spatial geometry calculation for candidate ranking and container proximity.
 */
data class ElementBounds(
    val left: Int = 0,
    val top: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0
) {
    val width: Int get() = (right - left).coerceAtLeast(0)
    val height: Int get() = (bottom - top).coerceAtLeast(0)
    val centerX: Int get() = left + width / 2
    val centerY: Int get() = top + height / 2
    val area: Int get() = width * height

    val isEmpty: Boolean get() = width <= 0 || height <= 0

    /**
     * Checks if this element is vertically located strictly or mostly above another element.
     */
    fun isAbove(other: ElementBounds): Boolean {
        return this.bottom <= other.top || (this.centerY < other.centerY && this.top < other.top)
    }

    /**
     * Vertical distance from this element to another element.
     * Returns 0 if they overlap vertically.
     */
    fun verticalDistanceTo(other: ElementBounds): Int {
        return if (this.bottom < other.top) {
            other.top - this.bottom
        } else if (other.bottom < this.top) {
            this.top - other.bottom
        } else {
            0
        }
    }

    /**
     * Euclidean distance between element centers.
     */
    fun centerDistanceTo(other: ElementBounds): Float {
        val dx = (this.centerX - other.centerX).toFloat()
        val dy = (this.centerY - other.centerY).toFloat()
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Checks if this element overlaps or is within a reasonable proximity threshold.
     */
    fun isNear(other: ElementBounds, thresholdPx: Int = 200): Boolean {
        return centerDistanceTo(other) <= thresholdPx
    }

    override fun toString(): String = "[$left, $top, $right, $bottom] (${width}x${height})"
}
