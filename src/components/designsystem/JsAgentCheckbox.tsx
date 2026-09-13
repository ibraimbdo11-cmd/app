import React from 'react';
import { JsAgentColors } from '../../core/designsystem/Theme';

interface JsAgentCheckboxProps {
  checked: boolean;
  onCheckedChange?: (checked: boolean) => void;
  size?: number;
  enabled?: boolean;
  className?: string;
  testTag?: string;
}

export const JsAgentCheckbox: React.FC<JsAgentCheckboxProps> = ({
  checked,
  onCheckedChange,
  size = 20,
  enabled = true,
  className = '',
  testTag,
}) => {
  const handleClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (enabled && onCheckedChange) {
      onCheckedChange(!checked);
    }
  };

  return (
    <button
      type="button"
      role="checkbox"
      aria-checked={checked}
      disabled={!enabled}
      onClick={handleClick}
      data-testid={testTag}
      className={`relative inline-flex items-center justify-center p-2 rounded-md transition-colors focus:outline-none focus-visible:ring-1 focus-visible:ring-[#00E676] cursor-pointer disabled:cursor-not-allowed select-none ${className}`}
      style={{ minWidth: 36, minHeight: 36 }}
    >
      <div
        className="flex items-center justify-center transition-all duration-200 rounded-[4px] border"
        style={{
          width: size,
          height: size,
          backgroundColor: !enabled
            ? 'rgba(32, 42, 35, 0.5)'
            : checked
            ? JsAgentColors.Accent
            : JsAgentColors.SurfaceElevated,
          borderColor: !enabled
            ? JsAgentColors.BorderSubtle
            : checked
            ? JsAgentColors.AccentBright
            : 'rgba(29, 40, 33, 0.9)',
          boxShadow: checked
            ? '0 0 10px rgba(0, 230, 118, 0.35)'
            : 'none',
        }}
      >
        {checked && (
          <svg
            className="w-3.5 h-3.5 text-[#06140A] stroke-[2.5]"
            viewBox="0 0 16 16"
            fill="none"
            stroke="currentColor"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <polyline points="2.5 8.5 6.5 12.5 13.5 3.5" />
          </svg>
        )}
      </div>
    </button>
  );
};

interface JsAgentControlRowProps {
  label: string;
  checked: boolean;
  onCheckedChange: (checked: boolean) => void;
  enabled?: boolean;
  testTag?: string;
  className?: string;
}

export const JsAgentControlRow: React.FC<JsAgentControlRowProps> = ({
  label,
  checked,
  onCheckedChange,
  enabled = true,
  testTag,
  className = '',
}) => {
  return (
    <div
      onClick={() => enabled && onCheckedChange(!checked)}
      className={`flex items-center justify-between px-3 py-1.5 rounded-lg cursor-pointer hover:bg-[#171E19]/60 transition-colors ${className}`}
      data-testid={testTag}
    >
      <span
        className="text-sm font-medium transition-colors"
        style={{
          color: enabled ? JsAgentColors.TextPrimary : JsAgentColors.TextTertiary,
        }}
      >
        {label}
      </span>
      <JsAgentCheckbox
        checked={checked}
        onCheckedChange={onCheckedChange}
        enabled={enabled}
        testTag={testTag ? `${testTag}_checkbox` : undefined}
      />
    </div>
  );
};
