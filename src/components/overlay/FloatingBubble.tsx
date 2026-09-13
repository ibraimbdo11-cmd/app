import React from 'react';
import { AgentStatus, isAgentWorking } from '../../types';
import { JsAgentColors, JsAgentDimens } from '../../core/designsystem/Theme';
import { JsAgentLogo } from '../designsystem/JsAgentLogo';

interface FloatingBubbleProps {
  status: AgentStatus;
  onClick: () => void;
  isDragging?: boolean;
}

export const FloatingBubble: React.FC<FloatingBubbleProps> = ({
  status,
  onClick,
  isDragging = false,
}) => {
  const isWorking = isAgentWorking(status);
  const isError = status === AgentStatus.ERROR;
  const isReadyOrScanning = [
    AgentStatus.READY,
    AgentStatus.SCANNING,
    AgentStatus.ANALYZING,
  ].includes(status);

  const ringColor = isError
    ? JsAgentColors.Error
    : isWorking
    ? JsAgentColors.AccentBright
    : isReadyOrScanning
    ? JsAgentColors.Accent
    : 'rgba(0, 230, 118, 0.45)';

  return (
    <button
      type="button"
      onClick={onClick}
      aria-label="Toggle JS Agent Floating Panel"
      className="relative flex items-center justify-center cursor-pointer select-none focus:outline-none group active:scale-95 transition-transform"
      style={{
        width: JsAgentDimens.FloatingBubbleSize + 8,
        height: JsAgentDimens.FloatingBubbleSize + 8,
      }}
    >
      {/* Outer Pulsing Aura */}
      <div
        className={`absolute inset-0 rounded-full transition-all duration-300 ${
          isWorking ? 'animate-ping opacity-25' : 'animate-pulse opacity-40'
        }`}
        style={{
          backgroundColor: isError
            ? 'rgba(255, 82, 82, 0.35)'
            : 'rgba(0, 230, 118, 0.25)',
        }}
      />

      {/* Core Dark Bubble */}
      <div
        className="relative z-10 flex items-center justify-center rounded-full border transition-all duration-200"
        style={{
          width: JsAgentDimens.FloatingBubbleSize - 2,
          height: JsAgentDimens.FloatingBubbleSize - 2,
          backgroundColor: JsAgentColors.SurfaceElevated,
          backgroundImage: `radial-gradient(circle at 30% 30%, #171E19 0%, #080B09 100%)`,
          borderColor: ringColor,
          borderWidth: isWorking ? 2 : 1.5,
          boxShadow: isWorking
            ? '0 0 16px rgba(0, 230, 118, 0.45)'
            : '0 8px 24px rgba(0, 0, 0, 0.65)',
        }}
      >
        <JsAgentLogo
          size={JsAgentDimens.FloatingBubbleInnerLogoSize}
          isActive={isWorking || isReadyOrScanning}
          showGlow={false}
        />
      </div>
    </button>
  );
};
