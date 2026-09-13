package com.example.core.accessibility.detector

import com.example.core.accessibility.model.ButtonElement
import com.example.core.accessibility.model.InputElement
import com.example.core.accessibility.model.RankedActionCandidate
import java.util.Locale

/**
 * Detector and ranker for identifying Next, Continue, Run, Check, or Submit buttons.
 * Distinguishes the primary navigation or verification action from secondary buttons like Back, Reset, or Hint.
 */
object ActionCandidateDetector {

    private val POSITIVE_KEYWORDS = listOf(
        "next", "continue", "submit", "check", "run", "verify", "test",
        "save and continue", "proceed", "execute", "validate",
        "التالي", "إرسال", "تحقق", "متابعة", "تشغيل", "فحص", "تسليم"
    )

    private val NEGATIVE_KEYWORDS = listOf(
        "cancel", "close", "back", "prev", "previous", "help", "hint",
        "reset", "clear", "delete", "remove", "sign out", "logout", "login",
        "menu", "settings", "share", "profile", "search",
        "إلغاء", "رجوع", "مساعدة", "تلميح", "إعادة ضبط", "مسح", "حذف"
    )

    fun rankActions(
        buttons: List<ButtonElement>,
        primaryInput: InputElement?
    ): List<RankedActionCandidate> {
        val ranked = mutableListOf<RankedActionCandidate>()

        for (btn in buttons) {
            val reasons = mutableListOf<String>()
            var score = 0.35f // Baseline for any clickable button

            if (!btn.isEnabled) {
                score -= 0.25f
                reasons.add("penalty:disabled")
            }

            val labelAndDesc = buildString {
                btn.label?.let { append(" ").append(it) }
                btn.contentDescription?.let { append(" ").append(it) }
                btn.viewId?.let { append(" ").append(it) }
            }.lowercase(Locale.ROOT)

            // 1. Positive semantic keyword match
            var matchedPositive: String? = null
            for (pos in POSITIVE_KEYWORDS) {
                if (labelAndDesc.contains(pos)) {
                    matchedPositive = pos
                    score += 0.45f
                    reasons.add("bonus:positive_action_keyword:$pos")
                    break
                }
            }

            // 2. Negative keyword penalty
            for (neg in NEGATIVE_KEYWORDS) {
                if (labelAndDesc.contains(neg)) {
                    score -= 0.50f
                    reasons.add("penalty:negative_action_keyword:$neg")
                    break
                }
            }

            // 3. Proximity to Input: Buttons are typically placed BELOW or next to the answer field
            var verticalDistance: Int? = null
            if (primaryInput != null && !primaryInput.bounds.isEmpty && !btn.bounds.isEmpty) {
                val inBounds = primaryInput.bounds
                verticalDistance = inBounds.verticalDistanceTo(btn.bounds)

                if (inBounds.isAbove(btn.bounds)) {
                    score += 0.15f
                    reasons.add("bonus:positioned_below_input")

                    if (verticalDistance in 0..500) {
                        score += 0.10f
                        reasons.add("bonus:close_vertical_proximity_to_input")
                    }
                }
            }

            // 4. Typical button dimensions (not a tiny dot or massive banner)
            if (btn.bounds.width in 40..450 && btn.bounds.height in 30..120) {
                score += 0.10f
                reasons.add("bonus:standard_button_dimensions")
            }

            val finalScore = score.coerceIn(0.0f, 1.0f)
            ranked.add(
                RankedActionCandidate(
                    button = btn,
                    score = finalScore,
                    reasons = reasons,
                    verticalDistanceToInput = verticalDistance
                )
            )
        }

        return ranked.sortedByDescending { it.score }
    }
}
