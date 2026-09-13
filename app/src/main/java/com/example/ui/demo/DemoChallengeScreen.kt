package com.example.ui.demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.agent.AgentState
import com.example.core.designsystem.JsAgentColors
import com.example.core.designsystem.JsAgentDimens
import com.example.core.designsystem.JsAgentTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class DemoQuestion(
    val id: String,
    val title: String,
    val problemStatement: String,
    val expectedFunctionName: String,
    val validationHint: String,
    val validator: (String) -> Pair<Boolean, String>
)

val DEMO_QUESTIONS = listOf(
    DemoQuestion(
        id = "q_add",
        title = "دالة الجمع (Add Function)",
        problemStatement = "اكتب دالة باسم add تستقبل رقمين وتعيد مجموعهما.",
        expectedFunctionName = "add",
        validationHint = "function add(a, b) { return a + b; }",
        validator = { code ->
            val clean = code.replace(Regex("\\s+"), " ")
            val hasName = clean.contains("function add") || clean.contains("const add") || clean.contains("let add") || clean.contains("add =")
            val hasReturn = clean.contains("return") || clean.contains("=>")
            val hasPlus = clean.contains("+")
            if (hasName && hasReturn && hasPlus) {
                Pair(true, "ممتاز! الدالة add صالحة وتحسب المجموع بشكل صحيح.")
            } else {
                Pair(false, "يجب أن تحتوي الدالة على الاسم add وتقوم بإرجاع مجموع الرقمين.")
            }
        }
    ),
    DemoQuestion(
        id = "q_square",
        title = "دالة المربع (Square Function)",
        problemStatement = "اكتب دالة باسم square تستقبل رقماً وتعيد مربعه.",
        expectedFunctionName = "square",
        validationHint = "function square(x) { return x * x; }",
        validator = { code ->
            val clean = code.replace(Regex("\\s+"), " ")
            val hasName = clean.contains("function square") || clean.contains("const square") || clean.contains("square =")
            val hasReturn = clean.contains("return") || clean.contains("=>")
            val hasMultiply = clean.contains("*") || clean.contains("**") || clean.contains("Math.pow")
            if (hasName && hasReturn && hasMultiply) {
                Pair(true, "ممتاز! الدالة square صالحة وتعيد مربع الرقم بنجاح.")
            } else {
                Pair(false, "يجب أن تسمى الدالة square وتعيد حاصل ضرب الرقم في نفسه.")
            }
        }
    ),
    DemoQuestion(
        id = "q_multiply",
        title = "Multiplication (English)",
        problemStatement = "Write a function named multiply that takes two numbers and returns their product.",
        expectedFunctionName = "multiply",
        validationHint = "function multiply(a, b) { return a * b; }",
        validator = { code ->
            val clean = code.replace(Regex("\\s+"), " ")
            val hasName = clean.contains("function multiply") || clean.contains("const multiply") || clean.contains("multiply =")
            val hasReturn = clean.contains("return") || clean.contains("=>")
            val hasMult = clean.contains("*")
            if (hasName && hasReturn && hasMult) {
                Pair(true, "Awesome! The multiply function correctly calculates product.")
            } else {
                Pair(false, "Function must be named multiply and return the product of parameters.")
            }
        }
    ),
    DemoQuestion(
        id = "q_is_even",
        title = "التحقق من الأعداد الزوجية (Even Check)",
        problemStatement = "اكتب دالة باسم isEven تستقبل رقمًا وتعيد true إذا كان الرقم زوجياً.",
        expectedFunctionName = "isEven",
        validationHint = "function isEven(num) { return num % 2 === 0; }",
        validator = { code ->
            val clean = code.replace(Regex("\\s+"), " ")
            val hasName = clean.contains("function isEven") || clean.contains("isEven =")
            val hasModulo = clean.contains("%")
            if (hasName && hasModulo) {
                Pair(true, "صحيح! الدالة isEven تفحص باقي القسمة على 2 بنجاح.")
            } else {
                Pair(false, "يجب أن تفحص الدالة isEven باقي القسمة على 2 باستخدام % 2 === 0.")
            }
        }
    )
)

@Composable
fun DemoChallengeScreen(
    agentState: AgentState,
    onToggleAgent: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var userCode by remember { mutableStateOf("") }
    var validationResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var completedCount by remember { mutableIntStateOf(0) }

    val coroutineScope = rememberCoroutineScope()
    val currentQuestion = DEMO_QUESTIONS[currentIndex]

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JsAgentColors.Background)
            .statusBarsPadding()
            .padding(horizontal = JsAgentDimens.Spacing20)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(JsAgentDimens.Spacing16)
    ) {
        // Top App Bar
        Spacer(modifier = Modifier.height(JsAgentDimens.Spacing8))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(JsAgentDimens.Spacing12)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(JsAgentColors.SurfaceElevated)
                        .border(1.dp, JsAgentColors.Border, CircleShape)
                        .testTag("demo_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = JsAgentColors.Accent
                    )
                }
                Column {
                    Text(
                        text = "بيئة الاختبار التجريبية",
                        style = JsAgentTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = JsAgentColors.TextPrimary
                    )
                    Text(
                        text = "JS Agent Real Test Environment",
                        style = JsAgentTypography.labelSmall,
                        color = JsAgentColors.TextSecondary
                    )
                }
            }

            // Quick Toggle Agent Button
            Button(
                onClick = onToggleAgent,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (agentState.status.isRunning) JsAgentColors.StatusStopped else JsAgentColors.Accent,
                    contentColor = JsAgentColors.Background
                ),
                shape = RoundedCornerShape(JsAgentDimens.RadiusSmall),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("demo_toggle_agent_button")
            ) {
                Icon(
                    imageVector = if (agentState.status.isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (agentState.status.isRunning) "إيقاف الـAgent" else "تشغيل الـAgent",
                    style = JsAgentTypography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Agent Live Activity Indicator Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(JsAgentDimens.RadiusMedium),
            colors = CardDefaults.cardColors(containerColor = JsAgentColors.SurfaceElevated),
            border = androidx.compose.foundation.BorderStroke(1.dp, JsAgentColors.Border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (agentState.status.isRunning) JsAgentColors.AccentBright else JsAgentColors.StatusStopped
                            )
                    )
                    Text(
                        text = "حالة الـAgent: ${agentState.status.name}",
                        style = JsAgentTypography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = JsAgentColors.TextPrimary
                    )
                }
                Text(
                    text = agentState.latestActivity.take(35),
                    style = JsAgentTypography.labelSmall,
                    color = JsAgentColors.Accent
                )
            }
        }

        // Question Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = JsAgentColors.Accent.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(JsAgentDimens.RadiusCard)
                )
                .testTag("demo_question_card"),
            shape = RoundedCornerShape(JsAgentDimens.RadiusCard),
            colors = CardDefaults.cardColors(containerColor = JsAgentColors.Surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(JsAgentDimens.Spacing16),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "السؤال ${currentIndex + 1} من ${DEMO_QUESTIONS.size}",
                        style = JsAgentTypography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = JsAgentColors.AccentBright
                    )
                    Text(
                        text = "المحلولة بنجاح: $completedCount",
                        style = JsAgentTypography.labelSmall,
                        color = JsAgentColors.TextSecondary
                    )
                }

                Text(
                    text = currentQuestion.title,
                    style = JsAgentTypography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = JsAgentColors.TextPrimary
                )

                // The actual question text that the Accessibility Tree Parser detects
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JsAgentDimens.RadiusSmall))
                        .background(JsAgentColors.Background)
                        .border(1.dp, JsAgentColors.BorderSubtle, RoundedCornerShape(JsAgentDimens.RadiusSmall))
                        .padding(14.dp)
                ) {
                    Text(
                        text = currentQuestion.problemStatement,
                        style = JsAgentTypography.bodyLarge.copy(lineHeight = 26.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = JsAgentColors.TextPrimary,
                        modifier = Modifier.testTag("demo_question_text")
                    )
                }
            }
        }

        // Code Editor / Answer Input Field
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(JsAgentDimens.RadiusCard),
            colors = CardDefaults.cardColors(containerColor = JsAgentColors.Surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, JsAgentColors.Border)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(JsAgentDimens.Spacing16),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = JsAgentColors.Accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "محرر الكود (JavaScript Editor):",
                            style = JsAgentTypography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = JsAgentColors.TextPrimary
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            userCode = ""
                            validationResult = null
                        },
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("demo_clear_button"),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JsAgentColors.TextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JsAgentColors.Border)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "مسح", style = JsAgentTypography.labelSmall)
                    }
                }

                // Target Input Field for Agent
                OutlinedTextField(
                    value = userCode,
                    onValueChange = {
                        userCode = it
                        validationResult = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("demo_code_input"),
                    placeholder = {
                        Text(
                            text = "اكتب كود الحل هنا أو دع الـAgent يكتبه تلقائيًا...",
                            style = JsAgentTypography.bodySmall,
                            color = JsAgentColors.TextTertiary
                        )
                    },
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = JsAgentColors.AccentBright
                    ),
                    shape = RoundedCornerShape(JsAgentDimens.RadiusMedium),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JsAgentColors.Accent,
                        unfocusedBorderColor = JsAgentColors.Border,
                        focusedContainerColor = JsAgentColors.Background,
                        unfocusedContainerColor = JsAgentColors.Background
                    )
                )

                // Primary Action Button (Next / Submit) for Agent
                Button(
                    onClick = {
                        val res = currentQuestion.validator(userCode)
                        validationResult = res
                        if (res.first) {
                            completedCount++
                            coroutineScope.launch {
                                delay(1200)
                                if (currentIndex < DEMO_QUESTIONS.size - 1) {
                                    currentIndex++
                                    userCode = ""
                                    validationResult = null
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("demo_submit_button"),
                    shape = RoundedCornerShape(JsAgentDimens.RadiusMedium),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JsAgentColors.Accent,
                        contentColor = JsAgentColors.Background
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "التالي / Submit & Next",
                        style = JsAgentTypography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Live Validation Feedback Banner
        AnimatedVisibility(
            visible = validationResult != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val res = validationResult
            if (res != null) {
                val isSuccess = res.first
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(JsAgentDimens.RadiusMedium),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSuccess) JsAgentColors.Accent.copy(alpha = 0.15f) else JsAgentColors.Warning.copy(alpha = 0.15f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSuccess) JsAgentColors.Accent else JsAgentColors.Warning
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = if (isSuccess) JsAgentColors.Accent else JsAgentColors.Warning,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = res.second,
                            style = JsAgentTypography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (isSuccess) JsAgentColors.AccentBright else JsAgentColors.Warning
                        )
                    }
                }
            }
        }

        // Manual Navigation between test questions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    if (currentIndex > 0) {
                        currentIndex--
                        userCode = ""
                        validationResult = null
                    }
                },
                enabled = currentIndex > 0,
                shape = RoundedCornerShape(JsAgentDimens.RadiusSmall),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = JsAgentColors.TextPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, JsAgentColors.Border)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "السابق", style = JsAgentTypography.labelMedium)
            }

            OutlinedButton(
                onClick = {
                    if (currentIndex < DEMO_QUESTIONS.size - 1) {
                        currentIndex++
                        userCode = ""
                        validationResult = null
                    }
                },
                enabled = currentIndex < DEMO_QUESTIONS.size - 1,
                shape = RoundedCornerShape(JsAgentDimens.RadiusSmall),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = JsAgentColors.TextPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, JsAgentColors.Border)
            ) {
                Text(text = "السؤال التالي", style = JsAgentTypography.labelMedium)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(imageVector = Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(JsAgentDimens.Spacing24))
    }
}
