package com.coderev.pr.service;

import com.coderev.auth.entity.User;
import com.coderev.github.dto.GitHubPullRequestResponse;
import com.coderev.github.service.GitHubApiClient;
import com.coderev.pr.entity.PrSnapshot;
import com.coderev.pr.entity.PullRequest;
import com.coderev.review.dto.OpenAiReviewResponse;
import com.coderev.review.service.OpenAiClient;
import com.coderev.subscription.entity.RepoSubscription;
import com.coderev.support.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class PrPollingServiceTest extends IntegrationTestBase {

  @Autowired
  private PrPollingService prPollingService;

  @MockitoBean
  private GitHubApiClient gitHubApiClient;

  @MockitoBean
  private OpenAiClient openAiClient;

  @Test
  @DisplayName("PR 신규 생성 및 스냅샷 생성")
  void processSubscription_newPr_createsSnapshotAndReview() {
    User user = createUser("testuser", 1L);
    RepoSubscription sub = createSubscription(user, "owner/repo");

    GitHubPullRequestResponse prResponse = createGitHubPrResponse(200L, 1, "Test PR", "abc123");

    when(gitHubApiClient.getDecryptedToken(user.getId())).thenReturn("fake-token");
    when(gitHubApiClient.getOpenPullRequests("owner/repo", "fake-token"))
      .thenReturn(List.of(prResponse));
    when(gitHubApiClient.getPullRequestDiff(eq("owner/repo"), eq(1), eq("fake-token")))
      .thenReturn("diff --git a/test.java b/test.java\n+new line");

    OpenAiReviewResponse reviewResponse = new OpenAiReviewResponse();
    ReflectionTestUtils.setField(reviewResponse, "comments", List.of());
    when(openAiClient.review(anyString(), anyString()))
      .thenReturn(new OpenAiClient.ReviewResult(reviewResponse, 10, 20, 30));

    prPollingService.processSubscription(sub);

    List<PullRequest> prs = pullRequestRepository.findBySubscriptionId(sub.getId());
    assertThat(prs).hasSize(1);
    assertThat(prs.get(0).getHeadSha()).isEqualTo("abc123");

    assertThat(prSnapshotRepository.existsByPullRequestIdAndHeadSha(prs.get(0).getId(), "abc123")).isTrue();
  }

  @Test
  @DisplayName("headSha 변경 시 새 스냅샷 생성")
  void processSubscription_headShaChanged_createsNewSnapshot() {
    User user = createUser("testuser", 1L);
    RepoSubscription sub = createSubscription(user, "owner/repo");
    PullRequest pr = createPullRequest(sub, "old-sha");
    createSnapshot(pr, "old-sha");

    GitHubPullRequestResponse prResponse = createGitHubPrResponse(100L, 1, "Test PR", "new-sha");

    when(gitHubApiClient.getDecryptedToken(user.getId())).thenReturn("fake-token");
    when(gitHubApiClient.getOpenPullRequests("owner/repo", "fake-token"))
      .thenReturn(List.of(prResponse));
    when(gitHubApiClient.getPullRequestDiff(eq("owner/repo"), eq(1), eq("fake-token")))
      .thenReturn("diff --git a/test.java b/test.java\n+updated line");

    OpenAiReviewResponse reviewResponse = new OpenAiReviewResponse();
    ReflectionTestUtils.setField(reviewResponse, "comments", List.of());
    when(openAiClient.review(anyString(), anyString()))
      .thenReturn(new OpenAiClient.ReviewResult(reviewResponse, 10, 20, 30));

    prPollingService.processSubscription(sub);

    assertThat(prSnapshotRepository.existsByPullRequestIdAndHeadSha(pr.getId(), "new-sha")).isTrue();
  }

  @Test
  @DisplayName("동일 headSha면 스냅샷 미생성")
  void processSubscription_sameHeadSha_noNewSnapshot() {
    User user = createUser("testuser", 1L);
    RepoSubscription sub = createSubscription(user, "owner/repo");
    PullRequest pr = createPullRequest(sub, "same-sha");
    createSnapshot(pr, "same-sha");

    GitHubPullRequestResponse prResponse = createGitHubPrResponse(100L, 1, "Test PR", "same-sha");

    when(gitHubApiClient.getDecryptedToken(user.getId())).thenReturn("fake-token");
    when(gitHubApiClient.getOpenPullRequests("owner/repo", "fake-token"))
      .thenReturn(List.of(prResponse));

    prPollingService.processSubscription(sub);

    List<PrSnapshot> snapshots = prSnapshotRepository.findAll();
    assertThat(snapshots).hasSize(1);
  }

  private GitHubPullRequestResponse createGitHubPrResponse(Long id, int number, String title, String headSha) {
    GitHubPullRequestResponse response = new GitHubPullRequestResponse();
    ReflectionTestUtils.setField(response, "id", id);
    ReflectionTestUtils.setField(response, "number", number);
    ReflectionTestUtils.setField(response, "title", title);
    ReflectionTestUtils.setField(response, "state", "open");
    ReflectionTestUtils.setField(response, "createdAt", OffsetDateTime.now());

    GitHubPullRequestResponse.GitHubUserResponse user = new GitHubPullRequestResponse.GitHubUserResponse();
    ReflectionTestUtils.setField(user, "login", "author");
    ReflectionTestUtils.setField(response, "user", user);

    GitHubPullRequestResponse.GitHubBranchRef head = new GitHubPullRequestResponse.GitHubBranchRef();
    ReflectionTestUtils.setField(head, "ref", "feature");
    ReflectionTestUtils.setField(head, "sha", headSha);
    ReflectionTestUtils.setField(response, "head", head);

    GitHubPullRequestResponse.GitHubBranchRef base = new GitHubPullRequestResponse.GitHubBranchRef();
    ReflectionTestUtils.setField(base, "ref", "main");
    ReflectionTestUtils.setField(base, "sha", "base-sha");
    ReflectionTestUtils.setField(response, "base", base);

    return response;
  }
}
