import React, { useState, useEffect, useRef, useCallback } from 'react';
import {
  AgentState,
  AgentStatus,
  InspectorReport,
  ScreenMode,
  JavaScriptQuestion,
} from './types';
import { DEMO_CHALLENGES } from './data/challenges';
import { QuestionUnderstanding } from './core/solver/QuestionUnderstanding';
import { JavaScriptRepairLoop } from './core/solver/JavaScriptRepairLoop';
import { JavaScriptValidator } from './core/solver/JavaScriptValidator';
import { MainScreen } from './components/screens/MainScreen';
import { SettingsScreen } from './components/screens/SettingsScreen';
import { DemoChallengeScreen } from './components/screens/DemoChallengeScreen';
import { FloatingAgentGroup } from './components/overlay/FloatingAgentGroup';

export const App: React.FC = () => {
  // Navigation
  const [screenMode, setScreenMode] = useState<ScreenMode>('HOME');

  // Questions and code
  const [questions] = useState<JavaScriptQuestion[]>(DEMO_CHALLENGES);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const currentQuestion = questions[currentQuestionIndex] || questions[0];

  const [userCode, setUserCode] = useState<string>(currentQuestion.initialCode);
  const [feedback, setFeedback] = useState<{ passed: boolean; message: string } | null>(null);

  // Agent State
  const [agentState, setAgentState] = useState<AgentState>({
    status: AgentStatus.READY,
    isFloatingAssistantEnabled: true,
    isAccessibilityEnabled: true,
    isInspectorEnabled: false,
    currentQuestionSnippet: currentQuestion.prompt.slice(0, 75) + '...',
    currentQuestionId: currentQuestion.id,
    hasDetectedInput: true,
    hasDetectedAction: true,
    latestActivity: 'Agent ready. Standby in authorized environment.',
    sessionSolvedCount: 0,
    sessionFailedCount: 0,
    totalRuns: 0,
  });

  // Floating Panel Open State
  const [isPanelOpen, setIsPanelOpen] = useState<boolean>(false);

  // Inspector Report
  const [inspectorReport, setInspectorReport] = useState<InspectorReport>({
    packageName: 'com.example.jsagent.web',
    totalNodes: 38,
    textsCount: 16,
    inputsCount: 1,
    actionsCount: 3,
    questionCandidate: {
      text: currentQuestion.prompt,
      confidence: 96,
      signals: ['prompt_keyword', 'arabic_js_signature', 'test_context'],
    },
    inputCandidate: {
      id: 'code-input-editor',
      bounds: '[x:24, y:380, w:640, h:210]',
      label: 'JavaScript Editor Textarea',
    },
    actionCandidate: {
      id: 'btn-submit-answer',
      bounds: '[x:510, y:605, w:150, h:40]',
      label: 'Submit & Next Button',
    },
  });

  // Update current question code and inspector when index changes
  useEffect(() => {
    setUserCode(currentQuestion.initialCode);
    setFeedback(null);
    setAgentState(prev => ({
      ...prev,
      currentQuestionSnippet: currentQuestion.prompt.slice(0, 80) + '...',
      currentQuestionId: currentQuestion.id,
      hasDetectedInput: true,
      hasDetectedAction: true,
    }));
    setInspectorReport(prev => ({
      ...prev,
      questionCandidate: {
        text: currentQuestion.prompt,
        confidence: 98,
        signals: ['prompt_keyword', 'function_signature', 'test_context'],
      },
    }));
  }, [currentQuestionIndex, currentQuestion]);

  // Test Runner logic
  const evaluateSolution = useCallback(
    (codeToTest: string, q: JavaScriptQuestion): { passed: boolean; details: string } => {
      try {
        const reqs = QuestionUnderstanding.analyze(q.prompt);
        const validation = JavaScriptValidator.validate(codeToTest, reqs);
        if (!validation.isValid) {
          return {
            passed: false,
            details: `Validation failed: ${validation.errorMessage || 'Structural check error'}`,
          };
        }

        const funcName = reqs.functionName || 'solution';
        // Safe Function constructor sandbox
        const runner = new Function(
          `${codeToTest}\nreturn (typeof ${funcName} === 'function' ? ${funcName} : null);`
        );
        const fn = runner();

        if (!fn) {
          return {
            passed: false,
            details: `Function '${funcName}' was not found or is not callable.`,
          };
        }

        for (let i = 0; i < q.testCases.length; i++) {
          const tc = q.testCases[i];
          const result = fn(...tc.args);
          if (JSON.stringify(result) !== JSON.stringify(tc.expected)) {
            return {
              passed: false,
              details: `Failed case ${i + 1} (${tc.label || tc.args.join(', ')}): expected ${JSON.stringify(
                tc.expected
              )} but got ${JSON.stringify(result)}`,
            };
          }
        }

        return {
          passed: true,
          details: `All ${q.testCases.length} test cases passed successfully!`,
        };
      } catch (err: any) {
        return {
          passed: false,
          details: `Runtime error: ${err.message || 'Execution failed'}`,
        };
      }
    },
    []
  );

  const handleSubmitAnswer = (code: string) => {
    const result = evaluateSolution(code, currentQuestion);
    setFeedback({
      passed: result.passed,
      message: result.details,
    });

    if (result.passed) {
      setAgentState(prev => ({
        ...prev,
        sessionSolvedCount: prev.sessionSolvedCount + 1,
        latestActivity: `Solved: ${currentQuestion.title}! All tests passed.`,
      }));

      // Auto advance to next question if available
      if (currentQuestionIndex < questions.length - 1) {
        setTimeout(() => {
          setCurrentQuestionIndex(prev => prev + 1);
        }, 1200);
      }
    } else {
      setAgentState(prev => ({
        ...prev,
        sessionFailedCount: prev.sessionFailedCount + 1,
        latestActivity: `Test failed on: ${currentQuestion.title}.`,
      }));
    }

    return result;
  };

  // Automated Agent Solver Loop
  const agentLoopTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  const triggerAgentSolve = useCallback(async () => {
    const q = questions[currentQuestionIndex];
    if (!q) return;

    setAgentState(prev => ({
      ...prev,
      status: AgentStatus.SCANNING,
      latestActivity: `Scanning question: ${q.title}...`,
    }));

    await new Promise(r => setTimeout(r, 450));

    setAgentState(prev => ({
      ...prev,
      status: AgentStatus.ANALYZING,
      latestActivity: 'Analyzing requirements with QuestionUnderstanding...',
    }));

    const reqs = QuestionUnderstanding.analyze(q.prompt);
    await new Promise(r => setTimeout(r, 400));

    setAgentState(prev => ({
      ...prev,
      status: AgentStatus.SOLVING,
      latestActivity: `Solving function '${reqs.functionName}' via Repair Loop...`,
    }));

    const solveResult = await JavaScriptRepairLoop.executeWithRepair(reqs, q.prompt);
    await new Promise(r => setTimeout(r, 450));

    setAgentState(prev => ({
      ...prev,
      status: AgentStatus.FILLING,
      latestActivity: `Injecting solution code into JavaScript Editor... (${solveResult.solverUsed})`,
    }));

    setUserCode(solveResult.code);
    await new Promise(r => setTimeout(r, 500));

    setAgentState(prev => ({
      ...prev,
      status: AgentStatus.VERIFYING,
      latestActivity: 'Verifying tests and language purity...',
    }));

    const result = evaluateSolution(solveResult.code, q);
    await new Promise(r => setTimeout(r, 450));

    setFeedback({
      passed: result.passed,
      message: `${result.details} [Engine: ${solveResult.solverUsed}]`,
    });

    if (result.passed) {
      setAgentState(prev => ({
        ...prev,
        status: AgentStatus.SUBMITTING,
        sessionSolvedCount: prev.sessionSolvedCount + 1,
        latestActivity: `✓ Passed ${q.title}! Submitting to next challenge...`,
      }));

      await new Promise(r => setTimeout(r, 900));

      if (currentQuestionIndex < questions.length - 1) {
        setCurrentQuestionIndex(idx => idx + 1);
        setAgentState(prev => ({
          ...prev,
          status: AgentStatus.READY,
          latestActivity: 'Advancing to next question...',
        }));
      } else {
        setAgentState(prev => ({
          ...prev,
          status: AgentStatus.COMPLETED,
          latestActivity: 'All demo challenges successfully completed!',
        }));
      }
    } else {
      setAgentState(prev => ({
        ...prev,
        status: AgentStatus.ERROR,
        sessionFailedCount: prev.sessionFailedCount + 1,
        latestActivity: `Verification failed: ${result.details}`,
      }));
    }
  }, [currentQuestionIndex, questions, evaluateSolution]);

  // Autonomous cycle when Agent is RUNNING
  const isAgentActive =
    agentState.status !== AgentStatus.STOPPED &&
    agentState.status !== AgentStatus.IDLE &&
    agentState.status !== AgentStatus.ERROR &&
    agentState.status !== AgentStatus.COMPLETED;

  useEffect(() => {
    if (!isAgentActive) {
      if (agentLoopTimeoutRef.current) {
        clearTimeout(agentLoopTimeoutRef.current);
      }
      return;
    }

    if (screenMode === 'DEMO_PLAYGROUND' && agentState.status === AgentStatus.READY) {
      agentLoopTimeoutRef.current = setTimeout(() => {
        triggerAgentSolve();
      }, 900);
    }

    return () => {
      if (agentLoopTimeoutRef.current) {
        clearTimeout(agentLoopTimeoutRef.current);
      }
    };
  }, [isAgentActive, screenMode, agentState.status, triggerAgentSolve]);

  const handleStartAgent = () => {
    setAgentState(prev => ({
      ...prev,
      status: AgentStatus.READY,
      latestActivity: 'Agent started. Ready to analyze and solve JavaScript questions.',
    }));
  };

  const handleStopAgent = () => {
    if (agentLoopTimeoutRef.current) {
      clearTimeout(agentLoopTimeoutRef.current);
    }
    setAgentState(prev => ({
      ...prev,
      status: AgentStatus.STOPPED,
      latestActivity: 'Agent stopped by user.',
    }));
  };

  const handleTriggerInspection = () => {
    setInspectorReport({
      packageName: 'com.example.jsagent.web',
      totalNodes: 42,
      textsCount: 18,
      inputsCount: 1,
      actionsCount: 3,
      questionCandidate: {
        text: currentQuestion.prompt,
        confidence: 99,
        signals: ['js_question_pattern', 'arabic_math_terms', 'code_editor_target'],
      },
      inputCandidate: {
        id: 'code-input-editor',
        bounds: '[x:24, y:380, w:640, h:210]',
        label: 'JavaScript Editor Textarea',
      },
      actionCandidate: {
        id: 'btn-submit-answer',
        bounds: '[x:510, y:605, w:150, h:40]',
        label: 'Submit & Next Button',
      },
    });
    setAgentState(prev => ({
      ...prev,
      latestActivity: 'Page structure analyzed: Question, Code Editor & Action detected.',
    }));
  };

  return (
    <div className="min-h-screen bg-[#080B09] text-[#F1F5F2] relative">
      {/* Primary Screen Views */}
      <main className="container mx-auto">
        {screenMode === 'HOME' && (
          <MainScreen
            agentState={agentState}
            inspectorReport={inspectorReport}
            onStartAgent={handleStartAgent}
            onStopAgent={handleStopAgent}
            onToggleFloating={enabled =>
              setAgentState(prev => ({ ...prev, isFloatingAssistantEnabled: enabled }))
            }
            onToggleAccessibility={enabled =>
              setAgentState(prev => ({ ...prev, isAccessibilityEnabled: enabled }))
            }
            onToggleInspector={enabled =>
              setAgentState(prev => ({ ...prev, isInspectorEnabled: enabled }))
            }
            onTriggerInspection={handleTriggerInspection}
            onNavigateToDemo={() => setScreenMode('DEMO_PLAYGROUND')}
            onNavigateToSettings={() => setScreenMode('SETTINGS')}
          />
        )}

        {screenMode === 'SETTINGS' && (
          <SettingsScreen
            agentState={agentState}
            onNavigateBack={() => setScreenMode('HOME')}
            onToggleFloating={enabled =>
              setAgentState(prev => ({ ...prev, isFloatingAssistantEnabled: enabled }))
            }
            onToggleAccessibility={enabled =>
              setAgentState(prev => ({ ...prev, isAccessibilityEnabled: enabled }))
            }
            onToggleInspector={enabled =>
              setAgentState(prev => ({ ...prev, isInspectorEnabled: enabled }))
            }
          />
        )}

        {screenMode === 'DEMO_PLAYGROUND' && (
          <DemoChallengeScreen
            questions={questions}
            currentQuestionIndex={currentQuestionIndex}
            agentState={agentState}
            onNavigateBack={() => setScreenMode('HOME')}
            onSelectQuestion={idx => setCurrentQuestionIndex(idx)}
            onStartAgent={handleStartAgent}
            onStopAgent={handleStopAgent}
            onTriggerAgentSolve={triggerAgentSolve}
            userCode={userCode}
            onUserCodeChange={setUserCode}
            onSubmitAnswer={handleSubmitAnswer}
            feedback={feedback}
          />
        )}
      </main>

      {/* Persistent Floating Assistant Overlay (Bubble + Panel) */}
      <FloatingAgentGroup
        agentState={agentState}
        isPanelOpen={isPanelOpen}
        onTogglePanel={() => setIsPanelOpen(open => !open)}
        onToggleAgent={() => {
          if (
            agentState.status === AgentStatus.STOPPED ||
            agentState.status === AgentStatus.IDLE
          ) {
            handleStartAgent();
          } else {
            handleStopAgent();
          }
        }}
        onToggleFloating={enabled =>
          setAgentState(prev => ({ ...prev, isFloatingAssistantEnabled: enabled }))
        }
      />
    </div>
  );
};
