package com.example.core.accessibility.analyzer

import com.example.core.accessibility.detector.ActionCandidateDetector
import com.example.core.accessibility.detector.InputCandidateDetector
import com.example.core.accessibility.detector.QuestionCandidateDetector
import com.example.core.accessibility.model.PageAnalysisResult
import com.example.core.accessibility.model.PageSnapshot

/**
 * Structural analyzer translating a raw [PageSnapshot] into organized semantic candidates.
 *
 * Prepares the extracted data cleanly for Phase 3 (Gemini JavaScript Solver)
 * without executing any automation or performing AI inference directly.
 */
object PageAnalyzer {

    fun analyze(snapshot: PageSnapshot): PageAnalysisResult {
        if (snapshot.isEmpty) {
            return PageAnalysisResult(
                snapshot = snapshot,
                statusMessage = "Empty page or inaccessible content",
                isJavaScriptContextLikely = false
            )
        }

        // 1. Detect question candidates from visible text nodes
        val questionCandidates = QuestionCandidateDetector.detectCandidates(snapshot.visibleTexts)
        val primaryQuestion = questionCandidates.firstOrNull()

        // 2. Rank candidate inputs, leveraging spatial relationship to the question
        val rankedInputs = InputCandidateDetector.rankInputs(
            inputs = snapshot.inputElements,
            primaryQuestion = primaryQuestion
        )
        val primaryInput = rankedInputs.firstOrNull()

        // 3. Rank candidate action buttons, leveraging proximity to the primary input
        val rankedActions = ActionCandidateDetector.rankActions(
            buttons = snapshot.buttonElements,
            primaryInput = primaryInput?.input
        )
        val primaryAction = rankedActions.firstOrNull()

        // 4. Determine if the context is JavaScript-related
        val hasJsCandidate = questionCandidates.any { it.isJavaScriptSpecific }
        val hasJsText = snapshot.visibleTexts.any {
            it.text.contains("javascript", ignoreCase = true) ||
                    it.text.contains("function", ignoreCase = true)
        }
        val isJsLikely = hasJsCandidate || hasJsText

        // 5. Construct user/developer readable status message
        val statusMessage = buildString {
            append("Analyzed ")
            append(snapshot.textCount).append(" texts, ")
            append(snapshot.inputCount).append(" inputs, ")
            append(snapshot.buttonCount).append(" actions.")
            if (primaryQuestion != null) {
                append(" Found candidate problem.")
            }
        }

        return PageAnalysisResult(
            snapshot = snapshot,
            questionCandidates = questionCandidates,
            primaryQuestionCandidate = primaryQuestion,
            rankedInputs = rankedInputs,
            primaryInputCandidate = primaryInput,
            rankedActions = rankedActions,
            primaryActionCandidate = primaryAction,
            isJavaScriptContextLikely = isJsLikely,
            statusMessage = statusMessage,
            analyzedAtTimestamp = System.currentTimeMillis()
        )
    }
}
