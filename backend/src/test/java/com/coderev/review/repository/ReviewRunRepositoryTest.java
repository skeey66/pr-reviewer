package com.coderev.review.repository;

import com.coderev.auth.entity.User;
import com.coderev.pr.entity.PrSnapshot;
import com.coderev.pr.entity.PullRequest;
import com.coderev.review.entity.ReviewRun;
import com.coderev.subscription.entity.RepoSubscription;
import com.coderev.support.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewRunRepositoryTest extends IntegrationTestBase {

  @Test
  @DisplayName("findBySnapshotPullRequestId - PR별 리뷰 조회")
  void findBySnapshotPullRequestId() {
    User user = createUser("testuser", 1L);
    RepoSubscription sub = createSubscription(user, "owner/repo");
    PullRequest pr = createPullRequest(sub, "abc123");
    PrSnapshot snapshot = createSnapshot(pr, "abc123");
    createReviewRun(snapshot, "COMPLETED");
    createReviewRun(snapshot, "FAILED");

    List<ReviewRun> result = reviewRunRepository.findBySnapshotPullRequestId(pr.getId());

    assertThat(result).hasSize(2);
  }

  @Test
  @DisplayName("findByStatusAndCompletedAtBetween - 기간별 완료 리뷰 조회")
  void findByStatusAndCompletedAtBetween() {
    User user = createUser("testuser", 1L);
    RepoSubscription sub = createSubscription(user, "owner/repo");
    PullRequest pr = createPullRequest(sub, "abc123");
    PrSnapshot snapshot = createSnapshot(pr, "abc123");
    createReviewRun(snapshot, "COMPLETED");
    createReviewRun(snapshot, "FAILED");

    LocalDateTime start = LocalDateTime.now().minusHours(1);
    LocalDateTime end = LocalDateTime.now().plusHours(1);

    List<ReviewRun> result = reviewRunRepository.findByStatusAndCompletedAtBetween("COMPLETED", start, end);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getStatus()).isEqualTo("COMPLETED");
  }

  @Test
  @DisplayName("findByStatusAndCompletedAtBetween - 범위 밖이면 빈 결과")
  void findByStatusAndCompletedAtBetween_outOfRange() {
    User user = createUser("testuser", 1L);
    RepoSubscription sub = createSubscription(user, "owner/repo");
    PullRequest pr = createPullRequest(sub, "abc123");
    PrSnapshot snapshot = createSnapshot(pr, "abc123");
    createReviewRun(snapshot, "COMPLETED");

    LocalDateTime start = LocalDateTime.now().plusDays(1);
    LocalDateTime end = LocalDateTime.now().plusDays(2);

    List<ReviewRun> result = reviewRunRepository.findByStatusAndCompletedAtBetween("COMPLETED", start, end);

    assertThat(result).isEmpty();
  }
}
