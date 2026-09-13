package com.example.core.accessibility.detector

import com.example.core.accessibility.model.ElementBounds
import com.example.core.accessibility.model.QuestionCandidate
import com.example.core.accessibility.model.TextElement
import java.util.Locale

/**
 * Heuristic detector for identifying potential JavaScript coding problem statements or instructions.
 * Supports both English and Arabic questions, as well as mixed-language prompts.
 */
object QuestionCandidateDetector {

    // English problem statement starter phrases
    private val INSTRUCTION_PATTERNS = listOf(
        "create a function",
        "write a function",
        "implement a function",
        "define a function",
        "write a program",
        "write code",
        "complete the function",
        "return the",
        "given an array",
        "given a string",
        "given two",
        "given an object",
        "your task is",
        "the function should",
        "you are given",
        "calculate the",
        "find the",
        "check if",
        "convert the",
        "determine if",
        "sort the"
    )

    // Arabic problem statement patterns
    private val ARABIC_INSTRUCTION_PATTERNS = listOf(
        "اكتب دالة",
        "قم بإنشاء دالة",
        "أنشئ دالة",
        "دالة باسم",
        "دالة تسمى",
        "دالة تدعى",
        "اكتب كود",
        "أرجع القيمة",
        "تعيد القيمة",
        "ترجع القيمة",
        "تستقبل رقمين",
        "تستقبل رقما",
        "تستقبل رقمًا",
        "تستقبل معاملين",
        "تأخذ رقمين",
        "تأخذ معاملين",
        "تستقبل مصفوفة",
        "احسب",
        "أوجد",
        "اطبع",
        "تحقق إذا",
        "تحقق من"
    )

    // English JavaScript-specific tokens
    private val JS_KEYWORDS = listOf(
        "javascript",
        "function",
        "return",
        "array",
        "object",
        "parameter",
        "parameters",
        "argument",
        "arguments",
        "const",
        "let",
        "var",
        "promise",
        "async",
        "await",
        "callback",
        "prototype",
        "arrow function",
        "console.log",
        "string",
        "boolean",
        "number",
        "integer",
        "null",
        "undefined",
        "json"
    )

    // Arabic JavaScript-related tokens
    private val ARABIC_JS_KEYWORDS = listOf(
        "دالة",
        "معامل",
        "معاملات",
        "بارامتر",
        "بارامترات",
        "مصفوفة",
        "كائن",
        "حلقة",
        "شرط",
        "أرجع",
        "تعيد",
        "ترجع",
        "جافاسكريبت",
        "جافا سكريبت",
        "كود",
        "متغير",
        "قيمة",
        "استخدم",
        "مجموعهما",
        "مربعه",
        "ضرب"
    )

    fun detectCandidates(texts: List<TextElement>): List<QuestionCandidate> {
        val candidates = mutableListOf<QuestionCandidate>()

        for (item in texts) {
            val raw = item.text.trim()
            if (raw.length < 15) continue // Skip trivial headers or nav labels

            val lower = raw.lowercase(Locale.ROOT)
            val matchingSignals = mutableListOf<String>()
            var score = 0.0f
            var jsSignalCount = 0

            // 1. English Instruction starter phrases
            for (pattern in INSTRUCTION_PATTERNS) {
                if (lower.contains(pattern)) {
                    matchingSignals.add("instruction:$pattern")
                    score += 0.40f
                    break
                }
            }

            // 2. Arabic Instruction patterns
            for (pattern in ARABIC_INSTRUCTION_PATTERNS) {
                if (raw.contains(pattern)) {
                    matchingSignals.add("arabic_instruction:$pattern")
                    score += 0.45f
                    break
                }
            }

            // 3. English JavaScript keywords
            for (kw in JS_KEYWORDS) {
                if (lower.contains(kw)) {
                    matchingSignals.add("keyword:$kw")
                    jsSignalCount++
                    score += 0.08f
                    if (jsSignalCount >= 4) break
                }
            }

            // 4. Arabic JavaScript tokens
            for (kw in ARABIC_JS_KEYWORDS) {
                if (raw.contains(kw)) {
                    matchingSignals.add("arabic_keyword:$kw")
                    jsSignalCount++
                    score += 0.08f
                    if (jsSignalCount >= 4) break
                }
            }

            // 5. Question punctuation and structure
            if (raw.endsWith("?") || raw.endsWith("؟") || raw.endsWith(":")) {
                matchingSignals.add("punctuation:terminal")
                score += 0.15f
            }

            // 6. Optimal length range for code challenge problems (25 - 900 chars)
            if (raw.length in 25..900) {
                matchingSignals.add("length:optimal")
                score += 0.15f
            } else if (raw.length > 900) {
                matchingSignals.add("length:long")
                score += 0.08f
            }

            // 7. Code markers (backticks, braces, parentheses)
            if (raw.contains("()") || raw.contains("`") || raw.contains("=>") || raw.contains("{")) {
                matchingSignals.add("syntax:code_markers")
                score += 0.20f
            }

            val finalScore = score.coerceIn(0.0f, 1.0f)
            val isJsContext = jsSignalCount >= 1 || lower.contains("javascript") || raw.contains("جافا")

            // Candidate threshold
            if (finalScore >= 0.25f || (isJsContext && finalScore >= 0.20f)) {
                candidates.add(
                    QuestionCandidate(
                        text = raw,
                        confidenceScore = finalScore,
                        bounds = item.bounds,
                        sourceElementId = item.id,
                        matchingSignals = matchingSignals,
                        isJavaScriptSpecific = isJsContext
                    )
                )
            }
        }

        return candidates.sortedByDescending { it.confidenceScore }
    }
}
