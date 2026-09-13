package com.example.core.solver

import com.example.core.agent.JavaScriptAnswer
import com.example.core.agent.JavaScriptQuestion

sealed class RepairLoopResult {
    data class Success(
        val finalAnswer: JavaScriptAnswer,
        val attemptsUsed: Int,
        val validationResult: ValidationResult
    ) : RepairLoopResult()

    data class Failure(
        val lastAttemptCode: String,
        val attemptsUsed: Int,
        val validationErrors: List<String>,
        val reason: String
    ) : RepairLoopResult()
}

/**
 * Orchestrates the Solver + Validator loop with up to 3 repair attempts.
 */
class JavaScriptRepairLoop(
    private val solver: GeminiJavaScriptSolver = GeminiJavaScriptSolver(),
    private val maxAttempts: Int = 3
) {

    suspend fun solveAndValidate(
        question: JavaScriptQuestion,
        onAttemptCallback: ((attemptNumber: Int, status: String) -> Unit)? = null
    ): RepairLoopResult {
        val requirements = QuestionUnderstanding.parse(question.rawProblemStatement)

        var currentCode = ""
        var lastValidation = ValidationResult(isValid = false)
        var lastAnswer: JavaScriptAnswer? = null

        for (attempt in 1..maxAttempts) {
            onAttemptCallback?.invoke(attempt, "Attempt $attempt of $maxAttempts: Generating solution...")

            val solveResult = if (attempt == 1) {
                solver.solveJavaScriptQuestion(question)
            } else {
                solver.solveWithRepairFeedback(
                    question = question,
                    requirements = requirements,
                    previousSolution = currentCode,
                    validationErrors = lastValidation.errors
                )
            }

            if (solveResult.isFailure) {
                val errorMsg = solveResult.exceptionOrNull()?.message ?: "Solver failure"
                if (attempt == maxAttempts) {
                    return RepairLoopResult.Failure(
                        lastAttemptCode = currentCode,
                        attemptsUsed = attempt,
                        validationErrors = listOf(errorMsg),
                        reason = "Failed to obtain a solution from AI solver after $attempt attempts: $errorMsg"
                    )
                }
                continue
            }

            val answer = solveResult.getOrThrow()
            currentCode = answer.solutionCode
            lastAnswer = answer

            onAttemptCallback?.invoke(attempt, "Attempt $attempt of $maxAttempts: Validating JavaScript syntax...")
            val validation = JavaScriptValidator.validate(currentCode, requirements)
            lastValidation = validation

            if (validation.isValid) {
                return RepairLoopResult.Success(
                    finalAnswer = answer.copy(solutionCode = validation.normalizedCode),
                    attemptsUsed = attempt,
                    validationResult = validation
                )
            }
        }

        return RepairLoopResult.Failure(
            lastAttemptCode = currentCode,
            attemptsUsed = maxAttempts,
            validationErrors = lastValidation.errors,
            reason = "Invalid JavaScript answer after $maxAttempts attempts. Errors: ${lastValidation.errors.joinToString("; ")}"
        )
    }
}
