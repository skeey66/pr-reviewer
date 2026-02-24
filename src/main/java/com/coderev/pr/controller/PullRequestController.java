package com.coderev.pr.controller;

import com.coderev.common.dto.ApiResponse;
import com.coderev.pr.dto.PullRequestResponse;
import com.coderev.pr.service.PullRequestService;
import com.coderev.review.dto.ReviewDetailResponse;
import com.coderev.review.dto.ReviewRunResponse;
import com.coderev.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Pull Request", description = "PR 및 리뷰 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PullRequestController {

  private final PullRequestService pullRequestService;
  private final ReviewService reviewService;

  @Operation(summary = "구독별 PR 목록 조회")
  @GetMapping("/subscriptions/{subscriptionId}/pull-requests")
  public ApiResponse<List<PullRequestResponse>> getPullRequests(@PathVariable Long subscriptionId) {
    return ApiResponse.ok(pullRequestService.getPullRequestsBySubscription(subscriptionId));
  }

  @Operation(summary = "PR별 리뷰 이력 조회")
  @GetMapping("/pull-requests/{pullRequestId}/reviews")
  public ApiResponse<List<ReviewRunResponse>> getReviews(@PathVariable Long pullRequestId) {
    return ApiResponse.ok(reviewService.getReviewsByPullRequest(pullRequestId));
  }

  @Operation(summary = "수동 리뷰 트리거")
  @PostMapping("/pull-requests/{pullRequestId}/review")
  public ApiResponse<ReviewRunResponse> triggerReview(@PathVariable Long pullRequestId) {
    return ApiResponse.ok(reviewService.triggerManualReview(pullRequestId));
  }

  @Operation(summary = "리뷰 상세 조회")
  @GetMapping("/reviews/{reviewRunId}")
  public ApiResponse<ReviewDetailResponse> getReviewDetail(@PathVariable Long reviewRunId) {
    return ApiResponse.ok(reviewService.getReviewDetail(reviewRunId));
  }
}
