import React from 'react';
import {
  AgentState,
  AgentStatus,
  InspectorReport,
  getStatusDisplayName,
  isAgentRunning,
} from '../../types';
import { JsAgentColors } from '../../core/designsystem/Theme';
import { JsAgentLogo } from '../designsystem/JsAgentLogo';
import {
  Settings,
  Play,
  Square,
  Layers,
  Sparkles,
  Search,
  Code2,
  CheckCircle2,
  AlertCircle,
  ExternalLink,
  ChevronRight,
  ShieldCheck,
  Cpu,
} from 'lucide-react';

interface MainScreenProps {
  agentState: AgentState;
  inspectorReport: InspectorReport;
  onStartAgent: () => void;
  onStopAgent: () => void;
  onToggleFloating: (enabled: boolean) => void;
  onToggleAccessibility: (enabled: boolean) => void;
  onToggleInspector: (enabled: boolean) => void;
  onTriggerInspection: () => void;
  onNavigateToDemo: () => void;
  onNavigateToSettings: () => void;
}

export const MainScreen: React.FC<MainScreenProps> = ({
  agentState,
  inspectorReport,
  onStartAgent,
  onStopAgent,
  onToggleFloating,
  onToggleAccessibility,
  onToggleInspector,
  onTriggerInspection,
  onNavigateToDemo,
  onNavigateToSettings,
}) => {
  const isRunning = isAgentRunning(agentState.status);

  const statusDotColor =
    agentState.status === AgentStatus.STOPPED || agentState.status === AgentStatus.IDLE
      ? JsAgentColors.StatusStopped
      : agentState.status === AgentStatus.ERROR
      ? JsAgentColors.Error
      : agentState.status === AgentStatus.READY
      ? 'rgba(0, 230, 118, 0.7)'
      : JsAgentColors.Accent;

  return (
    <div className="w-full max-w-2xl mx-auto px-4 py-6 space-y-5 pb-24 text-left">
      {/* ==========================================
          HEADER
          ========================================== */}
      <header className="flex items-center justify-between pb-2 border-b border-[#1D2821]">
        <div className="flex items-center space-x-3.5">
          <JsAgentLogo size={42} isActive={isRunning} showGlow={true} />
          <div>
            <h1 className="text-xl font-bold tracking-tight text-[#F1F5F2]">
              JS Agent
            </h1>
            <p className="text-xs text-[#90A396]">
              Smart JavaScript Automation Agent
            </p>
          </div>
        </div>

        <button
          type="button"
          onClick={onNavigateToSettings}
          className="p-2.5 rounded-xl bg-[#171E19] hover:bg-[#202A23] border border-[#1D2821] text-[#90A396] hover:text-[#00E676] transition-colors cursor-pointer"
          title="Open Settings"
        >
          <Settings className="w-5 h-5" />
        </button>
      </header>

      {/* ==========================================
          1. PRIMARY AGENT STATUS CARD
          ========================================== */}
      <section
        className="p-4 rounded-2xl border transition-all duration-300"
        style={{
          backgroundColor: JsAgentColors.Surface,
          borderColor: isRunning ? JsAgentColors.BorderGlow : JsAgentColors.Border,
          boxShadow: isRunning ? '0 0 20px rgba(0, 230, 118, 0.12)' : 'none',
        }}
      >
        <div className="flex items-center justify-between mb-3.5">
          <div className="flex items-center space-x-2">
            <Cpu className="w-4 h-4 text-[#00E676]" />
            <h2 className="text-xs font-bold uppercase tracking-wider text-[#90A396]">
              Agent Status
            </h2>
          </div>

          <div className="inline-flex items-center space-x-2 px-2.5 py-1 rounded-full bg-[#171E19] border border-[#1D2821]">
            <span
              className="w-2 h-2 rounded-full"
              style={{ backgroundColor: statusDotColor }}
            />
            <span
              className="text-xs font-semibold"
              style={{ color: statusDotColor }}
            >
              {getStatusDisplayName(agentState.status)}
            </span>
          </div>
        </div>

        {/* Current Task Box */}
        <div className="p-3 mb-4 rounded-xl bg-[#080B09] border border-[#151E18]">
          <span className="block text-[10px] font-semibold text-[#5A6F62] uppercase tracking-wider mb-1">
            Current Task
          </span>
          <p className="text-sm font-mono text-[#F1F5F2] line-clamp-2">
            {isRunning
              ? agentState.latestActivity || 'Analyzing authorized environment...'
              : 'No active task (Agent is stopped)'}
          </p>
        </div>

        {/* Start / Stop Button */}
        <button
          type="button"
          onClick={isRunning ? onStopAgent : onStartAgent}
          className={`w-full py-3 px-4 rounded-xl font-bold text-sm flex items-center justify-center space-x-2 transition-all duration-200 cursor-pointer ${
            isRunning
              ? 'bg-[#171E19] hover:bg-[#202A23] border border-[#FF5252]/40 text-[#FF5252]'
              : 'bg-[#00E676] hover:bg-[#33FF8A] text-[#06140A] shadow-lg shadow-[#00E676]/20'
          }`}
        >
          {isRunning ? (
            <>
              <Square className="w-4 h-4 fill-current" />
              <span>Stop Agent</span>
            </>
          ) : (
            <>
              <Play className="w-4 h-4 fill-current" />
              <span>Start Agent</span>
            </>
          )}
        </button>
      </section>

      {/* ==========================================
          2. INTERACTIVE TEST SANDBOX CARD
          ========================================== */}
      <section
        onClick={onNavigateToDemo}
        className="p-4 rounded-2xl bg-gradient-to-r from-[#101512] to-[#171E19] border border-[#1D2821] hover:border-[#00E676]/50 transition-all duration-200 cursor-pointer group"
      >
        <div className="flex items-start justify-between">
          <div className="flex items-start space-x-3.5">
            <div className="p-2.5 rounded-xl bg-[#00E676]/10 text-[#00E676] border border-[#00E676]/20 group-hover:scale-105 transition-transform">
              <Code2 className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <h3 className="text-sm font-bold text-[#F1F5F2]">
                  بيئة الاختبار التجريبية
                </h3>
                <span className="text-xs text-[#90A396]">/ Demo Sandbox</span>
              </div>
              <p className="text-xs text-[#90A396] mt-1 leading-relaxed">
                اختبر قدرات الـ Agent الذاتي في حل أسئلة JavaScript التفاعلية، والكتابة التلقائية، واجتياز حالات الاختبار.
              </p>
            </div>
          </div>
          <ChevronRight className="w-5 h-5 text-[#5A6F62] group-hover:text-[#00E676] group-hover:translate-x-1 transition-all shrink-0 mt-2" />
        </div>

        <div className="mt-3 pt-3 border-t border-[#1D2821] flex items-center justify-between text-xs">
          <span className="text-[#00E676] font-medium flex items-center space-x-1.5">
            <Sparkles className="w-3.5 h-3.5" />
            <span>4 أسئلة جاهزة للاختبار الآلي</span>
          </span>
          <span className="text-[#90A396] font-semibold group-hover:text-[#F1F5F2]">
            فتح البيئة ←
          </span>
        </div>
      </section>

      {/* ==========================================
          3. FLOATING ASSISTANT CARD
          ========================================== */}
      <section className="p-4 rounded-2xl bg-[#101512] border border-[#1D2821] space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="p-2 rounded-lg bg-[#171E19] text-[#00E676] border border-[#1D2821]">
              <Layers className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-sm font-semibold text-[#F1F5F2]">
                Floating Assistant
              </h3>
              <p className="text-xs text-[#90A396]">
                Display floating bubble & quick control panel on screen
              </p>
            </div>
          </div>

          <label className="relative inline-flex items-center cursor-pointer">
            <input
              type="checkbox"
              checked={agentState.isFloatingAssistantEnabled}
              onChange={e => onToggleFloating(e.target.checked)}
              className="sr-only peer"
            />
            <div className="w-11 h-6 bg-[#171E19] peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-[#90A396] peer-checked:after:bg-[#06140A] after:border-[#1D2821] after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[#00E676]"></div>
          </label>
        </div>

        <div className="flex items-center justify-between pt-2 border-t border-[#1D2821]/60 text-xs">
          <span className="text-[#5A6F62]">Screen Overlay Status:</span>
          <span className="text-[#00E676] font-medium flex items-center space-x-1">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>Overlay Ready</span>
          </span>
        </div>
      </section>

      {/* ==========================================
          4. ACCESSIBILITY SERVICE CARD
          ========================================== */}
      <section className="p-4 rounded-2xl bg-[#101512] border border-[#1D2821] space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="p-2 rounded-lg bg-[#171E19] text-[#00E676] border border-[#1D2821]">
              <ShieldCheck className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-sm font-semibold text-[#F1F5F2]">
                Accessibility Engine
              </h3>
              <p className="text-xs text-[#90A396]">
                Inspects JavaScript problem structures and input fields
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-2">
            <button
              type="button"
              onClick={onTriggerInspection}
              className="px-2.5 py-1 text-xs font-semibold rounded-lg bg-[#171E19] hover:bg-[#202A23] text-[#00E676] border border-[#1D2821] cursor-pointer"
            >
              Analyze Screen
            </button>
            <label className="relative inline-flex items-center cursor-pointer">
              <input
                type="checkbox"
                checked={agentState.isAccessibilityEnabled}
                onChange={e => onToggleAccessibility(e.target.checked)}
                className="sr-only peer"
              />
              <div className="w-11 h-6 bg-[#171E19] peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-[#90A396] peer-checked:after:bg-[#06140A] after:border-[#1D2821] after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[#00E676]"></div>
            </label>
          </div>
        </div>

        <div className="flex items-center justify-between pt-2 border-t border-[#1D2821]/60 text-xs">
          <span className="text-[#5A6F62]">Service State:</span>
          <span
            className={`font-medium ${
              agentState.isAccessibilityEnabled ? 'text-[#00E676]' : 'text-[#90A396]'
            }`}
          >
            {agentState.isAccessibilityEnabled
              ? 'Accessibility Active & Ready'
              : 'Accessibility Disabled'}
          </span>
        </div>
      </section>

      {/* ==========================================
          5. STRUCTURAL INSPECTOR CARD
          ========================================== */}
      <section className="p-4 rounded-2xl bg-[#101512] border border-[#1D2821] space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2.5">
            <Search className="w-4 h-4 text-[#00E676]" />
            <div>
              <h3 className="text-sm font-semibold text-[#F1F5F2]">
                Page Structural Inspector
              </h3>
              <p className="text-xs text-[#90A396]">
                Visualize detected elements, candidates, and ranking scores
              </p>
            </div>
          </div>

          <label className="relative inline-flex items-center cursor-pointer">
            <input
              type="checkbox"
              checked={agentState.isInspectorEnabled}
              onChange={e => onToggleInspector(e.target.checked)}
              className="sr-only peer"
            />
            <div className="w-11 h-6 bg-[#171E19] peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-[#90A396] peer-checked:after:bg-[#06140A] after:border-[#1D2821] after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[#00E676]"></div>
          </label>
        </div>

        {agentState.isInspectorEnabled && (
          <div className="pt-2 border-t border-[#1D2821] space-y-2.5">
            {/* Quick Metrics */}
            <div className="grid grid-cols-4 gap-2 text-center text-xs">
              <div className="p-2 rounded-lg bg-[#080B09] border border-[#151E18]">
                <div className="text-[#90A396] text-[10px]">Nodes</div>
                <div className="font-bold text-[#F1F5F2] font-mono">
                  {inspectorReport.totalNodes}
                </div>
              </div>
              <div className="p-2 rounded-lg bg-[#080B09] border border-[#151E18]">
                <div className="text-[#90A396] text-[10px]">Texts</div>
                <div className="font-bold text-[#F1F5F2] font-mono">
                  {inspectorReport.textsCount}
                </div>
              </div>
              <div className="p-2 rounded-lg bg-[#080B09] border border-[#151E18]">
                <div className="text-[#90A396] text-[10px]">Inputs</div>
                <div className="font-bold text-[#00E676] font-mono">
                  {inspectorReport.inputsCount}
                </div>
              </div>
              <div className="p-2 rounded-lg bg-[#080B09] border border-[#151E18]">
                <div className="text-[#90A396] text-[10px]">Actions</div>
                <div className="font-bold text-[#00E676] font-mono">
                  {inspectorReport.actionsCount}
                </div>
              </div>
            </div>

            {/* Candidate Question */}
            <div className="p-2.5 rounded-lg bg-[#080B09] border border-[#151E18] text-xs">
              <div className="flex items-center justify-between mb-1">
                <span className="text-[#5A6F62] uppercase font-bold text-[10px]">
                  Candidate Question
                </span>
                {inspectorReport.questionCandidate && (
                  <span className="text-[#00E676] font-semibold text-[10px]">
                    {inspectorReport.questionCandidate.confidence}% Confidence
                  </span>
                )}
              </div>
              <p className="text-[#F1F5F2] font-mono line-clamp-2">
                {inspectorReport.questionCandidate?.text ||
                  'No question candidate currently detected'}
              </p>
            </div>

            {/* Target Input & Action Candidates */}
            <div className="grid grid-cols-2 gap-2 text-xs">
              <div className="p-2 rounded-lg bg-[#080B09] border border-[#151E18]">
                <span className="text-[#5A6F62] block text-[10px] uppercase font-bold mb-0.5">
                  Input Target
                </span>
                <span className="text-[#00E676] font-mono block truncate">
                  {inspectorReport.inputCandidate?.id || '— None'}
                </span>
                <span className="text-[10px] text-[#90A396] block font-mono truncate">
                  {inspectorReport.inputCandidate?.bounds || 'N/A'}
                </span>
              </div>

              <div className="p-2 rounded-lg bg-[#080B09] border border-[#151E18]">
                <span className="text-[#5A6F62] block text-[10px] uppercase font-bold mb-0.5">
                  Action Button
                </span>
                <span className="text-[#00E676] font-mono block truncate">
                  {inspectorReport.actionCandidate?.label || '— None'}
                </span>
                <span className="text-[10px] text-[#90A396] block font-mono truncate">
                  {inspectorReport.actionCandidate?.bounds || 'N/A'}
                </span>
              </div>
            </div>
          </div>
        )}
      </section>

      {/* ==========================================
          6. ARCHITECTURE ROADMAP
          ========================================== */}
      <section className="p-4 rounded-2xl bg-[#101512] border border-[#1D2821] space-y-2.5">
        <h3 className="text-xs font-bold uppercase tracking-wider text-[#90A396] mb-2">
          Architecture Readiness
        </h3>

        <div className="space-y-2 text-xs">
          <div className="flex items-center justify-between p-2 rounded-lg bg-[#171E19]/60">
            <span className="text-[#F1F5F2]">Module 1: Floating Overlay & Panel</span>
            <span className="text-[#00E676] font-semibold">Active & Ready</span>
          </div>
          <div className="flex items-center justify-between p-2 rounded-lg bg-[#171E19]/60">
            <span className="text-[#F1F5F2]">Module 2: Accessibility Automation</span>
            <span className="text-[#00E676] font-semibold">Operational</span>
          </div>
          <div className="flex items-center justify-between p-2 rounded-lg bg-[#171E19]/60">
            <span className="text-[#F1F5F2]">Module 3: JavaScript AI Solver</span>
            <span className="text-[#33FF8A] font-semibold">Gemini + Local Rules Ready</span>
          </div>
        </div>
      </section>
    </div>
  );
};
