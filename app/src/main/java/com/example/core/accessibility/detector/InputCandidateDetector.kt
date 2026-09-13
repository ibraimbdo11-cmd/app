package com.example.core.accessibility.detector

import com.example.core.accessibility.model.ElementBounds
import com.example.core.accessibility.model.InputElement
import com.example.core.accessibility.model.QuestionCandidate
import com.example.core.accessibility.model.RankedInputCandidate
import java.util.Locale

/**
 * Detector and ranker for identifying the primary input field suitable for code/answer entry.
 * Distinguishes true answer/code editors from distractors like search bars, notes, or filter inputs.
 */
object InputCandidateDetector {

    private val NEGATIVE_KEYWORDS = listOf(
        "search", "find", "query", "url", "address", "filter", "search_bar",
        "username", "email", "password", "notes", "comment", "بحث", "اسم المستخدم", "ملاحظات", "تعليق"
    )

    private val POSITIVE_HINTS = listOf(
        "code", "solution", "answer", "function", "javascript", "script", "type your code", "editor",
        "كود", "الحل", "الإجابة", "اكتب الحل", "دالة", "جافاسكريبت", "محرر"
    )

    fun rankInputs(
        inputs: List<InputElement>,
        primaryQuestion: QuestionCandidate?
    ): List<RankedInputCandidate> {
        val ranked = mutableListOf<RankedInputCandidate>()

        for (input in inputs) {
            val reasons = mutableListOf<String>()
            var score = 0.50f // Base baseline for any editable element

            // 1. Password check: immediate disqualification/heavy penalty
            if (input.isPassword) {
                reasons.add("penalty:is_password")
                score -= 0.60f
            }

            // 2. Disabled check
            if (!input.isEnabled) {
                reasons.add("penalty:disabled")
                score -= 0.30f
            }

            // 3. Negative keyword check in hint, viewId, contentDescription
            val combinedDescriptors = buildString {
                input.hint?.let { append(" ").append(it) }
                input.viewId?.let { append(" ").append(it) }
                input.contentDescription?.let { append(" ").append(it) }
            }.lowercase(Locale.ROOT)

            for (neg in NEGATIVE_KEYWORDS) {
                if (combinedDescriptors.contains(neg)) {
                    reasons.add("penalty:search_or_auth_descriptor:$neg")
                    score -= 0.40f
                    break
                }
            }

            // 4. Positive keyword check in hint or description
            for (pos in POSITIVE_HINTS) {
                if (combinedDescriptors.contains(pos)) {
                    reasons.add("bonus:positive_code_hint:$pos")
                    score += 0.25f
                    break
                }
            }

            // 5. Sizing check: Coding inputs typically have good width and height
            if (input.bounds.width >= 160) {
                score += 0.15f
                reasons.add("bonus:wide_input")
            }
            if (input.bounds.height >= 48) {
                score += 0.15f
                reasons.add("bonus:multiline_or_tall_input")
            }

            // 6. Supported action: ACTION_SET_TEXT
            if (input.supportedActions.contains("ACTION_SET_TEXT")) {
                score += 0.10f
                reasons.add("bonus:supports_set_text")
            }

            // 7. Proximity and topological relationship with Primary Question Candidate
            var verticalDistance: Int? = null
            if (primaryQuestion != null && !primaryQuestion.bounds.isEmpty && !input.bounds.isEmpty) {
                val qBounds = primaryQuestion.bounds
                verticalDistance = qBounds.verticalDistanceTo(input.bounds)

                // Ideally the input is placed BELOW the question
                if (qBounds.isAbove(input.bounds)) {
                    score += 0.20f
                    reasons.add("bonus:positioned_below_question")

                    // Proximity bonus if within reasonable distance (e.g. within 600px vertically)
                    if (verticalDistance in 0..600) {
                        score += 0.15f
                        reasons.add("bonus:close_vertical_proximity_to_question")
                    }
                } else {
                    // Input placed above question is unusual for answer fields
                    score -= 0.15f
                    reasons.add("penalty:positioned_above_question")
                }
            }

            // 8. Surrounding context hints (e.g., words like "code", "solution", "answer", "الحل")
            val surroundingText = input.surroundingContextTexts.joinToString(" ").lowercase(Locale.ROOT)
            for (pos in POSITIVE_HINTS) {
                if (surroundingText.contains(pos)) {
                    score += 0.15f
                    reasons.add("bonus:surrounding_code_context:$pos")
                    break
                }
            }

            val finalScore = score.coerceIn(0.0f, 1.0f)
            ranked.add(
                RankedInputCandidate(
                    input = input,
                    score = finalScore,
                    reasons = reasons,
                    verticalDistanceToQuestion = verticalDistance
                )
            )
        }

        return ranked.sortedByDescending { it.score }
    }
}
