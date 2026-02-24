export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string | null;
}

export interface ErrorResponse {
  status: number;
  error: string;
  message: string;
  fieldErrors: Record<string, string> | null;
  timestamp: string;
}

export interface UserResponse {
  githubId: number;
  login: string;
  email: string | null;
  avatarUrl: string;
}

export interface GitHubRepoResponse {
  id: number;
  fullName: string;
  name: string;
  private: boolean;
  description: string | null;
  language: string | null;
  htmlUrl: string;
}

export interface SubscriptionResponse {
  id: number;
  repoFullName: string;
  repoId: number;
  isActive: boolean;
  reviewLanguage: string;
  createdAt: string;
}

export interface SubscriptionRequest {
  repoId: number;
  repoFullName: string;
  reviewLanguage?: string;
}

export interface PullRequestResponse {
  id: number;
  githubPrId: number;
  prNumber: number;
  title: string;
  author: string;
  state: string;
  headSha: string;
  baseBranch: string;
  headBranch: string;
  openedAt: string;
  createdAt: string;
}

export interface ReviewRunResponse {
  id: number;
  snapshotId: number;
  status: string;
  model: string;
  totalTokens: number | null;
  startedAt: string | null;
  completedAt: string | null;
  errorMessage: string | null;
  createdAt: string;
}

export interface ReviewCommentResponse {
  id: number;
  filePath: string;
  startLine: number;
  endLine: number;
  severity: string;
  category: string;
  body: string;
}

export interface ReviewDetailResponse {
  reviewRun: ReviewRunResponse;
  comments: ReviewCommentResponse[];
}

export interface DailyReportResponse {
  id: number;
  subscriptionId: number;
  reportDate: string;
  summary: string;
  totalPrs: number;
  totalComments: number;
  criticalCount: number;
  warningCount: number;
  infoCount: number;
  createdAt: string;
}

export interface DailyReportDiffResponse {
  id: number;
  pullRequestId: number;
  reviewRunId: number | null;
  commentCount: number;
  criticalCount: number;
  warningCount: number;
}

export interface DailyReportDetailResponse {
  report: DailyReportResponse;
  diffs: DailyReportDiffResponse[];
}
