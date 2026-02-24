package com.coderev.support;

import com.coderev.auth.entity.User;
import com.coderev.auth.repository.UserRepository;
import com.coderev.pr.entity.PrSnapshot;
import com.coderev.pr.entity.PullRequest;
import com.coderev.pr.repository.PrSnapshotRepository;
import com.coderev.pr.repository.PullRequestRepository;
import com.coderev.report.repository.DailyReportDiffRepository;
import com.coderev.report.repository.DailyReportRepository;
import com.coderev.review.entity.ReviewComment;
import com.coderev.review.entity.ReviewRun;
import com.coderev.review.repository.ReviewCommentRepository;
import com.coderev.review.repository.ReviewRunRepository;
import com.coderev.subscription.entity.RepoSubscription;
import com.coderev.subscription.repository.RepoSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public abstract class IntegrationTestBase {

  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
    .withDatabaseName("pr_reviewer_test")
    .withUsername("test")
    .withPassword("test");

  @Autowired
  protected UserRepository userRepository;
  @Autowired
  protected RepoSubscriptionRepository repoSubscriptionRepository;
  @Autowired
  protected PullRequestRepository pullRequestRepository;
  @Autowired
  protected PrSnapshotRepository prSnapshotRepository;
  @Autowired
  protected ReviewRunRepository reviewRunRepository;
  @Autowired
  protected ReviewCommentRepository reviewCommentRepository;
  @Autowired
  protected DailyReportRepository dailyReportRepository;
  @Autowired
  protected DailyReportDiffRepository dailyReportDiffRepository;

  @BeforeEach
  void cleanUp() {
    dailyReportDiffRepository.deleteAll();
    dailyReportRepository.deleteAll();
    reviewCommentRepository.deleteAll();
    reviewRunRepository.deleteAll();
    prSnapshotRepository.deleteAll();
    pullRequestRepository.deleteAll();
    repoSubscriptionRepository.deleteAll();
    userRepository.deleteAll();
  }

  protected User createUser(String login, Long githubId) {
    return userRepository.save(User.builder()
      .githubId(githubId)
      .login(login)
      .email(login + "@test.com")
      .build());
  }

  protected RepoSubscription createSubscription(User user, String repoFullName) {
    return repoSubscriptionRepository.save(RepoSubscription.builder()
      .user(user)
      .repoId(1000L)
      .repoFullName(repoFullName)
      .reviewLanguage("ko")
      .build());
  }

  protected PullRequest createPullRequest(RepoSubscription subscription, String headSha) {
    return pullRequestRepository.save(PullRequest.builder()
      .subscription(subscription)
      .githubPrId(100L)
      .prNumber(1)
      .title("Test PR")
      .author("testuser")
      .state("open")
      .headSha(headSha)
      .baseBranch("main")
      .headBranch("feature")
      .openedAt(LocalDateTime.now())
      .build());
  }

  protected PrSnapshot createSnapshot(PullRequest pullRequest, String headSha) {
    return prSnapshotRepository.save(PrSnapshot.builder()
      .pullRequest(pullRequest)
      .headSha(headSha)
      .diffText("diff --git a/test.java b/test.java\n+added line\n-removed line")
      .fileCount(1)
      .additions(1)
      .deletions(1)
      .build());
  }

  protected ReviewRun createReviewRun(PrSnapshot snapshot, String status) {
    ReviewRun run = reviewRunRepository.save(ReviewRun.builder()
      .snapshot(snapshot)
      .model("gpt-4o")
      .build());

    if ("RUNNING".equals(status)) {
      run.start();
      return reviewRunRepository.save(run);
    }
    if ("COMPLETED".equals(status)) {
      run.start();
      run.complete(100, 200, 300);
      return reviewRunRepository.save(run);
    }
    if ("FAILED".equals(status)) {
      run.start();
      run.fail("테스트 에러");
      return reviewRunRepository.save(run);
    }
    return run;
  }

  protected ReviewComment createReviewComment(ReviewRun reviewRun, String severity) {
    return reviewCommentRepository.save(ReviewComment.builder()
      .reviewRun(reviewRun)
      .filePath("src/main/java/Test.java")
      .startLine(10)
      .endLine(15)
      .severity(severity)
      .category("bug")
      .body("테스트 코멘트: " + severity)
      .build());
  }
}
