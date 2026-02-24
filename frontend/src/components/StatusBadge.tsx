const statusStyles: Record<string, { bg: string; text: string }> = {
  open: { bg: 'rgba(63, 185, 80, 0.15)', text: 'var(--accent-green)' },
  closed: { bg: 'rgba(248, 81, 73, 0.15)', text: 'var(--accent-red)' },
  merged: { bg: 'rgba(188, 140, 255, 0.15)', text: 'var(--accent-purple)' },
  pending: { bg: 'rgba(210, 153, 34, 0.15)', text: 'var(--accent-yellow)' },
  completed: { bg: 'rgba(63, 185, 80, 0.15)', text: 'var(--accent-green)' },
  failed: { bg: 'rgba(248, 81, 73, 0.15)', text: 'var(--accent-red)' },
};

export default function StatusBadge({ status }: { status: string }) {
  const style = statusStyles[status.toLowerCase()] || statusStyles.pending;

  return (
    <span
      className="px-2 py-0.5 rounded-full text-xs font-medium"
      style={{ backgroundColor: style.bg, color: style.text }}
    >
      {status}
    </span>
  );
}
