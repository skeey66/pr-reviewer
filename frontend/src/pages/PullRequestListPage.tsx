import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Play, ChevronDown, ChevronRight, ArrowLeft, Calendar, GitPullRequest, CheckCircle, XCircle } from 'lucide-react';
import { useApi } from '@/hooks/useApi';
import { getPullRequests, getReviews, triggerReview } from '@/api';
import type { PullRequestResponse, ReviewRunResponse } from '@/types';
import LoadingSpinner from '@/components/LoadingSpinner';
import EmptyState from '@/components/EmptyState';
import StatusBadge from '@/components/StatusBadge';

function formatRelativeTime(dateStr: string): string {
  const date = new Date(dateStr);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMinutes = Math.floor(diffMs / (1000 * 60));
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

  if (diffMinutes < 1) return '방금 전';
  if (diffMinutes < 60) return `${diffMinutes}분 전`;
  if (diffHours < 24) return `${diffHours}시간 전`;
  if (diffDays < 7) return `${diffDays}일 전`;
  if (diffDays < 30) return `${Math.floor(diffDays / 7)}주 전`;
  if (diffDays < 365) return `${Math.floor(diffDays / 30)}개월 전`;
  return date.toLocaleDateString('ko-KR');
}

function getDateGroup(dateStr: string): string {
  const date = new Date(dateStr);
  const now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const dateOnly = new Date(date.getFullYear(), date.getMonth(), date.getDate());
  const diffDays = Math.floor((today.getTime() - dateOnly.getTime()) / (1000 * 60 * 60 * 24));

  if (diffDays === 0) return '오늘';
  if (diffDays <= 7) return '이번 주';
  if (diffDays <= 30) return '이번 달';
  return '이전';
}

const GROUP_ORDER = ['오늘', '이번 주', '이번 달', '이전'];

export default function PullRequestListPage() {
  const { id } = useParams<{ id: string }>();
  const subscriptionId = Number(id);
  const { data: pulls, loading, error } = useApi(
    () => getPullRequests(subscriptionId),
    [subscriptionId],
  );

  if (loading) return <LoadingSpinner />;
  if (error) return <div style={{ color: 'var(--accent-red)' }}>오류: {error}</div>;

  if (!pulls?.length) {
    return (
      <div>
        <div className="flex items-center gap-3 mb-6">
          <Link to="/" className="p-1" style={{ color: 'var(--text-secondary)' }}>
            <ArrowLeft className="w-5 h-5" />
          </Link>
          <h1 className="text-xl font-bold">Pull Requests</h1>
        </div>
        <EmptyState message="PR이 없습니다" description="저장소에 열린 PR이 생기면 자동으로 표시됩니다" />
      </div>
    );
  }

  const openCount = pulls.filter((p) => p.state === 'open').length;
  const closedCount = pulls.filter((p) => p.state !== 'open').length;

  const grouped = pulls.reduce((acc, pr) => {
    const group = getDateGroup(pr.openedAt);
    if (!acc[group]) acc[group] = [];
    acc[group].push(pr);
    return acc;
  }, {} as Record<string, PullRequestResponse[]>);

  return (
    <div>
      <div className="flex items-center gap-3 mb-6">
        <Link to="/" className="p-1" style={{ color: 'var(--text-secondary)' }}>
          <ArrowLeft className="w-5 h-5" />
        </Link>
        <h1 className="text-xl font-bold">Pull Requests</h1>
      </div>

      {/* 통계 요약 */}
      <div className="grid grid-cols-3 gap-3 mb-6">
        <div
          className="rounded-xl p-4 flex items-center gap-3"
          style={{ backgroundColor: 'var(--bg-secondary)', border: '1px solid var(--border-primary)' }}
        >
          <GitPullRequest className="w-5 h-5 shrink-0" style={{ color: 'var(--accent-blue)' }} />
          <div>
            <p className="text-2xl font-bold">{pulls.length}</p>
            <p className="text-xs" style={{ color: 'var(--text-muted)' }}>전체 PR</p>
          </div>
        </div>
        <div
          className="rounded-xl p-4 flex items-center gap-3"
          style={{ backgroundColor: 'var(--bg-secondary)', border: '1px solid var(--border-primary)' }}
        >
          <CheckCircle className="w-5 h-5 shrink-0" style={{ color: 'var(--accent-green)' }} />
          <div>
            <p className="text-2xl font-bold">{openCount}</p>
            <p className="text-xs" style={{ color: 'var(--text-muted)' }}>열린 PR</p>
          </div>
        </div>
        <div
          className="rounded-xl p-4 flex items-center gap-3"
          style={{ backgroundColor: 'var(--bg-secondary)', border: '1px solid var(--border-primary)' }}
        >
          <XCircle className="w-5 h-5 shrink-0" style={{ color: 'var(--text-muted)' }} />
          <div>
            <p className="text-2xl font-bold">{closedCount}</p>
            <p className="text-xs" style={{ color: 'var(--text-muted)' }}>닫힌 PR</p>
          </div>
        </div>
      </div>

      {/* 날짜 그룹별 PR 목록 */}
      <div className="space-y-6">
        {GROUP_ORDER.filter((g) => grouped[g]?.length).map((group) => (
          <div key={group}>
            <div className="flex items-center gap-2 mb-3">
              <Calendar className="w-4 h-4" style={{ color: 'var(--accent-blue)' }} />
              <span className="text-sm font-semibold" style={{ color: 'var(--text-secondary)' }}>{group}</span>
              <span
                className="text-xs px-2 py-0.5 rounded-full"
                style={{ backgroundColor: 'var(--bg-tertiary)', color: 'var(--text-muted)' }}
              >
                {grouped[group].length}
              </span>
              <div className="flex-1 h-px" style={{ backgroundColor: 'var(--border-primary)' }} />
            </div>
            <div className="space-y-3">
              {grouped[group].map((pr) => (
                <PullRequestCard key={pr.id} pr={pr} />
              ))}
            </div>
          </div>
        ))}
      </div>
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

  const openedDate = new Date(pr.openedAt);

  return (
    <div
      className="rounded-xl overflow-hidden"
      style={{ backgroundColor: 'var(--bg-secondary)', border: '1px solid var(--border-primary)' }}
    >
      <div className="p-4">
        <div className="flex items-start justify-between gap-3">
          <div className="min-w-0 flex-1">
            <div className="flex items-center gap-2 mb-2">
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
            <div className="flex flex-wrap items-center gap-x-3 gap-y-1 text-xs ml-6" style={{ color: 'var(--text-muted)' }}>
              <span
                className="px-1.5 py-0.5 rounded font-mono"
                style={{ backgroundColor: 'var(--bg-tertiary)' }}
              >
                #{pr.prNumber}
              </span>
              <span>{pr.author}</span>
              <span style={{ color: 'var(--border-primary)' }}>·</span>
              <span>{pr.baseBranch} ← {pr.headBranch}</span>
              <span style={{ color: 'var(--border-primary)' }}>·</span>
              <span
                className="flex items-center gap-1"
                title={openedDate.toLocaleString('ko-KR')}
                style={{ cursor: 'default' }}
              >
                <Calendar className="w-3 h-3" />
                {formatRelativeTime(pr.openedAt)}
              </span>
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
                    <span
                      className="flex items-center gap-1 text-xs"
                      style={{ color: 'var(--text-muted)' }}
                      title={new Date(review.createdAt).toLocaleString('ko-KR')}
                    >
                      <Calendar className="w-3 h-3" />
                      {formatRelativeTime(review.createdAt)}
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
