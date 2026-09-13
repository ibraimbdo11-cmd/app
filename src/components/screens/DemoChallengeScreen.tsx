import React, { useState, useEffect } from 'react';
import {
  AgentState,
  AgentStatus,
  JavaScriptQuestion,
  getStatusDisplayName,
  isAgentRunning,
} from '../../types';
import { JsAgentColors } from '../../core/designsystem/Theme';
import { JsAgentLogo } from '../designsystem/JsAgentLogo';
import {
  ArrowLeft,
  Play,
  Square,
  Sparkles,
  CheckCircle2,
  XCircle,
  RotateCcw,
  Send,
  ChevronLeft,
  ChevronRight,
  Code2,
  Terminal,
  Activity,
  Check,
} from 'lucide-react';

interface DemoChallengeScreenProps {
  questions: JavaScriptQuestion[];
  currentQuestionIndex: number;
  agentState: AgentState;
  onNavigateBack: () => void;
  onSelectQuestion: (index: number) => void;
  onStartAgent: () => void;
  onStopAgent: () => void;
  onTriggerAgentSolve: () => void;
  userCode: string;
  onUserCodeChange: (code: string) => void;
  onSubmitAnswer: (code: string) => { passed: boolean; details: string };
  feedback: { passed: boolean; message: string } | null;
}

export const DemoChallengeScreen: React.FC<DemoChallengeScreenProps> = ({
  questions,
  currentQuestionIndex,
  agentState,
  onNavigateBack,
  onSelectQuestion,
  onStartAgent,
  onStopAgent,
  onTriggerAgentSolve,
  userCode,
  onUserCodeChange,
  onSubmitAnswer,
  feedback,
}) => {
  const currentQuestion = questions[currentQuestionIndex] || questions[0];
  const isRunning = isAgentRunning(agentState.status);

  const handleClearCode = () => {
    onUserCodeChange('');
  };

  const handleNext = () => {
    if (currentQuestionIndex < questions.length - 1) {
      onSelectQuestion(currentQuestionIndex + 1);
    }
  };

  const handlePrev = () => {
    if (currentQuestionIndex > 0) {
      onSelectQuestion(currentQuestionIndex - 1);
    }
  };

  return (
    <div className="w-full max-w-3xl mx-auto px-4 py-5 space-y-4 pb-24 text-left">
      {/* Header */}
      <header className="flex items-center justify-between pb-2 border-b border-[#1D2821]">
        <div className="flex items-center space-x-3">
          <button
            type="button"
            onClick={onNavigateBack}
            className="p-2 rounded-xl bg-[#171E19] hover:bg-[#202A23] border border-[#1D2821] text-[#90A396] hover:text-[#00E676] transition-colors cursor-pointer"
            title="Back to Home"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <div className="flex items-center space-x-2">
              <h1 className="text-base font-bold text-[#F1F5F2]">
                بيئة الاختبار التجريبية
              </h1>
              <span className="text-xs text-[#90A396]">
                / JS Automation Sandbox
              </span>
            </div>
            <p className="text-[11px] text-[#90A396]">
              Real interactive questions solved natively by JS Agent
            </p>
          </div>
        </div>

        {/* Quick Agent Toggle */}
        <button
          type="button"
          onClick={isRunning ? onStopAgent : onStartAgent}
          className={`px-3 py-1.5 rounded-xl font-bold text-xs flex items-center space-x-1.5 transition-all duration-200 cursor-pointer ${
            isRunning
              ? 'bg-[#171E19] border border-[#FF5252]/40 text-[#FF5252]'
              : 'bg-[#00E676] hover:bg-[#33FF8A] text-[#06140A] shadow-md shadow-[#00E676]/20'
          }`}
        >
          {isRunning ? (
            <>
              <Square className="w-3.5 h-3.5 fill-current" />
              <span>إيقاف الـAgent</span>
            </>
          ) : (
            <>
              <Play className="w-3.5 h-3.5 fill-current" />
              <span>تشغيل الـAgent</span>
            </>
          )}
        </button>
      </header>

      {/* Live Agent Activity Bar */}
      <div
        className="px-3.5 py-2.5 rounded-xl border flex items-center justify-between text-xs transition-all duration-300"
        style={{
          backgroundColor: JsAgentColors.SurfaceElevated,
          borderColor: isRunning ? JsAgentColors.BorderGlow : JsAgentColors.Border,
        }}
      >
        <div className="flex items-center space-x-2.5">
          <Activity
            className={`w-4 h-4 ${
              isRunning ? 'text-[#00E676] animate-pulse' : 'text-[#5A6F62]'
            }`}
          />
          <span className="font-semibold text-[#F1F5F2]">
            {getStatusDisplayName(agentState.status)}:
          </span>
          <span className="text-[#33FF8A] font-mono truncate max-w-xs md:max-w-md">
            {agentState.latestActivity}
          </span>
        </div>

        {isRunning && (
          <span className="px-2 py-0.5 text-[10px] font-bold rounded bg-[#00E676]/10 text-[#00E676] border border-[#00E676]/30 animate-pulse">
            LIVE AGENT
          </span>
        )}
      </div>

      {/* Question Card */}
      <div className="p-4 rounded-2xl bg-[#101512] border border-[#1D2821] space-y-3">
        <div className="flex items-center justify-between text-xs">
          <div className="flex items-center space-x-2">
            <span className="px-2 py-0.5 rounded-md bg-[#171E19] font-mono font-bold text-[#00E676] border border-[#1D2821]">
              Question {currentQuestionIndex + 1} of {questions.length}
            </span>
            <span className="text-[#90A396]">
              {currentQuestion.titleArabic || currentQuestion.title}
            </span>
          </div>

          <div className="flex items-center space-x-3 text-xs">
            <span className="text-[#00E676] font-semibold">
              Solved: {agentState.sessionSolvedCount}
            </span>
          </div>
        </div>

        {/* Prompt Container */}
        <div
          id="question-container"
          className="p-3.5 rounded-xl bg-[#080B09] border border-[#151E18] space-y-2"
        >
          <div className="flex items-center justify-between">
            <span className="text-[10px] uppercase font-bold text-[#5A6F62] tracking-wider">
              Problem Statement
            </span>
            {currentQuestion.expectedOutputHint && (
              <span className="text-[10px] font-mono text-[#90A396] bg-[#171E19] px-2 py-0.5 rounded border border-[#1D2821]">
                Target: {currentQuestion.expectedOutputHint}
              </span>
            )}
          </div>
          <p
            id="question-text"
            className="text-sm text-[#F1F5F2] font-medium leading-relaxed"
            dir={/[\u0600-\u06FF]/.test(currentQuestion.prompt) ? 'rtl' : 'ltr'}
          >
            {currentQuestion.prompt}
          </p>
        </div>

        {/* Test Cases preview pills */}
        <div className="flex flex-wrap gap-1.5 pt-1">
          {currentQuestion.testCases.map((tc, idx) => (
            <span
              key={idx}
              className="px-2 py-0.5 text-[10px] font-mono rounded bg-[#171E19] border border-[#1D2821] text-[#90A396]"
            >
              {tc.label || `Case ${idx + 1}`} → {JSON.stringify(tc.expected)}
            </span>
          ))}
        </div>
      </div>

      {/* JavaScript Editor Card */}
      <div className="p-4 rounded-2xl bg-[#101512] border border-[#1D2821] space-y-2.5">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <Code2 className="w-4 h-4 text-[#00E676]" />
            <span className="text-xs font-bold text-[#F1F5F2]">
              محرر الكود (JavaScript Editor):
            </span>
          </div>

          <div className="flex items-center space-x-2">
            <button
              type="button"
              onClick={onTriggerAgentSolve}
              className="px-2.5 py-1 text-xs font-semibold rounded-lg bg-[#00E676]/10 hover:bg-[#00E676]/20 text-[#00E676] border border-[#00E676]/30 flex items-center space-x-1 cursor-pointer"
              title="Solve automatically using JS Agent"
            >
              <Sparkles className="w-3.5 h-3.5" />
              <span>حل بالـAgent</span>
            </button>
            <button
              type="button"
              onClick={handleClearCode}
              className="px-2.5 py-1 text-xs font-semibold rounded-lg bg-[#171E19] hover:bg-[#202A23] text-[#90A396] hover:text-[#F1F5F2] border border-[#1D2821] flex items-center space-x-1 cursor-pointer"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              <span>مسح / Clear</span>
            </button>
          </div>
        </div>

        {/* Code Input Field */}
        <div className="relative rounded-xl border border-[#1D2821] bg-[#080B09] overflow-hidden focus-within:border-[#00E676]/60 transition-colors">
          <div className="flex items-center px-3 py-1.5 bg-[#101512] border-b border-[#151E18] text-[10px] font-mono text-[#5A6F62] justify-between">
            <div className="flex items-center space-x-1.5">
              <Terminal className="w-3 h-3 text-[#00E676]" />
              <span>solution.js</span>
            </div>
            <span>JavaScript (ES2022)</span>
          </div>

          <textarea
            id="code-input-editor"
            value={userCode}
            onChange={e => onUserCodeChange(e.target.value)}
            placeholder="// اكتب كود الـ JavaScript هنا أو دع الـ Agent يقوم بحله..."
            rows={8}
            spellCheck={false}
            className="w-full p-3 font-mono text-sm bg-transparent text-[#F1F5F2] placeholder-[#5A6F62] resize-y focus:outline-none leading-relaxed"
          />
        </div>

        {/* Submit & Navigation Bar */}
        <div className="flex items-center justify-between pt-1">
          <div className="flex items-center space-x-2">
            <button
              type="button"
              disabled={currentQuestionIndex === 0}
              onClick={handlePrev}
              className="px-3 py-1.5 text-xs font-semibold rounded-xl bg-[#171E19] hover:bg-[#202A23] disabled:opacity-40 text-[#90A396] hover:text-[#F1F5F2] border border-[#1D2821] flex items-center space-x-1 cursor-pointer disabled:cursor-not-allowed"
            >
              <ChevronLeft className="w-4 h-4" />
              <span>السابق</span>
            </button>
            <button
              type="button"
              disabled={currentQuestionIndex === questions.length - 1}
              onClick={handleNext}
              className="px-3 py-1.5 text-xs font-semibold rounded-xl bg-[#171E19] hover:bg-[#202A23] disabled:opacity-40 text-[#90A396] hover:text-[#F1F5F2] border border-[#1D2821] flex items-center space-x-1 cursor-pointer disabled:cursor-not-allowed"
            >
              <span>التالي</span>
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>

          <button
            type="button"
            id="btn-submit-answer"
            onClick={() => onSubmitAnswer(userCode)}
            className="px-5 py-2 rounded-xl font-bold text-xs bg-[#00E676] hover:bg-[#33FF8A] text-[#06140A] flex items-center space-x-1.5 shadow-md shadow-[#00E676]/20 transition-colors cursor-pointer"
          >
            <Send className="w-3.5 h-3.5" />
            <span>التالي / Submit &amp; Next</span>
          </button>
        </div>
      </div>

      {/* Validation Feedback Banner */}
      {feedback && (
        <div
          className={`p-3.5 rounded-xl border flex items-start space-x-3 text-xs transition-all duration-200 animate-in fade-in slide-in-from-top-2 ${
            feedback.passed
              ? 'bg-[#00E676]/10 border-[#00E676]/40 text-[#00E676]'
              : 'bg-[#FF5252]/10 border-[#FF5252]/40 text-[#FF5252]'
          }`}
        >
          {feedback.passed ? (
            <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5" />
          ) : (
            <XCircle className="w-4 h-4 shrink-0 mt-0.5" />
          )}
          <div className="space-y-0.5">
            <span className="font-bold">
              {feedback.passed
                ? '✓ اكتمل بنجاح (All test cases passed)'
                : '✕ فشل في التحقق أو حالات الاختبار (Test failed)'}
            </span>
            <p className="text-[11px] opacity-90 leading-relaxed font-mono">
              {feedback.message}
            </p>
          </div>
        </div>
      )}
    </div>
  );
};
