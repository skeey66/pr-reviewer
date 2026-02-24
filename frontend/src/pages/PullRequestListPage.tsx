import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Play, ChevronDown, ChevronRight, ArrowLeft } from 'lucide-react';
import { useApi } from '@/hooks/useApi';
import { getPullRequests, getReviews, triggerReview } from '@/api';
import type { PullRequestResponse, ReviewRunResponse } from '@/types';
import LoadingSpinner from '@/components/LoadingSpinner';
import EmptyState from '@/components/EmptyState';
import StatusBadge from '@/components/StatusBadge';

export default function PullRequestListPage() {
  const { id } = useParams<{ id: string }>();
  const subscriptionId = Number(id);
  const { data: pulls, loading, error } = useApi(
    () => getPullRequests(subscriptionId),
    [subscriptionId],
  );

  if (loading) return <LoadingSpinner />;
  if (error) return <div style={{ color: 'var(--accent-red)' }}>오류: {error}</div>;

  return (
    <div>
      <div className="flex items-center gap-3 mb-6">
        <Link to="/" className="p-1" style={{ color: 'var(--text-secondary)' }}>
          <ArrowLeft className="w-5 h-5" />
        </Link>
        <h1 className="text-xl font-bold">Pull Requests</h1>
      </div>

      {!pulls?.length ? (
        <EmptyState message="PR이 없습니다" description="저장소에 열린 PR이 생기면 자동으로 표시됩니다" />
      ) : (
        <div className="space-y-3">
          {pulls.map((pr) => (
            <PullRequestCard key={pr.id} pr={pr} />
          ))}
        </div>
      )}
    </div>
  );
}

function PullRequestCard({ pr }: { pr: PullRequestResponse }) {
  const [expanded, setExpanded] = useState(false);
  const [triggering, setTriggering] = useState(false);
  const { data: reviews, loading: reviewsLoading, refetch } = useApi(
    () => (expanded ? getReviews(pr.id) : Promise.resolve([])),
    [expanded, pr.id],
  );

  const handleTrigger = async () => {
    setTriggering(true);
    await triggerReview(pr.id);
    setTriggering(false);
    refetch();
  };

  return (
    <div
      className="rounded-xl overflow-hidden"
      style={{ backgroundColor: 'var(--bg-secondary)', border: '1px solid var(--border-primary)' }}
    >
      <div className="p-4">
        <div className="flex items-start justify-between gap-3">
          <div className="min-w-0 flex-1">
            <div className="flex items-center gap-2 mb-1">
              <button
                onClick={() => setExpanded(!expanded)}
                className="p-0.5 cursor-pointer"
                style={{ background: 'none', border: 'none', color: 'var(--text-secondary)' }}
              >
                {expanded ? <ChevronDown className="w-4 h-4" /> : <ChevronRight className="w-4 h-4" />}
              </button>
              <span className="font-semibold truncate">{pr.title}</span>
              <StatusBadge status={pr.state} />
            </div>
            <div className="flex items-center gap-3 text-xs ml-6" style={{ color: 'var(--text-muted)' }}>
              <span>#{pr.prNumber}</span>
              <span>{pr.author}</span>
              <span>{pr.baseBranch} &larr; {pr.headBranch}</span>
              <span>{new Date(pr.openedAt).toLocaleDateString('ko-KR')}</span>
            </div>
          </div>
          <button
            onClick={handleTrigger}
            disabled={triggering}
            className="flex items-center gap-1 px-3 py-1.5 rounded-lg text-xs font-medium shrink-0 cursor-pointer"
            style={{
              backgroundColor: 'var(--accent-green)',
              color: 'var(--bg-primary)',
              border: 'none',
              opacity: triggering ? 0.5 : 1,
            }}
          >
            <Play className="w-3 h-3" />
            {triggering ? '실행 중...' : '리뷰'}
          </button>
        </div>
      </div>

      {expanded && (
        <div className="px-4 pb-4 pt-0">
          <div className="border-t pt-3" style={{ borderColor: 'var(--border-primary)' }}>
            {reviewsLoading ? (
              <LoadingSpinner />
            ) : !reviews?.length ? (
              <p className="text-sm py-2" style={{ color: 'var(--text-muted)' }}>리뷰 기록이 없습니다</p>
            ) : (
              <div className="space-y-2">
                {reviews.map((review: ReviewRunResponse) => (
                  <Link
                    key={review.id}
                    to={`/reviews/${review.id}`}
                    className="flex items-center justify-between p-3 rounded-lg no-underline"
                    style={{ backgroundColor: 'var(--bg-tertiary)' }}
                  >
                    <div className="flex items-center gap-3">
                      <StatusBadge status={review.status} />
                      <span className="text-sm" style={{ color: 'var(--text-primary)' }}>{review.model}</span>
                    </div>
                    <span className="text-xs" style={{ color: 'var(--text-muted)' }}>
                      {new Date(review.createdAt).toLocaleString('ko-KR')}
                    </span>
                  </Link>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
