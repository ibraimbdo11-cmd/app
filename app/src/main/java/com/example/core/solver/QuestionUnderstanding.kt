package com.example.core.solver

import java.util.regex.Pattern

/**
 * Structured requirements extracted from a JavaScript problem statement.
 */
data class ParsedQuestionRequirements(
    val rawQuestion: String,
    val cleanedProblemStatement: String,
    val requiredFunctionName: String? = null,
    val expectedParameters: List<String> = emptyList(),
    val requiresReturnStatement: Boolean = false,
    val returnDescription: String? = null,
    val restrictions: List<String> = emptyList(),
    val isFunctionRequired: Boolean = true,
    val involvesArray: Boolean = false,
    val involvesObject: Boolean = false,
    val involvesLoop: Boolean = false,
    val expectedBehavior: String? = null,
    val confidence: Float = 0.95f
)

/**
 * Analyzes detected problem text and extracts precise JavaScript requirements.
 * Fully supports Arabic, English, and mixed-language programming questions.
 */
object QuestionUnderstanding {

    private val FUNCTION_NAME_PATTERNS = listOf(
        // Arabic patterns
        Pattern.compile("""(?:دالة\s+(?:باسم|تسمى|تدعى)?\s*([a-zA-Z_$][a-zA-Z0-9_$]*))"""),
        Pattern.compile("""(?:باسم\s+([a-zA-Z_$][a-zA-Z0-9_$]*))"""),
        Pattern.compile("""(?:تسمى\s+([a-zA-Z_$][a-zA-Z0-9_$]*))"""),
        // English patterns
        Pattern.compile("""(?:function\s+(?:named|called)\s+([a-zA-Z_$][a-zA-Z0-9_$]*))""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""(?:named\s+([a-zA-Z_$][a-zA-Z0-9_$]*))""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""(?:called\s+([a-zA-Z_$][a-zA-Z0-9_$]*))""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""(?:function\s+([a-zA-Z_$][a-zA-Z0-9_$]*)\s*\()""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""(?:name\s*[:=]\s*([a-zA-Z_$][a-zA-Z0-9_$]*))""", Pattern.CASE_INSENSITIVE)
    )

    private val PARAMETERS_PATTERNS = listOf(
        Pattern.compile("""(?:takes\s+(?:two|three|four|five|\d+)?\s*(?:parameters|arguments)?\s*([a-zA-Z0-9_$,\s]+?)\s+and\s+returns)""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""(?:parameters?\s*(?:are|named|called)?\s*[:=]?\s*([a-zA-Z0-9_$,\s]+?)(?:\.|\s+and|\s+that|\s+which|$))""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""(?:takes\s+([a-zA-Z0-9_$,\s]+?)(?:\.|\s+and|\s+which|\s+that|$))""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""(?:accepts\s+([a-zA-Z0-9_$,\s]+?)(?:\.|\s+and|\s+which|\s+that|$))""", Pattern.CASE_INSENSITIVE)
    )

    private val RETURN_PATTERNS = listOf(
        Pattern.compile("""(?:returns?\s+([^\.]+?)(?:\.|$))""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""(?:return\s+([^\.]+?)(?:\.|$))""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""(?:should\s+return\s+([^\.]+?)(?:\.|$))""", Pattern.CASE_INSENSITIVE)
    )

    fun parse(rawText: String): ParsedQuestionRequirements {
        val cleaned = rawText.replace(Regex("\\s+"), " ").trim()

        // 1. Function Name
        var functionName: String? = null
        for (pattern in FUNCTION_NAME_PATTERNS) {
            val matcher = pattern.matcher(cleaned)
            if (matcher.find()) {
                val candidate = matcher.group(1)?.trim()
                if (candidate != null && isValidJsIdentifier(candidate)) {
                    functionName = candidate
                    break
                }
            }
        }

        // 2. Parameters
        val parameters = mutableListOf<String>()

        // Arabic parameter count semantics
        when {
            cleaned.contains("رقمين") || cleaned.contains("معاملين") || cleaned.contains("two numbers") || cleaned.contains("two parameters") -> {
                parameters.add("a")
                parameters.add("b")
            }
            cleaned.contains("ثلاثة أرقام") || cleaned.contains("ثلاثة معاملات") || cleaned.contains("three numbers") -> {
                parameters.add("a")
                parameters.add("b")
                parameters.add("c")
            }
            cleaned.contains("مصفوفة") || cleaned.contains("array") -> {
                parameters.add("arr")
            }
            cleaned.contains("رقمًا") || cleaned.contains("رقما") || cleaned.contains("قيمة") || cleaned.contains("a number") -> {
                parameters.add("n")
            }
            cleaned.contains("نصًا") || cleaned.contains("نصا") || cleaned.contains("a string") -> {
                parameters.add("str")
            }
            else -> {
                for (pattern in PARAMETERS_PATTERNS) {
                    val matcher = pattern.matcher(cleaned)
                    if (matcher.find()) {
                        val rawParams = matcher.group(1)?.trim().orEmpty()
                        val parsed = parseParametersList(rawParams)
                        if (parsed.isNotEmpty()) {
                            parameters.addAll(parsed)
                            break
                        }
                    }
                }
            }
        }

        // 3. Return Statement & Expected Behavior
        var returnDesc: String? = null
        var requiresReturn = false

        if (cleaned.contains("تعيد") || cleaned.contains("ترجع") || cleaned.contains("أرجع") || cleaned.contains("يرجع")) {
            requiresReturn = true
            when {
                cleaned.contains("مجموعهما") || cleaned.contains("مجموع") -> returnDesc = "return sum of numbers"
                cleaned.contains("مربعه") || cleaned.contains("مربع") -> returnDesc = "return square of number"
                cleaned.contains("ضرب") || cleaned.contains("حاصل ضرب") -> returnDesc = "return product of numbers"
                cleaned.contains("زوجي") || cleaned.contains("زوجيا") -> returnDesc = "return true if even"
                cleaned.contains("فردي") || cleaned.contains("فرديا") -> returnDesc = "return true if odd"
                cleaned.contains("عكس") -> returnDesc = "return reversed string or array"
                else -> returnDesc = "return calculated result"
            }
        } else {
            for (pattern in RETURN_PATTERNS) {
                val matcher = pattern.matcher(cleaned)
                if (matcher.find()) {
                    requiresReturn = true
                    returnDesc = matcher.group(1)?.trim()
                    break
                }
            }
            if (!requiresReturn) {
                requiresReturn = cleaned.contains("return", ignoreCase = true)
            }
        }

        val restrictions = mutableListOf<String>()
        if (cleaned.contains("without using", ignoreCase = true) || cleaned.contains("do not use", ignoreCase = true) ||
            cleaned.contains("بدون استخدام") || cleaned.contains("لا تستخدم")
        ) {
            val match = Regex("""(?:without using|do not use|بدون استخدام|لا تستخدم)\s+([^.]+)""", RegexOption.IGNORE_CASE).find(cleaned)
            match?.groupValues?.getOrNull(1)?.let { restrictions.add(it.trim()) }
        }

        val isFunctionRequired = cleaned.contains("function", ignoreCase = true) ||
                cleaned.contains("دالة") ||
                cleaned.contains("وظيفة") ||
                functionName != null ||
                parameters.isNotEmpty()

        val involvesArray = cleaned.contains("array", ignoreCase = true) ||
                cleaned.contains("مصفوفة") ||
                cleaned.contains("عناصر")

        val involvesObject = cleaned.contains("object", ignoreCase = true) ||
                cleaned.contains("كائن")

        val involvesLoop = cleaned.contains("loop", ignoreCase = true) ||
                cleaned.contains("حلقة") ||
                cleaned.contains("تكرار")

        val behavior = returnDesc ?: if (requiresReturn) "return result" else "execute operation"

        return ParsedQuestionRequirements(
            rawQuestion = rawText,
            cleanedProblemStatement = cleaned,
            requiredFunctionName = functionName,
            expectedParameters = parameters.distinct(),
            requiresReturnStatement = requiresReturn,
            returnDescription = returnDesc,
            restrictions = restrictions,
            isFunctionRequired = isFunctionRequired,
            involvesArray = involvesArray,
            involvesObject = involvesObject,
            involvesLoop = involvesLoop,
            expectedBehavior = behavior,
            confidence = if (functionName != null) 0.95f else 0.85f
        )
    }

    private fun parseParametersList(raw: String): List<String> {
        val cleaned = raw
            .replace("parameters", "", ignoreCase = true)
            .replace("parameter", "", ignoreCase = true)
            .replace("arguments", "", ignoreCase = true)
            .replace("argument", "", ignoreCase = true)
            .replace("two", "", ignoreCase = true)
            .replace("three", "", ignoreCase = true)
            .replace("and", ",", ignoreCase = true)
            .replace("و", ",", ignoreCase = true)
            .trim()

        return cleaned.split(",", " ")
            .map { it.trim().removeSurrounding("`", "`").removeSurrounding("'", "'").removeSurrounding("\"", "\"") }
            .filter { isValidJsIdentifier(it) }
    }

    fun isValidJsIdentifier(name: String): Boolean {
        if (name.isEmpty()) return false
        val reserved = setOf(
            "function", "var", "let", "const", "return", "if", "else", "for", "while",
            "do", "switch", "case", "default", "break", "continue", "new", "this", "class",
            "extends", "super", "try", "catch", "finally", "throw", "typeof", "instanceof",
            "in", "of", "void", "delete", "yield", "await", "async", "null", "true", "false",
            "that", "which", "takes", "returns", "called", "named"
        )
        if (name.lowercase() in reserved) return false
        val first = name[0]
        if (!first.isLetter() && first != '_' && first != '$') return false
        for (i in 1 until name.length) {
            val c = name[i]
            if (!c.isLetterOrDigit() && c != '_' && c != '$') return false
        }
        return true
    }
}
