import React, { useState, useRef, useEffect } from 'react';
import { AgentState } from '../../types';
import { FloatingBubble } from './FloatingBubble';
import { FloatingPanel } from './FloatingPanel';

interface FloatingAgentGroupProps {
  agentState: AgentState;
  isPanelOpen: boolean;
  onTogglePanel: () => void;
  onToggleAgent: () => void;
  onToggleFloating: (enabled: boolean) => void;
}

export const FloatingAgentGroup: React.FC<FloatingAgentGroupProps> = ({
  agentState,
  isPanelOpen,
  onTogglePanel,
  onToggleAgent,
  onToggleFloating,
}) => {
  if (!agentState.isFloatingAssistantEnabled) return null;

  // Position state (defaults to bottom-right)
  const [position, setPosition] = useState<{ x: number; y: number }>(() => {
    const defaultX = typeof window !== 'undefined' ? Math.max(20, window.innerWidth - 380) : 100;
    const defaultY = typeof window !== 'undefined' ? Math.max(40, window.innerHeight - 440) : 100;
    return { x: defaultX, y: defaultY };
  });

  const isDraggingRef = useRef(false);
  const dragStartRef = useRef<{ x: number; y: number }>({ x: 0, y: 0 });
  const initialPosRef = useRef<{ x: number; y: number }>({ x: 0, y: 0 });

  // Handle window resizing to keep inside viewport
  useEffect(() => {
    const handleResize = () => {
      setPosition(prev => ({
        x: Math.min(prev.x, Math.max(10, window.innerWidth - 340)),
        y: Math.min(prev.y, Math.max(10, window.innerHeight - 380)),
      }));
    };
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const startDrag = (clientX: number, clientY: number) => {
    isDraggingRef.current = true;
    dragStartRef.current = { x: clientX, y: clientY };
    initialPosRef.current = { ...position };

    const handlePointerMove = (e: PointerEvent) => {
      if (!isDraggingRef.current) return;
      const dx = e.clientX - dragStartRef.current.x;
      const dy = e.clientY - dragStartRef.current.y;

      const maxX = Math.max(10, window.innerWidth - (isPanelOpen ? 340 : 80));
      const maxY = Math.max(10, window.innerHeight - (isPanelOpen ? 400 : 80));

      const newX = Math.max(10, Math.min(maxX, initialPosRef.current.x + dx));
      const newY = Math.max(10, Math.min(maxY, initialPosRef.current.y + dy));

      setPosition({ x: newX, y: newY });
    };

    const handlePointerUp = () => {
      isDraggingRef.current = false;
      window.removeEventListener('pointermove', handlePointerMove);
      window.removeEventListener('pointerup', handlePointerUp);
    };

    window.addEventListener('pointermove', handlePointerMove);
    window.addEventListener('pointerup', handlePointerUp);
  };

  const handleBubblePointerDown = (e: React.PointerEvent) => {
    // Only drag bubble if panel is closed
    if (!isPanelOpen) {
      startDrag(e.clientX, e.clientY);
    }
  };

  const handleHeaderPointerDown = (e: React.PointerEvent) => {
    startDrag(e.clientX, e.clientY);
  };

  return (
    <div
      className="fixed z-50 pointer-events-auto select-none"
      style={{
        left: `${position.x}px`,
        top: `${position.y}px`,
        touchAction: 'none',
      }}
    >
      <div className="relative">
        {/* 1. Floating Bubble (Always rendered, placed at top-start with high z-index) */}
        <div
          onPointerDown={handleBubblePointerDown}
          className="relative z-30 inline-block"
        >
          <FloatingBubble
            status={agentState.status}
            onClick={onTogglePanel}
          />
        </div>

        {/* 2. Floating Panel (Rendered with offset so bubble overlaps neatly) */}
        {isPanelOpen && (
          <div className="absolute top-4 left-4 z-20 animate-in fade-in zoom-in-95 duration-150">
            <FloatingPanel
              agentState={agentState}
              isVisible={isPanelOpen}
              onToggleAgent={onToggleAgent}
              onToggleFloating={onToggleFloating}
              onHeaderPointerDown={handleHeaderPointerDown}
            />
          </div>
        )}
      </div>
    </div>
  );
};
