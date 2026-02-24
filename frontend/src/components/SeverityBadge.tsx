const severityStyles: Record<string, { bg: string; text: string }> = {
  critical: { bg: 'rgba(248, 81, 73, 0.15)', text: 'var(--accent-red)' },
  warning: { bg: 'rgba(210, 153, 34, 0.15)', text: 'var(--accent-yellow)' },
  info: { bg: 'rgba(88, 166, 255, 0.15)', text: 'var(--accent-blue)' },
};

export default function SeverityBadge({ severity }: { severity: string }) {
  const style = severityStyles[severity.toLowerCase()] || severityStyles.info;

  return (
    <span
      className="px-2 py-0.5 rounded-full text-xs font-medium"
      style={{ backgroundColor: style.bg, color: style.text }}
    >
      {severity}
    </span>
  );
}
