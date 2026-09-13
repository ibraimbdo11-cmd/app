import React from 'react';
import { AgentState } from '../../types';
import { JsAgentColors } from '../../core/designsystem/Theme';
import { JsAgentLogo } from '../designsystem/JsAgentLogo';
import {
  ArrowLeft,
  Moon,
  Layers,
  ShieldCheck,
  Search,
  Info,
  CheckCircle2,
} from 'lucide-react';

interface SettingsScreenProps {
  agentState: AgentState;
  onNavigateBack: () => void;
  onToggleFloating: (enabled: boolean) => void;
  onToggleAccessibility: (enabled: boolean) => void;
  onToggleInspector: (enabled: boolean) => void;
}

export const SettingsScreen: React.FC<SettingsScreenProps> = ({
  agentState,
  onNavigateBack,
  onToggleFloating,
  onToggleAccessibility,
  onToggleInspector,
}) => {
  return (
    <div className="w-full max-w-2xl mx-auto px-4 py-6 space-y-5 pb-24 text-left">
      {/* Header */}
      <header className="flex items-center space-x-3 pb-2 border-b border-[#1D2821]">
        <button
          type="button"
          onClick={onNavigateBack}
          className="p-2 rounded-xl bg-[#171E19] hover:bg-[#202A23] border border-[#1D2821] text-[#90A396] hover:text-[#00E676] transition-colors cursor-pointer"
          title="Back to Home"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <div>
          <h1 className="text-xl font-bold tracking-tight text-[#F1F5F2]">
            Settings
          </h1>
          <p className="text-xs text-[#90A396]">Configure JS Agent features</p>
        </div>
      </header>

      {/* 1. Appearance Card */}
      <section className="p-4 rounded-2xl bg-[#101512] border border-[#1D2821] space-y-2">
        <div className="flex items-center space-x-3">
          <div className="p-2 rounded-lg bg-[#171E19] text-[#00E676] border border-[#1D2821]">
            <Moon className="w-4 h-4" />
          </div>
          <div>
            <h2 className="text-sm font-semibold text-[#F1F5F2]">Appearance</h2>
            <p className="text-xs text-[#90A396]">
              Dark Matrix AI (Default &amp; Fixed)
            </p>
          </div>
        </div>
      </section>

      {/* 2. Floating Assistant */}
      <section className="p-4 rounded-2xl bg-[#101512] border border-[#1D2821] space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="p-2 rounded-lg bg-[#171E19] text-[#00E676] border border-[#1D2821]">
              <Layers className="w-4 h-4" />
            </div>
            <div>
              <h2 className="text-sm font-semibold text-[#F1F5F2]">
                Floating Assistant
              </h2>
              <p className="text-xs text-[#90A396]">
                Display floating bubble &amp; quick control panel
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
          <span className="text-[#5A6F62]">Overlay Permission:</span>
          <span className="text-[#00E676] font-medium flex items-center space-x-1">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>Granted &amp; Ready</span>
          </span>
        </div>
      </section>

      {/* 3. Accessibility Service */}
      <section className="p-4 rounded-2xl bg-[#101512] border border-[#1D2821] space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="p-2 rounded-lg bg-[#171E19] text-[#00E676] border border-[#1D2821]">
              <ShieldCheck className="w-4 h-4" />
            </div>
            <div>
              <h2 className="text-sm font-semibold text-[#F1F5F2]">
                Accessibility Automation
              </h2>
              <p className="text-xs text-[#90A396]">
                Inspect JavaScript problem structures and input fields
              </p>
            </div>
          </div>

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
      </section>

      {/* 4. Structural Inspector */}
      <section className="p-4 rounded-2xl bg-[#101512] border border-[#1D2821] space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="p-2 rounded-lg bg-[#171E19] text-[#00E676] border border-[#1D2821]">
              <Search className="w-4 h-4" />
            </div>
            <div>
              <h2 className="text-sm font-semibold text-[#F1F5F2]">
                Developer Inspector Mode
              </h2>
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
      </section>

      {/* 5. About Card */}
      <section className="p-4 rounded-2xl bg-[#101512] border border-[#1D2821] space-y-3">
        <div className="flex items-start space-x-3">
          <div className="p-2 rounded-lg bg-[#171E19] text-[#00E676] border border-[#1D2821]">
            <Info className="w-4 h-4" />
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <h2 className="text-sm font-semibold text-[#F1F5F2]">About JS Agent</h2>
              <span className="px-2 py-0.5 text-[10px] font-bold rounded-full bg-[#171E19] text-[#00E676] border border-[#1D2821]">
                Version 1.0
              </span>
            </div>
            <p className="text-xs text-[#90A396] mt-1.5 leading-relaxed">
              JS Agent is a dedicated smart assistant designed for JavaScript questions and automation in authorized test environments.
            </p>
          </div>
        </div>

        <div className="pt-2 border-t border-[#1D2821]/60 flex items-center justify-between text-xs text-[#5A6F62]">
          <span>Engine: AI Gemini + Deterministic Solver</span>
          <span className="text-[#00E676]">Production Ready</span>
        </div>
      </section>
    </div>
  );
};
