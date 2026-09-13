package com.example.core.accessibility.model

/**
 * Immutable structural snapshot of the currently inspected window/screen.
 */
data class PageSnapshot(
    val timestamp: Long = System.currentTimeMillis(),
    val packageName: String = "",
    val windowTitle: String? = null,
    val elements: List<AccessibleElement> = emptyList(),
    val visibleTexts: List<TextElement> = emptyList(),
    val inputElements: List<InputElement> = emptyList(),
    val buttonElements: List<ButtonElement> = emptyList(),
    val fingerprint: String = ""
) {
    val totalElementsCount: Int get() = elements.size
    val textCount: Int get() = visibleTexts.size
    val inputCount: Int get() = inputElements.size
    val buttonCount: Int get() = buttonElements.size

    val isEmpty: Boolean get() = elements.isEmpty() && visibleTexts.isEmpty() && inputElements.isEmpty()
}

/**
 * Represents a detected question or problem statement candidate.
 * Explicitly designated as a Candidate (final determination occurs via AI in Phase 3).
 */
data class QuestionCandidate(
    val text: String,
    val confidenceScore: Float,
    val bounds: ElementBounds,
    val sourceElementId: String,
    val matchingSignals: List<String>,
    val isJavaScriptSpecific: Boolean
)

/**
 * Represents a ranked input candidate for injecting or entering answers.
 */
data class RankedInputCandidate(
    val input: InputElement,
    val score: Float,
    val reasons: List<String>,
    val verticalDistanceToQuestion: Int? = null
)

/**
 * Represents a ranked action button candidate for submitting or moving to the next item.
 */
data class RankedActionCandidate(
    val button: ButtonElement,
    val score: Float,
    val reasons: List<String>,
    val verticalDistanceToInput: Int? = null
)

/**
 * Comprehensive analysis of a [PageSnapshot] performed by PageAnalyzer.
 */
data class PageAnalysisResult(
    val snapshot: PageSnapshot,
    val questionCandidates: List<QuestionCandidate> = emptyList(),
    val primaryQuestionCandidate: QuestionCandidate? = null,
    val rankedInputs: List<RankedInputCandidate> = emptyList(),
    val primaryInputCandidate: RankedInputCandidate? = null,
    val rankedActions: List<RankedActionCandidate> = emptyList(),
    val primaryActionCandidate: RankedActionCandidate? = null,
    val isJavaScriptContextLikely: Boolean = false,
    val statusMessage: String = "",
    val analyzedAtTimestamp: Long = System.currentTimeMillis()
)
