import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Plus, GitBranch, Trash2, Search, Lock, Globe } from 'lucide-react';
import { useApi } from '@/hooks/useApi';
import { getSubscriptions, getRepos, createSubscription, deleteSubscription } from '@/api';
import type { GitHubRepoResponse, SubscriptionRequest } from '@/types';
import LoadingSpinner from '@/components/LoadingSpinner';
import EmptyState from '@/components/EmptyState';
import Modal from '@/components/Modal';

export default function DashboardPage() {
  const { data: subscriptions, loading, error, refetch } = useApi(() => getSubscriptions());
  const [modalOpen, setModalOpen] = useState(false);

  if (loading) return <LoadingSpinner />;
  if (error) return <div style={{ color: 'var(--accent-red)' }}>오류: {error}</div>;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-xl font-bold">구독 저장소</h1>
        <button
          onClick={() => setModalOpen(true)}
          className="flex items-center gap-1.5 px-3 py-2 rounded-lg text-sm font-medium cursor-pointer"
          style={{
            backgroundColor: 'var(--accent-blue)',
            color: 'var(--bg-primary)',
            border: 'none',
          }}
        >
          <Plus className="w-4 h-4" />
          저장소 추가
        </button>
      </div>

      {!subscriptions?.length ? (
        <EmptyState message="구독 중인 저장소가 없습니다" description="저장소를 추가하여 PR 자동 리뷰를 시작하세요" />
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {subscriptions.map((sub) => (
            <div
              key={sub.id}
              className="rounded-xl p-5 group"
              style={{ backgroundColor: 'var(--bg-secondary)', border: '1px solid var(--border-primary)' }}
            >
              <div className="flex items-start justify-between mb-3">
                <Link
                  to={`/subscriptions/${sub.id}/pulls`}
                  className="flex items-center gap-2 no-underline hover:underline font-semibold"
                  style={{ color: 'var(--accent-blue)' }}
                >
                  <GitBranch className="w-4 h-4 shrink-0" />
                  <span className="truncate">{sub.repoFullName}</span>
                </Link>
                <button
                  onClick={async () => {
                    try {
                      await deleteSubscription(sub.id);
                      refetch();
                    } catch {
                      // 에러 무시
                    }
                  }}
                  className="p-1.5 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer"
                  style={{ color: 'var(--accent-red)', background: 'none', border: 'none' }}
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
              <div className="flex items-center gap-3 text-xs" style={{ color: 'var(--text-muted)' }}>
                <span>리뷰 언어: {sub.reviewLanguage}</span>
                <span>{new Date(sub.createdAt).toLocaleDateString('ko-KR')}</span>
              </div>
              <div className="mt-3 flex gap-2">
                <Link
                  to={`/subscriptions/${sub.id}/pulls`}
                  className="text-xs px-2 py-1 rounded no-underline"
                  style={{ backgroundColor: 'var(--bg-tertiary)', color: 'var(--text-secondary)' }}
                >
                  PR 목록
                </Link>
                <Link
                  to={`/subscriptions/${sub.id}/reports`}
                  className="text-xs px-2 py-1 rounded no-underline"
                  style={{ backgroundColor: 'var(--bg-tertiary)', color: 'var(--text-secondary)' }}
                >
                  리포트
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}

      <AddRepoModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        onAdded={() => { setModalOpen(false); refetch(); }}
        existingRepoIds={subscriptions?.map((s) => s.repoId) || []}
      />
    </div>
  );
}

function AddRepoModal({
  open, onClose, onAdded, existingRepoIds,
}: {
  open: boolean;
  onClose: () => void;
  onAdded: () => void;
  existingRepoIds: number[];
}) {
  const { data: repos, loading, error: reposError } = useApi(() => open ? getRepos() : Promise.resolve([]), [open]);
  const [search, setSearch] = useState('');
  const [adding, setAdding] = useState<number | null>(null);

  const filtered = repos?.filter(
    (r: GitHubRepoResponse) =>
      !existingRepoIds.includes(r.id) &&
      (r.fullName ?? '').toLowerCase().includes(search.toLowerCase()),
  ) || [];

  const handleAdd = async (repo: GitHubRepoResponse) => {
    try {
      setAdding(repo.id);
      const request: SubscriptionRequest = {
        repoId: repo.id,
        repoFullName: repo.fullName,
      };
      await createSubscription(request);
      onAdded();
    } catch {
      // 에러는 무시하고 UI 유지
    } finally {
      setAdding(null);
    }
  };

  return (
    <Modal open={open} onClose={onClose} title="저장소 추가">
      <div
        className="flex items-center gap-2 px-3 py-2 rounded-lg mb-4"
        style={{ backgroundColor: 'var(--bg-tertiary)', border: '1px solid var(--border-primary)' }}
      >
        <Search className="w-4 h-4" style={{ color: 'var(--text-muted)' }} />
        <input
          type="text"
          placeholder="저장소 검색..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="bg-transparent border-none outline-none flex-1 text-sm"
          style={{ color: 'var(--text-primary)' }}
        />
      </div>

      {loading ? (
        <LoadingSpinner />
      ) : reposError ? (
        <p className="text-center py-4 text-sm" style={{ color: 'var(--accent-red)' }}>
          저장소 목록을 불러올 수 없습니다: {reposError}
        </p>
      ) : !filtered.length ? (
        <p className="text-center py-4 text-sm" style={{ color: 'var(--text-muted)' }}>
          추가 가능한 저장소가 없습니다
        </p>
      ) : (
        <div className="space-y-2 max-h-80 overflow-y-auto">
          {filtered.map((repo: GitHubRepoResponse) => (
            <div
              key={repo.id}
              className="flex items-center justify-between p-3 rounded-lg"
              style={{ backgroundColor: 'var(--bg-tertiary)' }}
            >
              <div className="flex items-center gap-2 min-w-0">
                {repo.private ? (
                  <Lock className="w-4 h-4 shrink-0" style={{ color: 'var(--accent-yellow)' }} />
                ) : (
                  <Globe className="w-4 h-4 shrink-0" style={{ color: 'var(--text-muted)' }} />
                )}
                <div className="min-w-0">
                  <p className="text-sm font-medium truncate">{repo.fullName}</p>
                  {repo.language && (
                    <p className="text-xs" style={{ color: 'var(--text-muted)' }}>{repo.language}</p>
                  )}
                </div>
              </div>
              <button
                onClick={() => handleAdd(repo)}
                disabled={adding === repo.id}
                className="px-3 py-1 rounded-lg text-xs font-medium shrink-0 cursor-pointer"
                style={{
                  backgroundColor: 'var(--accent-blue)',
                  color: 'var(--bg-primary)',
                  border: 'none',
                  opacity: adding === repo.id ? 0.5 : 1,
                }}
              >
                {adding === repo.id ? '추가 중...' : '추가'}
              </button>
            </div>
          ))}
        </div>
      )}
    </Modal>
  );
}
