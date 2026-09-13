import React from 'react';
import { AgentState, AgentStatus, getStatusDisplayName, isAgentRunning } from '../../types';
import { JsAgentColors, JsAgentDimens } from '../../core/designsystem/Theme';
import { JsAgentLogo } from '../designsystem/JsAgentLogo';
import { JsAgentControlRow } from '../designsystem/JsAgentCheckbox';

interface FloatingPanelProps {
  agentState: AgentState;
  isVisible: boolean;
  onToggleAgent: () => void;
  onToggleFloating: (enabled: boolean) => void;
  onHeaderPointerDown?: (e: React.PointerEvent) => void;
}

export const FloatingPanel: React.FC<FloatingPanelProps> = ({
  agentState,
  isVisible,
  onToggleAgent,
  onToggleFloating,
  onHeaderPointerDown,
}) => {
  if (!isVisible) return null;

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
    <div
      className="flex flex-col rounded-2xl border shadow-2xl overflow-hidden transition-all duration-200 select-none"
      style={{
        width: JsAgentDimens.FloatingPanelWidth,
        backgroundColor: JsAgentColors.Surface,
        borderColor: isRunning ? JsAgentColors.BorderGlow : JsAgentColors.Border,
        boxShadow: '0 20px 40px rgba(0,0,0,0.8), 0 0 20px rgba(0, 230, 118, 0.08)',
      }}
    >
      {/* 1. DEDICATED HEADER & DRAG AREA */}
      <div
        onPointerDown={onHeaderPointerDown}
        className="flex flex-col items-center px-3 py-2 bg-[#171E19]/80 border-b border-[#1D2821] cursor-grab active:cursor-grabbing hover:bg-[#171E19] transition-colors"
        title="Drag floating panel"
      >
        {/* Drag handle pill */}
        <div className="w-9 h-1 rounded-full bg-[#00E676]/45 mb-1.5" />

        <div className="flex items-center justify-between w-full">
          <div className="flex items-center space-x-2">
            <JsAgentLogo size={22} isActive={isRunning} showGlow={false} />
            <div className="flex flex-col text-left">
              <span className="text-xs font-bold text-[#F1F5F2] leading-tight">
                JS Agent
              </span>
              <span className="text-[9px] text-[#90A396] leading-tight">
                JavaScript Automation Agent
              </span>
            </div>
          </div>

          <span className="px-1.5 py-0.5 text-[8px] font-bold tracking-wider text-[#00E676]/80 border border-[#00E676]/30 rounded">
            DRAG
          </span>
        </div>
      </div>

      {/* Content Body */}
      <div className="p-3 space-y-2.5 text-left max-h-[420px] overflow-y-auto">
        {/* 2. STATUS CARD */}
        <div className="flex items-center justify-between px-2.5 py-1.5 bg-[#101512] border border-[#1D2821] rounded-lg">
          <span className="text-xs text-[#90A396]">Agent Status</span>
          <div className="flex items-center space-x-1.5">
            <span
              className="w-2 h-2 rounded-full"
              style={{ backgroundColor: statusDotColor }}
            />
            <span
              className="text-xs font-semibold"
              style={{ color: statusDotColor }}
            >
              ● {getStatusDisplayName(agentState.status)}
            </span>
          </div>
        </div>

        {/* 3. CONTROLS (Custom Square Checkboxes) */}
        <div className="bg-[#101512] border border-[#1D2821] rounded-lg py-1 divide-y divide-[#1D2821]/60">
          <JsAgentControlRow
            label="Agent"
            checked={isRunning}
            onCheckedChange={onToggleAgent}
            testTag="panel_agent_control"
          />
          <JsAgentControlRow
            label="Floating Panel"
            checked={agentState.isFloatingAssistantEnabled}
            onCheckedChange={onToggleFloating}
            testTag="panel_floating_control"
          />
        </div>

        {/* 4. CURRENT QUESTION */}
        <div className="px-2.5 py-1.5 bg-[#101512] border border-[#1D2821] rounded-lg">
          <span className="block text-[10px] uppercase font-semibold text-[#5A6F62] mb-0.5">
            Current Question
          </span>
          <p className="text-xs text-[#F1F5F2] line-clamp-2 leading-relaxed font-mono">
            {agentState.currentQuestionSnippet || 'No active question'}
          </p>
        </div>

        {/* 5. TARGET SUMMARY */}
        <div className="flex items-center justify-between px-2.5 py-1.5 bg-[#101512] border border-[#1D2821] rounded-lg text-xs">
          <div className="flex items-center space-x-1">
            <span className="text-[#90A396]">Input:</span>
            <span
              className={`font-bold ${
                agentState.hasDetectedInput ? 'text-[#00E676]' : 'text-[#5A6F62]'
              }`}
            >
              {agentState.hasDetectedInput ? '✓ Detected' : '— None'}
            </span>
          </div>

          <div className="flex items-center space-x-1">
            <span className="text-[#90A396]">Action:</span>
            <span
              className={`font-bold ${
                agentState.hasDetectedAction ? 'text-[#00E676]' : 'text-[#5A6F62]'
              }`}
            >
              {agentState.hasDetectedAction ? '✓ Detected' : '— None'}
            </span>
          </div>
        </div>

        {/* 6. ACTIVITY */}
        <div className="px-2.5 py-1.5 bg-[#101512] border border-[#1D2821] rounded-lg">
          <span className="block text-[10px] uppercase font-semibold text-[#5A6F62] mb-0.5">
            Activity
          </span>
          <p className="text-xs text-[#33FF8A] line-clamp-2 leading-relaxed">
            {agentState.latestActivity}
          </p>
        </div>

        {/* 7. SESSION STATISTICS */}
        <div className="flex items-center justify-between px-2.5 py-1.5 bg-[#171E19] border border-[#1D2821] rounded-lg text-xs">
          <span className="font-bold text-[#F1F5F2]">Session</span>
          <div className="flex items-center space-x-3 font-semibold">
            <span className="text-[#00E676]">
              Solved: {agentState.sessionSolvedCount}
            </span>
            <span
              className={
                agentState.sessionFailedCount > 0 ? 'text-[#FF5252]' : 'text-[#5A6F62]'
              }
            >
              Failed: {agentState.sessionFailedCount}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};
