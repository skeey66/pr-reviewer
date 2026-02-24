import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ArrowLeft, Calendar, ChevronRight, AlertTriangle, AlertCircle, Info } from 'lucide-react';
import { useApi } from '@/hooks/useApi';
import { getReports, getReportDetail } from '@/api';
import type { DailyReportResponse, DailyReportDetailResponse } from '@/types';
import LoadingSpinner from '@/components/LoadingSpinner';
import EmptyState from '@/components/EmptyState';

export default function ReportsPage() {
  const { id } = useParams<{ id: string }>();
  const subscriptionId = Number(id);
  const { data: reports, loading, error } = useApi(
    () => getReports(subscriptionId),
    [subscriptionId],
  );
  const [selectedReport, setSelectedReport] = useState<DailyReportDetailResponse | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);

  const handleSelectReport = async (report: DailyReportResponse) => {
    setDetailLoading(true);
    const detail = await getReportDetail(report.id);
    setSelectedReport(detail);
    setDetailLoading(false);
  };

  if (loading) return <LoadingSpinner />;
  if (error) return <div style={{ color: 'var(--accent-red)' }}>오류: {error}</div>;

  return (
    <div>
      <div className="flex items-center gap-3 mb-6">
        <Link to="/" className="p-1" style={{ color: 'var(--text-secondary)' }}>
          <ArrowLeft className="w-5 h-5" />
        </Link>
        <h1 className="text-xl font-bold">일일 리포트</h1>
      </div>

      {!reports?.length ? (
        <EmptyState message="리포트가 없습니다" description="리뷰가 실행되면 일일 리포트가 자동으로 생성됩니다" />
      ) : (
        <div className="grid gap-6 lg:grid-cols-[1fr,1.5fr]">
          {/* 리포트 목록 */}
          <div className="space-y-2">
            {reports.map((report) => (
              <button
                key={report.id}
                onClick={() => handleSelectReport(report)}
                className="w-full text-left p-4 rounded-xl cursor-pointer"
                style={{
                  backgroundColor: selectedReport?.report.id === report.id ? 'var(--bg-tertiary)' : 'var(--bg-secondary)',
                  border: `1px solid ${selectedReport?.report.id === report.id ? 'var(--accent-blue)' : 'var(--border-primary)'}`,
                }}
              >
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center gap-2">
                    <Calendar className="w-4 h-4" style={{ color: 'var(--text-muted)' }} />
                    <span className="font-medium">{report.reportDate}</span>
                  </div>
                  <ChevronRight className="w-4 h-4" style={{ color: 'var(--text-muted)' }} />
                </div>
                <div className="flex items-center gap-3 text-xs" style={{ color: 'var(--text-muted)' }}>
                  <span>PR {report.totalPrs}개</span>
                  <span className="flex items-center gap-0.5" style={{ color: 'var(--accent-red)' }}>
                    <AlertTriangle className="w-3 h-3" /> {report.criticalCount}
                  </span>
                  <span className="flex items-center gap-0.5" style={{ color: 'var(--accent-yellow)' }}>
                    <AlertCircle className="w-3 h-3" /> {report.warningCount}
                  </span>
                  <span className="flex items-center gap-0.5" style={{ color: 'var(--accent-blue)' }}>
                    <Info className="w-3 h-3" /> {report.infoCount}
                  </span>
                </div>
              </button>
            ))}
          </div>

          {/* 리포트 상세 */}
          <div>
            {detailLoading ? (
              <LoadingSpinner />
            ) : !selectedReport ? (
              <div
                className="rounded-xl p-8 text-center"
                style={{ backgroundColor: 'var(--bg-secondary)', border: '1px solid var(--border-primary)' }}
              >
                <p style={{ color: 'var(--text-muted)' }}>리포트를 선택하세요</p>
              </div>
            ) : (
              <div
                className="rounded-xl p-5"
                style={{ backgroundColor: 'var(--bg-secondary)', border: '1px solid var(--border-primary)' }}
              >
                <h2 className="text-lg font-semibold mb-3">{selectedReport.report.reportDate} 리포트</h2>
                <p className="text-sm mb-4 leading-relaxed" style={{ color: 'var(--text-secondary)' }}>
                  {selectedReport.report.summary}
                </p>

                <div className="grid grid-cols-3 gap-3 mb-4">
                  <StatCard label="Critical" value={selectedReport.report.criticalCount} color="var(--accent-red)" />
                  <StatCard label="Warning" value={selectedReport.report.warningCount} color="var(--accent-yellow)" />
                  <StatCard label="Info" value={selectedReport.report.infoCount} color="var(--accent-blue)" />
                </div>

                {selectedReport.diffs.length > 0 && (
                  <>
                    <h3 className="text-sm font-semibold mb-2" style={{ color: 'var(--text-secondary)' }}>PR별 상세</h3>
                    <div className="space-y-2">
                      {selectedReport.diffs.map((diff) => (
                        <div
                          key={diff.id}
                          className="flex items-center justify-between p-3 rounded-lg"
                          style={{ backgroundColor: 'var(--bg-tertiary)' }}
                        >
                          <span className="text-sm">PR #{diff.pullRequestId}</span>
                          <div className="flex items-center gap-2 text-xs">
                            <span style={{ color: 'var(--accent-red)' }}>{diff.criticalCount}C</span>
                            <span style={{ color: 'var(--accent-yellow)' }}>{diff.warningCount}W</span>
                            <span style={{ color: 'var(--text-muted)' }}>{diff.commentCount} comments</span>
                          </div>
                        </div>
                      ))}
                    </div>
                  </>
                )}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

function StatCard({ label, value, color }: { label: string; value: number; color: string }) {
  return (
    <div className="text-center p-3 rounded-lg" style={{ backgroundColor: 'var(--bg-tertiary)' }}>
      <p className="text-2xl font-bold" style={{ color }}>{value}</p>
      <p className="text-xs" style={{ color: 'var(--text-muted)' }}>{label}</p>
    </div>
  );
}
