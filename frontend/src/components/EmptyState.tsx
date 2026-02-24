import { Inbox } from 'lucide-react';

interface EmptyStateProps {
  message: string;
  description?: string;
}

export default function EmptyState({ message, description }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-16 gap-3">
      <Inbox className="w-12 h-12" style={{ color: 'var(--text-muted)' }} />
      <p className="text-lg font-medium" style={{ color: 'var(--text-secondary)' }}>{message}</p>
      {description && (
        <p className="text-sm" style={{ color: 'var(--text-muted)' }}>{description}</p>
      )}
    </div>
  );
}
