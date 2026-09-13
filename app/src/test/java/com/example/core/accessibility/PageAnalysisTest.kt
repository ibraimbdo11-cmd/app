package com.example.core.accessibility

import com.example.core.accessibility.analyzer.PageAnalyzer
import com.example.core.accessibility.detector.ActionCandidateDetector
import com.example.core.accessibility.detector.InputCandidateDetector
import com.example.core.accessibility.detector.QuestionCandidateDetector
import com.example.core.accessibility.model.ButtonElement
import com.example.core.accessibility.model.ElementBounds
import com.example.core.accessibility.model.InputElement
import com.example.core.accessibility.model.PageSnapshot
import com.example.core.accessibility.model.TextElement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PageAnalysisTest {

    @Test
    fun `detects JavaScript question candidate accurately`() {
        val texts = listOf(
            TextElement(
                id = "header",
                text = "Welcome to the JS coding platform",
                bounds = ElementBounds(0, 0, 1080, 100),
                sourceElementIndex = 0
            ),
            TextElement(
                id = "q1",
                text = "Write a JavaScript function sum(a, b) that returns the sum of two numbers. What should return?",
                bounds = ElementBounds(0, 150, 1080, 400),
                sourceElementIndex = 1
            ),
            TextElement(
                id = "footer",
                text = "Copyright 2026",
                bounds = ElementBounds(0, 2000, 1080, 2100),
                sourceElementIndex = 2
            )
        )

        val candidates = QuestionCandidateDetector.detectCandidates(texts)

        assertTrue("Expected at least one candidate", candidates.isNotEmpty())
        val top = candidates.first()
        assertEquals("q1", top.sourceElementId)
        assertTrue(top.isJavaScriptSpecific)
        assertTrue(top.confidenceScore > 0.6f)
    }

    @Test
    fun `ranks input candidates with distance to question`() {
        val texts = listOf(
            TextElement(
                id = "q1",
                text = "Given an array, return the first element in JavaScript.",
                bounds = ElementBounds(50, 200, 1000, 400),
                sourceElementIndex = 0
            )
        )
        val question = QuestionCandidateDetector.detectCandidates(texts).first()

        val inputs = listOf(
            InputElement(
                id = "search_box",
                hint = "Search website...",
                bounds = ElementBounds(50, 50, 500, 120),
                sourceElementIndex = 1
            ),
            InputElement(
                id = "code_editor",
                hint = "Write your solution here...",
                bounds = ElementBounds(50, 450, 1000, 900),
                sourceElementIndex = 2
            )
        )

        val ranked = InputCandidateDetector.rankInputs(inputs, question)

        assertEquals(2, ranked.size)
        // Code editor is directly below the question and has code-solution hint
        assertEquals("code_editor", ranked.first().input.id)
        assertTrue(ranked.first().score > ranked.last().score)
    }

    @Test
    fun `ranks action buttons favoring submit and run`() {
        val buttons = listOf(
            ButtonElement(
                id = "btn_cancel",
                label = "Cancel",
                className = "android.widget.Button",
                bounds = ElementBounds(50, 1000, 250, 1100),
                sourceElementIndex = 0
            ),
            ButtonElement(
                id = "btn_submit",
                label = "Submit Solution",
                className = "android.widget.Button",
                bounds = ElementBounds(700, 1000, 1000, 1100),
                sourceElementIndex = 1
            )
        )

        val ranked = ActionCandidateDetector.rankActions(buttons, primaryInput = null)

        assertEquals(2, ranked.size)
        assertEquals("btn_submit", ranked.first().button.id)
        assertTrue(ranked.first().score > 0.6f)
    }

    @Test
    fun `PageAnalyzer orchestrates full snapshot analysis`() {
        val snapshot = PageSnapshot(
            packageName = "com.android.chrome",
            elements = emptyList(),
            visibleTexts = listOf(
                TextElement(
                    id = "p1",
                    text = "Implement a function reverseString(str) in JavaScript that reverses the characters.",
                    bounds = ElementBounds(40, 200, 1000, 450),
                    sourceElementIndex = 0
                )
            ),
            inputElements = listOf(
                InputElement(
                    id = "editor",
                    hint = "Code answer",
                    bounds = ElementBounds(40, 500, 1000, 900),
                    sourceElementIndex = 1
                )
            ),
            buttonElements = listOf(
                ButtonElement(
                    id = "run_btn",
                    label = "Run Tests",
                    className = "android.widget.Button",
                    bounds = ElementBounds(700, 950, 1000, 1050),
                    sourceElementIndex = 2
                )
            )
        )

        val result = PageAnalyzer.analyze(snapshot)

        assertTrue(result.isJavaScriptContextLikely)
        assertNotNull(result.primaryQuestionCandidate)
        assertNotNull(result.primaryInputCandidate)
        assertNotNull(result.primaryActionCandidate)
        assertEquals("editor", result.primaryInputCandidate?.input?.id)
        assertEquals("run_btn", result.primaryActionCandidate?.button?.id)
    }

    @Test
    fun `PageAnalyzer handles empty snapshot gracefully`() {
        val emptySnapshot = PageSnapshot()
        val result = PageAnalyzer.analyze(emptySnapshot)

        assertFalse(result.isJavaScriptContextLikely)
        assertEquals(0, result.questionCandidates.size)
        assertEquals(null, result.primaryQuestionCandidate)
    }

    @Test
    fun `detects Arabic JavaScript question candidate accurately`() {
        val texts = listOf(
            TextElement(
                id = "title",
                text = "منصة تحديات البرمجة",
                bounds = ElementBounds(0, 0, 1080, 100),
                sourceElementIndex = 0
            ),
            TextElement(
                id = "arabic_q",
                text = "اكتب دالة باسم add تستقبل رقمين وتعيد مجموعهما.",
                bounds = ElementBounds(0, 150, 1080, 400),
                sourceElementIndex = 1
            )
        )

        val candidates = QuestionCandidateDetector.detectCandidates(texts)
        assertTrue("Expected candidate for Arabic question", candidates.isNotEmpty())
        val top = candidates.first()
        assertEquals("arabic_q", top.sourceElementId)
        assertTrue(top.confidenceScore > 0.6f)
    }

    @Test
    fun `parses Arabic add function requirements accurately`() {
        val q = "اكتب دالة باسم add تستقبل رقمين وتعيد مجموعهما."
        val reqs = com.example.core.solver.QuestionUnderstanding.parse(q)

        assertEquals("add", reqs.requiredFunctionName)
        assertEquals(listOf("a", "b"), reqs.expectedParameters)
        assertTrue(reqs.requiresReturnStatement)

        val solver = com.example.core.solver.GeminiJavaScriptSolver()
        val solution = solver.solveLocally(reqs)

        assertNotNull(solution)
        assertTrue(solution!!.first.contains("function add(a, b)"))
        assertTrue(solution.first.contains("return a + b"))
    }

    @Test
    fun `solves Arabic square function locally with correct syntax`() {
        val q = "اكتب دالة باسم square تستقبل رقماً وتعيد مربعه."
        val reqs = com.example.core.solver.QuestionUnderstanding.parse(q)

        assertEquals("square", reqs.requiredFunctionName)
        assertTrue(reqs.requiresReturnStatement)

        val solver = com.example.core.solver.GeminiJavaScriptSolver()
        val solution = solver.solveLocally(reqs)

        assertNotNull(solution)
        assertTrue(solution!!.first.contains("function square"))
        assertTrue(solution.first.contains("*"))
    }
}

