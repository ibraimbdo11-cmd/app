export enum AgentStatus {
  STOPPED = 'STOPPED',
  READY = 'READY',
  SCANNING = 'SCANNING',
  QUESTION_DETECTED = 'QUESTION_DETECTED',
  ANALYZING = 'ANALYZING',
  SOLVING = 'SOLVING',
  VALIDATING = 'VALIDATING',
  FILLING = 'FILLING',
  VERIFYING = 'VERIFYING',
  SUBMITTING = 'SUBMITTING',
  WAITING_NEXT = 'WAITING_NEXT',
  COMPLETED = 'COMPLETED',
  ERROR = 'ERROR',
  IDLE = 'IDLE',
}

export function isAgentRunning(status: AgentStatus): boolean {
  return status !== AgentStatus.STOPPED && status !== AgentStatus.IDLE && status !== AgentStatus.ERROR;
}

export function isAgentWorking(status: AgentStatus): boolean {
  return [
    AgentStatus.QUESTION_DETECTED,
    AgentStatus.ANALYZING,
    AgentStatus.SOLVING,
    AgentStatus.VALIDATING,
    AgentStatus.FILLING,
    AgentStatus.VERIFYING,
    AgentStatus.SUBMITTING,
  ].includes(status);
}

export function getStatusDisplayName(status: AgentStatus): string {
  switch (status) {
    case AgentStatus.STOPPED:
      return 'Stopped';
    case AgentStatus.IDLE:
      return 'Idle';
    case AgentStatus.READY:
      return 'Ready';
    case AgentStatus.SCANNING:
      return 'Scanning...';
    case AgentStatus.QUESTION_DETECTED:
      return 'Question Found';
    case AgentStatus.ANALYZING:
      return 'Analyzing...';
    case AgentStatus.SOLVING:
      return 'Solving...';
    case AgentStatus.VALIDATING:
      return 'Validating...';
    case AgentStatus.FILLING:
      return 'Filling...';
    case AgentStatus.VERIFYING:
      return 'Verifying...';
    case AgentStatus.SUBMITTING:
      return 'Submitting...';
    case AgentStatus.WAITING_NEXT:
      return 'Waiting Next...';
    case AgentStatus.COMPLETED:
      return 'Completed';
    case AgentStatus.ERROR:
      return 'Error';
    default:
      return status;
  }
}

export interface AgentState {
  status: AgentStatus;
  isFloatingAssistantEnabled: boolean;
  isAccessibilityEnabled: boolean;
  isInspectorEnabled: boolean;
  currentQuestionSnippet: string | null;
  currentQuestionId: string | null;
  hasDetectedInput: boolean;
  hasDetectedAction: boolean;
  latestActivity: string;
  sessionSolvedCount: number;
  sessionFailedCount: number;
  totalRuns: number;
}

export interface QuestionTestCase {
  args: any[];
  expected: any;
  label?: string;
}

export interface JavaScriptQuestion {
  id: string;
  title: string;
  titleArabic?: string;
  prompt: string;
  initialCode: string;
  expectedOutputHint?: string;
  testCases: QuestionTestCase[];
}

export interface QuestionRequirements {
  functionName: string;
  parameters: string[];
  returnDescription: string;
  requiresArrayOperations: boolean;
  requiresLoop: boolean;
  forbidsLoop: boolean;
  requiresObject: boolean;
  detectedLanguage: 'ar' | 'en' | 'unknown';
}

export interface ValidationResult {
  isValid: boolean;
  errorMessage?: string;
  errorCategory?: string;
  feedbackHint?: string;
}

export interface SolveResult {
  success: boolean;
  code: string;
  explanation?: string;
  attemptCount: number;
  errors: string[];
  solverUsed: 'gemini' | 'local_rules';
}

export interface InspectorCandidate {
  text: string;
  confidence: number;
  signals: string[];
}

export interface TargetCandidate {
  id: string;
  bounds: string;
  label?: string;
}

export interface InspectorReport {
  packageName: string;
  totalNodes: number;
  textsCount: number;
  inputsCount: number;
  actionsCount: number;
  questionCandidate: InspectorCandidate | null;
  inputCandidate: TargetCandidate | null;
  actionCandidate: TargetCandidate | null;
}

export type ScreenMode = 'HOME' | 'SETTINGS' | 'DEMO_PLAYGROUND';
