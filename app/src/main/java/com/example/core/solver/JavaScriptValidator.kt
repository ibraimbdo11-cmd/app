package com.example.core.solver

import java.util.Stack
import java.util.regex.Pattern

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val normalizedCode: String = ""
)

/**
 * Robust lexical and structural validator for generated JavaScript solutions.
 * Validates syntax, bracket balance, required function names, parameters,
 * return requirements, and language purity.
 */
object JavaScriptValidator {

    private val ILLEGAL_FOREIGN_KEYWORDS = listOf(
        Regex("""\bdef\s+[a-zA-Z_]"""),         // Python
        Regex("""\bprint\s*\("""),               // Python
        Regex("""\bpublic\s+class\b"""),        // Java
        Regex("""\bSystem\.out\.print"""),       // Java
        Regex("""\bimport\s+java\."""),         // Java
        Regex("""\bstd::"""),                   // C++
        Regex("""\bcout\s*<<"""),               // C++
        Regex("""\bfn\s+[a-zA-Z_]"""),          // Rust
        Regex("""\bfunc\s+[a-zA-Z_]"""),        // Go / Swift
        Regex("""\$\b[a-zA-Z_][a-zA-Z0-9_]*\s*=""") // PHP variables
    )

    fun validate(
        rawCode: String,
        requirements: ParsedQuestionRequirements? = null
    ): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        val cleaned = sanitizeCode(rawCode)

        if (cleaned.isBlank()) {
            return ValidationResult(
                isValid = false,
                errors = listOf("Solution code is empty or missing."),
                normalizedCode = ""
            )
        }

        // 1. Language purity check: Reject non-JavaScript constructs
        for (illegalRegex in ILLEGAL_FOREIGN_KEYWORDS) {
            if (illegalRegex.containsMatchIn(cleaned)) {
                errors.add("Code contains foreign non-JavaScript syntax: ${illegalRegex.pattern}")
            }
        }

        // 2. Syntax & Bracket Balance Check
        val bracketError = checkBracketsAndLiterals(cleaned)
        if (bracketError != null) {
            errors.add("Syntax Error: $bracketError")
        }

        // 3. Functional Requirements Check
        if (requirements != null) {
            if (requirements.requiredFunctionName != null) {
                val hasFunction = checkFunctionName(cleaned, requirements.requiredFunctionName)
                if (!hasFunction) {
                    errors.add("Function '${requirements.requiredFunctionName}' is not declared in the solution.")
                }
            }

            if (requirements.expectedParameters.isNotEmpty()) {
                val paramCheckResult = checkParameters(cleaned, requirements.expectedParameters)
                if (paramCheckResult != null) {
                    warnings.add(paramCheckResult)
                }
            }

            if (requirements.requiresReturnStatement) {
                val hasReturn = checkReturnStatement(cleaned)
                if (!hasReturn) {
                    errors.add("Solution does not contain a return statement, but the problem requires returning a value.")
                }
            }

            for (restriction in requirements.restrictions) {
                if (restriction.contains("loop", ignoreCase = true)) {
                    if (Regex("""\b(for|while|do)\s*\(""").containsMatchIn(cleaned)) {
                        errors.add("Solution uses a loop, violating restriction: $restriction")
                    }
                }
            }
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings,
            normalizedCode = cleaned
        )
    }

    /**
     * Strips markdown code blocks (```javascript ... ```) and leading/trailing markers.
     */
    fun sanitizeCode(raw: String): String {
        var code = raw.trim()
        if (code.startsWith("```")) {
            code = code.substringAfter("\n")
        }
        if (code.endsWith("```")) {
            code = code.substringBeforeLast("```")
        }
        return code.trim()
    }

    /**
     * Verifies that curly braces, parentheses, square brackets, and string literals are balanced.
     */
    fun checkBracketsAndLiterals(code: String): String? {
        val stack = Stack<Char>()
        var inSingleQuote = false
        var inDoubleQuote = false
        var inBacktick = false
        var inLineComment = false
        var inBlockComment = false
        var isEscaped = false

        var i = 0
        while (i < code.length) {
            val c = code[i]
            val next = if (i + 1 < code.length) code[i + 1] else ' '

            // Handle comments
            if (inLineComment) {
                if (c == '\n') inLineComment = false
                i++
                continue
            }
            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    inBlockComment = false
                    i += 2
                    continue
                }
                i++
                continue
            }

            // Start comments if not in string
            if (!inSingleQuote && !inDoubleQuote && !inBacktick) {
                if (c == '/' && next == '/') {
                    inLineComment = true
                    i += 2
                    continue
                }
                if (c == '/' && next == '*') {
                    inBlockComment = true
                    i += 2
                    continue
                }
            }

            // Handle string literals
            if (isEscaped) {
                isEscaped = false
                i++
                continue
            }
            if (c == '\\') {
                if (inSingleQuote || inDoubleQuote || inBacktick) {
                    isEscaped = true
                }
                i++
                continue
            }

            if (c == '\'' && !inDoubleQuote && !inBacktick) {
                inSingleQuote = !inSingleQuote
                i++
                continue
            }
            if (c == '"' && !inSingleQuote && !inBacktick) {
                inDoubleQuote = !inDoubleQuote
                i++
                continue
            }
            if (c == '`' && !inSingleQuote && !inDoubleQuote) {
                inBacktick = !inBacktick
                i++
                continue
            }

            // If inside a string literal, skip bracket matching
            if (inSingleQuote || inDoubleQuote || inBacktick) {
                i++
                continue
            }

            // Match opening/closing brackets
            when (c) {
                '{', '(', '[' -> stack.push(c)
                '}' -> {
                    if (stack.isEmpty() || stack.pop() != '{') return "Unmatched closing brace '}'"
                }
                ')' -> {
                    if (stack.isEmpty() || stack.pop() != '(') return "Unmatched closing parenthesis ')'"
                }
                ']' -> {
                    if (stack.isEmpty() || stack.pop() != '[') return "Unmatched closing bracket ']'"
                }
            }

            i++
        }

        if (inSingleQuote || inDoubleQuote || inBacktick) {
            return "Unclosed string literal"
        }
        if (inBlockComment) {
            return "Unclosed block comment /* ... */"
        }
        if (stack.isNotEmpty()) {
            return "Unclosed bracket '${stack.peek()}'"
        }

        return null
    }

    fun checkFunctionName(code: String, expectedName: String): Boolean {
        val patterns = listOf(
            Regex("""\bfunction\s+$expectedName\s*\("""),
            Regex("""\b(?:const|let|var)\s+$expectedName\s*=\s*(?:async\s*)?(?:function|\([^)]*\)\s*=>|[a-zA-Z0-9_$]+\s*=>)"""),
            Regex("""\b$expectedName\s*:\s*function\s*\("""),
            Regex("""\b$expectedName\s*\([^)]*\)\s*\{""") // Method shorthand
        )
        return patterns.any { it.containsMatchIn(code) }
    }

    fun checkParameters(code: String, expectedParams: List<String>): String? {
        val paramPattern = Pattern.compile("""(?:function\s+[a-zA-Z0-9_$]*\s*\(([^)]*)\)|(?:const|let|var)\s+[a-zA-Z0-9_$]*\s*=\s*(?:\(([^)]*)\)|([a-zA-Z0-9_$]+))\s*=>)""")
        val matcher = paramPattern.matcher(code)
        if (matcher.find()) {
            val rawParams = (matcher.group(1) ?: matcher.group(2) ?: matcher.group(3) ?: "").trim()
            val parsedParams = rawParams.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            if (parsedParams.size != expectedParams.size) {
                return "Parameter count differs: expected ${expectedParams.size} (${expectedParams.joinToString(", ")}), found ${parsedParams.size} (${parsedParams.joinToString(", ")})"
            }
        }
        return null
    }

    fun checkReturnStatement(code: String): Boolean {
        val returnPattern = Regex("""\breturn\b""")
        val arrowImplicitReturn = Regex("""=>\s*[^{;\s]+""")
        return returnPattern.containsMatchIn(code) || arrowImplicitReturn.containsMatchIn(code)
    }
}
