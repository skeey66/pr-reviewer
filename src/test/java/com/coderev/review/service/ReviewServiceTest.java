package com.coderev.review.service;

import com.coderev.auth.entity.User;
import com.coderev.common.exception.ResourceNotFoundException;
import com.coderev.pr.entity.PrSnapshot;
import com.coderev.pr.entity.PullRequest;
import com.coderev.review.dto.OpenAiReviewResponse;
import com.coderev.review.dto.ReviewDetailResponse;
import com.coderev.review.dto.ReviewRunResponse;
import com.coderev.review.entity.ReviewRun;
import com.coderev.subscription.entity.RepoSubscription;
import com.coderev.support.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ReviewServiceTest extends IntegrationTestBase {

  @Autowired
  private ReviewService reviewService;

  @MockitoBean
  private OpenAiClient openAiClient;

  @Test
  @DisplayName("리뷰 트리거 성공")
  void triggerReview_success() {
    User user = createUser("testuser", 1L);
    RepoSubscription sub = createSubscription(user, "owner/repo");
    PullRequest pr = createPullRequest(sub, "abc123");
    PrSnapshot snapshot = createSnapshot(pr, "abc123");

    OpenAiReviewResponse reviewResponse = createReviewResponse(List.of(
      createCommentItem("src/Test.java", "CRITICAL", "bug", "심각한 버그입니다")
    ));
    when(openAiClient.review(anyString(), anyString()))
      .thenReturn(new OpenAiClient.ReviewResult(reviewResponse, 100, 200, 300));

    reviewService.triggerReview(snapshot);

    List<ReviewRun> runs = reviewRunRepository.findBySnapshotPullRequestId(pr.getId());
    assertThat(runs).hasSize(1);
    assertThat(runs.get(0).getStatus()).isEqualTo("COMPLETED");
    assertThat(runs.get(0).getTotalTokens()).isEqualTo(300);

    assertThat(reviewCommentRepository.findByReviewRunId(runs.get(0).getId())).hasSize(1);
  }

  @Test
  @DisplayName("리뷰 실패 시 FAILED 상태로 전환")
  void triggerReview_failure_statusFailed() {
    User user = createUser("testuser", 1L);
    RepoSubscription sub = createSubscription(user, "owner/repo");
    PullRequest pr = createPullRequest(sub, "abc123");
    PrSnapshot snapshot = createSnapshot(pr, "abc123");

    when(openAiClient.review(anyString(), anyString()))
      .thenThrow(new RuntimeException("OpenAI API 오류"));

    reviewService.triggerReview(snapshot);

    List<ReviewRun> runs = reviewRunRepository.findBySnapshotPullRequestId(pr.getId());
    assertThat(runs).hasSize(1);
    assertThat(runs.get(0).getStatus()).isEqualTo("FAILED");
    assertThat(runs.get(0).getErrorMessage()).contains("OpenAI API 오류");
  }

  @Test
  @DisplayName("PR별 리뷰 이력 조회")
  void getReviewsByPullRequest() {
    User user = createUser("testuser", 1L);
    RepoSubscription sub = createSubscription(user, "owner/repo");
    PullRequest pr = createPullRequest(sub, "abc123");
    PrSnapshot snapshot = createSnapshot(pr, "abc123");
    createReviewRun(snapshot, "COMPLETED");
    createReviewRun(snapshot, "FAILED");

    List<ReviewRunResponse> reviews = reviewService.getReviewsByPullRequest(pr.getId());

    assertThat(reviews).hasSize(2);
  }

  @Test
  @DisplayName("리뷰 상세 조회 - 코멘트 포함")
  void getReviewDetail_withComments() {
    User user = createUser("testuser", 1L);
    RepoSubscription sub = createSubscription(user, "owner/repo");
    PullRequest pr = createPullRequest(sub, "abc123");
    PrSnapshot snapshot = createSnapshot(pr, "abc123");
    ReviewRun run = createReviewRun(snapshot, "COMPLETED");
    createReviewComment(run, "CRITICAL");
    createReviewComment(run, "WARNING");

    ReviewDetailResponse detail = reviewService.getReviewDetail(run.getId());

    assertThat(detail.getReviewRun().getStatus()).isEqualTo("COMPLETED");
    assertThat(detail.getComments()).hasSize(2);
  }

  @Test
  @DisplayName("존재하지 않는 리뷰 상세 조회 시 예외")
  void getReviewDetail_notFound() {
    assertThatThrownBy(() -> reviewService.getReviewDetail(999L))
      .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("수동 리뷰 트리거 - 스냅샷 없을 때 예외")
  void triggerManualReview_noSnapshot_throwsException() {
    assertThatThrownBy(() -> reviewService.triggerManualReview(999L))
      .isInstanceOf(ResourceNotFoundException.class)
      .hasMessageContaining("스냅샷을 찾을 수 없습니다");
  }

  @Test
  @DisplayName("수동 리뷰 트리거 성공")
  void triggerManualReview_success() {
    User user = createUser("testuser", 1L);
    RepoSubscription sub = createSubscription(user, "owner/repo");
    PullRequest pr = createPullRequest(sub, "abc123");
    createSnapshot(pr, "abc123");

    OpenAiReviewResponse reviewResponse = createReviewResponse(List.of());
    when(openAiClient.review(anyString(), anyString()))
      .thenReturn(new OpenAiClient.ReviewResult(reviewResponse, 50, 100, 150));

    ReviewRunResponse response = reviewService.triggerManualReview(pr.getId());

    assertThat(response.getStatus()).isEqualTo("COMPLETED");
    assertThat(response.getTotalTokens()).isEqualTo(150);
  }

  private OpenAiReviewResponse createReviewResponse(List<OpenAiReviewResponse.ReviewCommentItem> comments) {
    OpenAiReviewResponse response = new OpenAiReviewResponse();
    ReflectionTestUtils.setField(response, "comments", comments);
    return response;
  }

  private OpenAiReviewResponse.ReviewCommentItem createCommentItem(
    String filePath, String severity, String category, String body) {
    OpenAiReviewResponse.ReviewCommentItem item = new OpenAiReviewResponse.ReviewCommentItem();
    ReflectionTestUtils.setField(item, "filePath", filePath);
    ReflectionTestUtils.setField(item, "startLine", 1);
    ReflectionTestUtils.setField(item, "endLine", 5);
    ReflectionTestUtils.setField(item, "severity", severity);
    ReflectionTestUtils.setField(item, "category", category);
    ReflectionTestUtils.setField(item, "body", body);
    return item;
  }
}
