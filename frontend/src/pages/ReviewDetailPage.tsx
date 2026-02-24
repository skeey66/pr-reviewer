import { useParams, Link } from 'react-router-dom';
import { ArrowLeft, FileCode, Clock } from 'lucide-react';
import { useApi } from '@/hooks/useApi';
import { getReviewDetail } from '@/api';
import LoadingSpinner from '@/components/LoadingSpinner';
import EmptyState from '@/components/EmptyState';
import StatusBadge from '@/components/StatusBadge';
import SeverityBadge from '@/components/SeverityBadge';

export default function ReviewDetailPage() {
  const { id } = useParams<{ id: string }>();
  const reviewId = Number(id);
  const { data, loading, error } = useApi(
    () => getReviewDetail(reviewId),
    [reviewId],
  );

  if (loading) return <LoadingSpinner />;
  if (error) return <div style={{ color: 'var(--accent-red)' }}>오류: {error}</div>;
  if (!data) return null;

  const { reviewRun, comments } = data;

  return (
    <div>
      <div className="flex items-center gap-3 mb-6">
        <Link to="/" className="p-1" style={{ color: 'var(--text-secondary)' }}>
          <ArrowLeft className="w-5 h-5" />
        </Link>
        <h1 className="text-xl font-bold">리뷰 상세</h1>
      </div>

      {/* 리뷰 정보 카드 */}
      <div
        className="rounded-xl p-5 mb-6"
        style={{ backgroundColor: 'var(--bg-secondary)', border: '1px solid var(--border-primary)' }}
      >
        <div className="flex flex-wrap items-center gap-3 mb-3">
          <StatusBadge status={reviewRun.status} />
          <span className="text-sm" style={{ color: 'var(--text-secondary)' }}>
            모델: {reviewRun.model}
          </span>
          {reviewRun.totalTokens && (
            <span className="text-sm" style={{ color: 'var(--text-secondary)' }}>
              토큰: {reviewRun.totalTokens.toLocaleString()}
            </span>
          )}
        </div>
        <div className="flex items-center gap-2 text-xs" style={{ color: 'var(--text-muted)' }}>
          <Clock className="w-3 h-3" />
          <span>시작: {reviewRun.startedAt ? new Date(reviewRun.startedAt).toLocaleString('ko-KR') : '-'}</span>
          <span>|</span>
          <span>완료: {reviewRun.completedAt ? new Date(reviewRun.completedAt).toLocaleString('ko-KR') : '-'}</span>
        </div>
        {reviewRun.errorMessage && (
          <p className="mt-3 text-sm p-3 rounded-lg" style={{ backgroundColor: 'rgba(248,81,73,0.1)', color: 'var(--accent-red)' }}>
            {reviewRun.errorMessage}
          </p>
        )}
      </div>

      {/* 코멘트 목록 */}
      <h2 className="text-lg font-semibold mb-4">코멘트 ({comments.length})</h2>

      {!comments.length ? (
        <EmptyState message="코멘트가 없습니다" />
      ) : (
        <div className="space-y-4">
          {comments.map((comment) => (
            <div
              key={comment.id}
              className="rounded-xl overflow-hidden"
              style={{ backgroundColor: 'var(--bg-secondary)', border: '1px solid var(--border-primary)' }}
            >
              <div className="px-4 py-3 flex items-center justify-between" style={{ borderBottom: '1px solid var(--border-primary)' }}>
                <div className="flex items-center gap-2">
                  <FileCode className="w-4 h-4" style={{ color: 'var(--text-muted)' }} />
                  <span className="text-sm font-mono" style={{ color: 'var(--accent-blue)' }}>
                    {comment.filePath}
                  </span>
                  <span className="text-xs" style={{ color: 'var(--text-muted)' }}>
                    L{comment.startLine}{comment.endLine !== comment.startLine ? `-${comment.endLine}` : ''}
                  </span>
                </div>
                <div className="flex items-center gap-2">
                  <SeverityBadge severity={comment.severity} />
                  <span className="text-xs px-2 py-0.5 rounded-full" style={{ backgroundColor: 'var(--bg-tertiary)', color: 'var(--text-secondary)' }}>
                    {comment.category}
                  </span>
                </div>
              </div>
              <div className="p-4 text-sm leading-relaxed whitespace-pre-wrap" style={{ color: 'var(--text-primary)' }}>
                {comment.body}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
