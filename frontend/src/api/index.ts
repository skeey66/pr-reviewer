import client from './client';
import type {
  ApiResponse,
  UserResponse,
  GitHubRepoResponse,
  SubscriptionResponse,
  SubscriptionRequest,
  PullRequestResponse,
  ReviewRunResponse,
  ReviewDetailResponse,
  DailyReportResponse,
  DailyReportDetailResponse,
} from '@/types';

export const getMe = () =>
  client.get<ApiResponse<UserResponse>>('/api/me').then((r) => r.data.data);

export const getRepos = () =>
  client.get<ApiResponse<GitHubRepoResponse[]>>('/api/repos').then((r) => r.data.data);

export const getSubscriptions = () =>
  client.get<ApiResponse<SubscriptionResponse[]>>('/api/subscriptions').then((r) => r.data.data);

export const createSubscription = (request: SubscriptionRequest) =>
  client.post<ApiResponse<SubscriptionResponse>>('/api/subscriptions', request).then((r) => r.data.data);

export const deleteSubscription = (id: number) =>
  client.delete<ApiResponse<void>>(`/api/subscriptions/${id}`).then((r) => r.data);

export const getPullRequests = (subscriptionId: number) =>
  client.get<ApiResponse<PullRequestResponse[]>>(`/api/subscriptions/${subscriptionId}/pull-requests`).then((r) => r.data.data);

export const getReviews = (pullRequestId: number) =>
  client.get<ApiResponse<ReviewRunResponse[]>>(`/api/pull-requests/${pullRequestId}/reviews`).then((r) => r.data.data);

export const triggerReview = (pullRequestId: number) =>
  client.post<ApiResponse<ReviewRunResponse>>(`/api/pull-requests/${pullRequestId}/review`).then((r) => r.data.data);

export const getReviewDetail = (reviewRunId: number) =>
  client.get<ApiResponse<ReviewDetailResponse>>(`/api/reviews/${reviewRunId}`).then((r) => r.data.data);

export const getReports = (subscriptionId: number) =>
  client.get<ApiResponse<DailyReportResponse[]>>(`/api/subscriptions/${subscriptionId}/reports`).then((r) => r.data.data);

export const getReportDetail = (reportId: number) =>
  client.get<ApiResponse<DailyReportDetailResponse>>(`/api/reports/${reportId}`).then((r) => r.data.data);
