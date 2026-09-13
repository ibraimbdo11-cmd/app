package com.example.core.accessibility

import android.content.Context
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import com.example.core.agent.AgentEngine

/**
 * Controller abstraction for JS Agent:
 * Android Accessibility Service interaction, Screen content inspection,
 * Element detection, Text extraction, and authorized Input interaction.
 */
interface AccessibilityController {
    fun isAccessibilityPermissionGranted(context: Context): Boolean
    suspend fun inspectScreenContent(): Result<ScreenInspectionResult>
    suspend fun detectJavaScriptQuestionElements(): Result<List<DetectedElement>>
    suspend fun extractQuestionText(elementId: String): Result<String>
    suspend fun interactWithInputField(elementId: String, textToInject: String): Result<Boolean>
}

/**
 * Represents metadata of an inspected UI element on screen.
 */
data class DetectedElement(
    val id: String,
    val boundsInScreen: String,
    val className: String,
    val textSnippet: String?,
    val isEditable: Boolean
)

/**
 * Snapshot of screen inspection.
 */
data class ScreenInspectionResult(
    val timestamp: Long = System.currentTimeMillis(),
    val totalElementsScanned: Int,
    val detectedQuestionsCount: Int
)

/**
 * Real production implementation of [AccessibilityController] binding to [AccessibilityEngine].
 */
class DefaultAccessibilityController : AccessibilityController {

    override fun isAccessibilityPermissionGranted(context: Context): Boolean {
        return AccessibilityEngine.isAccessibilityServiceEnabled(context)
    }

    override suspend fun inspectScreenContent(): Result<ScreenInspectionResult> {
        val analysis = AgentEngine.state.value.currentAnalysis
        return if (analysis != null) {
            Result.success(
                ScreenInspectionResult(
                    timestamp = analysis.analyzedAtTimestamp,
                    totalElementsScanned = analysis.snapshot.totalElementsCount,
                    detectedQuestionsCount = analysis.questionCandidates.size
                )
            )
        } else {
            AccessibilityEngine.requestManualInspection(immediate = true)
            Result.success(
                ScreenInspectionResult(
                    totalElementsScanned = 0,
                    detectedQuestionsCount = 0
                )
            )
        }
    }

    override suspend fun detectJavaScriptQuestionElements(): Result<List<DetectedElement>> {
        val candidates = AgentEngine.state.value.currentAnalysis?.questionCandidates ?: emptyList()
        val elements = candidates.map { candidate ->
            DetectedElement(
                id = candidate.sourceElementId,
                boundsInScreen = candidate.bounds.toString(),
                className = "CandidateText",
                textSnippet = candidate.text,
                isEditable = false
            )
        }
        return Result.success(elements)
    }

    override suspend fun extractQuestionText(elementId: String): Result<String> {
        val candidate = AgentEngine.state.value.currentAnalysis?.questionCandidates?.find { it.sourceElementId == elementId }
            ?: AgentEngine.state.value.currentAnalysis?.primaryQuestionCandidate
        return Result.success(candidate?.text.orEmpty())
    }

    /**
     * Replaces the stub implementation:
     * 1. Resolves target [AccessibilityNodeInfo] by elementId or focused node.
     * 2. Focuses the input field via ACTION_FOCUS.
     * 3. Executes direct text insertion via multi-level injection strategy.
     */
    override suspend fun interactWithInputField(elementId: String, textToInject: String): Result<Boolean> {
        // Attempt resilient interaction through AccessibilityEngine
        val engineSuccess = AccessibilityEngine.interactWithInputField(elementId, textToInject)
        if (engineSuccess) {
            return Result.success(true)
        }

        // Direct AccessibilityNodeInfo fallback resolution:
        val rootNode = AccessibilityEngine.getRootInActiveWindow()
        var targetNode: AccessibilityNodeInfo? = null

        if (elementId.isNotBlank() && rootNode != null) {
            val matching = try { rootNode.findAccessibilityNodeInfosByViewId(elementId) } catch (_: Throwable) { null }
            targetNode = matching?.firstOrNull()
        }

        if (targetNode == null) {
            targetNode = AccessibilityEngine.findFocusedNode() ?: AccessibilityEngine.findFocusableInputNode(rootNode)
        }

        return if (targetNode != null) {
            // Request focus on target node
            try {
                targetNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            } catch (_: Throwable) {}

            // Send direct ACTION_SET_TEXT
            val args = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, textToInject)
            }
            val setSuccess = try {
                targetNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
            } catch (_: Throwable) {
                false
            }

            Result.success(setSuccess)
        } else {
            Result.failure(IllegalStateException("Target AccessibilityNodeInfo not found for input '$elementId'"))
        }
    }
}
