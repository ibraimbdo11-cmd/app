package com.example.core.agent

import com.example.core.accessibility.model.PageAnalysisResult

/**
 * Represents the current operational state of the JS Agent State Machine.
 */
enum class AgentStatus {
    IDLE,
    READY,
    RUNNING,
    SCANNING,
    QUESTION_DETECTED,
    ANALYZING,
    SOLVING,
    VALIDATING,
    FILLING,
    VERIFYING,
    SUBMITTING,
    WAITING_NEXT,
    COMPLETED,
    ERROR,
    STOPPED;

    val isWorking: Boolean
        get() = this in listOf(
            RUNNING,
            SCANNING,
            QUESTION_DETECTED,
            ANALYZING,
            SOLVING,
            VALIDATING,
            FILLING,
            VERIFYING,
            SUBMITTING
        )

    val isRunning: Boolean
        get() = this != STOPPED && this != IDLE && this != READY && this != ERROR

    fun toDisplayString(): String = when (this) {
        READY -> "Ready"
        RUNNING -> "Running"
        SCANNING -> "Scanning"
        QUESTION_DETECTED -> "Question Detected"
        ANALYZING -> "Analyzing"
        SOLVING -> "Solving"
        VALIDATING -> "Validating"
        FILLING -> "Filling"
        VERIFYING -> "Verifying"
        SUBMITTING -> "Submitting"
        WAITING_NEXT -> "Waiting"
        COMPLETED -> "Completed"
        ERROR -> "Error"
        STOPPED -> "Stopped"
        IDLE -> "Idle"
    }
}

/**
 * Status of the Accessibility inspection engine.
 */
enum class AnalysisStatus {
    IDLE,
    SCANNING,
    ANALYZED,
    UNAVAILABLE,
    ERROR
}

/**
 * Immutable state representing the agent engine status, statistics, and active operations.
 */
data class AgentState(
    val status: AgentStatus = AgentStatus.STOPPED,
    val activeTask: String? = null,
    val isFloatingAssistantEnabled: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val analysisStatus: AnalysisStatus = AnalysisStatus.IDLE,
    val currentAnalysis: PageAnalysisResult? = null,
    val isDebugInspectorEnabled: Boolean = false,
    val lastErrorMessage: String? = null,
    val lastStateChangeTimestamp: Long = System.currentTimeMillis(),
    val sessionSolvedCount: Int = 0,
    val sessionFailedCount: Int = 0,
    val currentQuestionSnippet: String? = null,
    val latestActivity: String = "Ready",
    val hasDetectedInput: Boolean = false,
    val hasDetectedAction: Boolean = false
)

/**
 * Domain model representing a JavaScript question detected or extracted from
 * an authorized test web page in upcoming phases.
 */
data class JavaScriptQuestion(
    val id: String,
    val title: String,
    val rawProblemStatement: String,
    val sourceContextUrl: String? = null,
    val codeSnippet: String? = null,
    val options: List<String> = emptyList(),
    val detectedAtTimestamp: Long = System.currentTimeMillis()
)

/**
 * Domain model representing the structured answer for a JavaScript question.
 */
data class JavaScriptAnswer(
    val questionId: String,
    val solutionCode: String,
    val explanation: String,
    val selectedOptionIndex: Int? = null,
    val confidenceScore: Float = 1.0f
)

/**
 * Dedicated contract for the specialized JavaScript AI solver.
 * Will be powered by Gemini / JavaScript fundamental solver in Phase 3.
 */
interface JavaScriptSolver {
    suspend fun solveJavaScriptQuestion(question: JavaScriptQuestion): Result<JavaScriptAnswer>
}
