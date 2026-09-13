import React, { useEffect, useRef } from 'react';
import { JsAgentColors } from '../../core/designsystem/Theme';

interface JsAgentLogoProps {
  size?: number;
  isActive?: boolean;
  showGlow?: boolean;
  accentColor?: string;
  baseColor?: string;
  className?: string;
}

export const JsAgentLogo: React.FC<JsAgentLogoProps> = ({
  size = 48,
  isActive = false,
  showGlow = true,
  accentColor = JsAgentColors.Accent,
  baseColor = '#FFFFFF',
  className = '',
}) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let animationFrameId: number;
    let startTime = performance.now();

    const render = (time: number) => {
      const elapsed = time - startTime;
      const pulseDuration = isActive ? 1200 : 2200;
      const progress = (elapsed % pulseDuration) / pulseDuration;
      // Sine wave pulse between 0.25 and 0.65
      const pulseAlpha =
        0.3 + 0.35 * Math.sin(progress * Math.PI * 2);

      const dpr = window.devicePixelRatio || 1;
      canvas.width = size * dpr;
      canvas.height = size * dpr;
      ctx.resetTransform?.();
      ctx.scale(dpr, dpr);

      ctx.clearRect(0, 0, size, size);

      const w = size;
      const h = size;
      const centerX = w / 2;
      const centerY = h / 2;
      const strokeWidth = Math.max(1.6, w * 0.065);

      // 1. Soft Ambient Glow
      if (showGlow) {
        const glowGrad = ctx.createRadialGradient(
          centerX,
          centerY,
          w * 0.1,
          centerX,
          centerY,
          w * 0.48
        );
        glowGrad.addColorStop(0, `rgba(0, 230, 118, ${pulseAlpha * 0.45})`);
        glowGrad.addColorStop(1, 'rgba(0, 230, 118, 0)');
        ctx.fillStyle = glowGrad;
        ctx.beginPath();
        ctx.arc(centerX, centerY, w * 0.48, 0, Math.PI * 2);
        ctx.fill();
      }

      // 2. Outer Tech Arc / Orbit
      ctx.beginPath();
      const startAngle = (-40 * Math.PI) / 180;
      const endAngle = (200 * Math.PI) / 180;
      ctx.arc(centerX, centerY, w * 0.38, startAngle, endAngle, false);
      ctx.strokeStyle = `rgba(0, 230, 118, ${isActive ? 0.9 : 0.45})`;
      ctx.lineWidth = strokeWidth * 0.65;
      ctx.lineCap = 'round';
      ctx.stroke();

      // 3. AI Pulse Core Node (at -40 deg angle)
      const nodeX = centerX + w * 0.38 * Math.cos(startAngle);
      const nodeY = centerY + w * 0.38 * Math.sin(startAngle);

      // AI Node Glow
      ctx.beginPath();
      ctx.arc(nodeX, nodeY, strokeWidth * 1.6, 0, Math.PI * 2);
      ctx.fillStyle = `rgba(0, 230, 118, ${pulseAlpha})`;
      ctx.fill();

      // AI Node Center Dot
      ctx.beginPath();
      ctx.arc(nodeX, nodeY, strokeWidth * 0.85, 0, Math.PI * 2);
      ctx.fillStyle = isActive ? JsAgentColors.AccentBright : accentColor;
      ctx.fill();

      // 4. Stylized J Letter (Base color: White)
      ctx.beginPath();
      ctx.moveTo(w * 0.44, h * 0.38);
      ctx.lineTo(w * 0.44, h * 0.58);
      ctx.bezierCurveTo(w * 0.44, h * 0.68, w * 0.33, h * 0.68, w * 0.33, h * 0.6);
      ctx.strokeStyle = baseColor;
      ctx.lineWidth = strokeWidth;
      ctx.lineCap = 'round';
      ctx.lineJoin = 'round';
      ctx.stroke();

      // 5. Stylized S Letter (Matrix Green)
      ctx.beginPath();
      ctx.moveTo(w * 0.67, h * 0.41);
      ctx.lineTo(w * 0.54, h * 0.41);
      ctx.bezierCurveTo(w * 0.5, h * 0.41, w * 0.5, h * 0.5, w * 0.57, h * 0.5);
      ctx.bezierCurveTo(w * 0.67, h * 0.5, w * 0.67, h * 0.63, w * 0.58, h * 0.63);
      ctx.lineTo(w * 0.5, h * 0.63);
      ctx.strokeStyle = accentColor;
      ctx.lineWidth = strokeWidth;
      ctx.lineCap = 'round';
      ctx.lineJoin = 'round';
      ctx.stroke();

      // 6. Terminal Tech Brackets < >
      // Left bracket <
      ctx.beginPath();
      ctx.moveTo(w * 0.22, h * 0.46);
      ctx.lineTo(w * 0.17, h * 0.51);
      ctx.lineTo(w * 0.22, h * 0.56);
      ctx.strokeStyle = `rgba(0, 230, 118, 0.5)`;
      ctx.lineWidth = strokeWidth * 0.5;
      ctx.lineCap = 'round';
      ctx.lineJoin = 'round';
      ctx.stroke();

      // Right bracket >
      ctx.beginPath();
      ctx.moveTo(w * 0.78, h * 0.46);
      ctx.lineTo(w * 0.83, h * 0.51);
      ctx.lineTo(w * 0.78, h * 0.56);
      ctx.stroke();

      animationFrameId = requestAnimationFrame(render);
    };

    animationFrameId = requestAnimationFrame(render);

    return () => {
      cancelAnimationFrame(animationFrameId);
    };
  }, [size, isActive, showGlow, accentColor, baseColor]);

  return (
    <div
      className={`inline-flex items-center justify-center shrink-0 ${className}`}
      style={{ width: size, height: size }}
      aria-label="JS Agent Logo"
      role="img"
    >
      <canvas
        ref={canvasRef}
        style={{ width: `${size}px`, height: `${size}px` }}
        className="block"
      />
    </div>
  );
};
