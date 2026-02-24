package com.coderev.pr.service;

import com.coderev.github.dto.GitHubPullRequestResponse;
import com.coderev.github.service.GitHubApiClient;
import com.coderev.pr.entity.PrSnapshot;
import com.coderev.pr.entity.PullRequest;
import com.coderev.pr.repository.PrSnapshotRepository;
import com.coderev.pr.repository.PullRequestRepository;
import com.coderev.review.service.ReviewService;
import com.coderev.subscription.entity.RepoSubscription;
import com.coderev.subscription.repository.RepoSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrPollingService {

  private final RepoSubscriptionRepository repoSubscriptionRepository;
  private final PullRequestRepository pullRequestRepository;
  private final PrSnapshotRepository prSnapshotRepository;
  private final GitHubApiClient gitHubApiClient;
  private final ReviewService reviewService;

  @Scheduled(cron = "${app.scheduler.pr-poll-cron}")
  public void pollOpenPullRequests() {
    log.info("PR 폴링 시작");
    List<RepoSubscription> activeSubscriptions = repoSubscriptionRepository.findByIsActiveTrue();

    for (RepoSubscription subscription : activeSubscriptions) {
      try {
        processSubscription(subscription);
      } catch (Exception e) {
        log.error("구독 처리 실패: subscriptionId={}, repo={}", subscription.getId(), subscription.getRepoFullName(), e);
      }
    }
    log.info("PR 폴링 완료: 구독 {}개 처리", activeSubscriptions.size());
  }

  @Transactional
  public void processSubscription(RepoSubscription subscription) {
    String token = gitHubApiClient.getDecryptedToken(subscription.getUser().getId());
    List<GitHubPullRequestResponse> openPrs = gitHubApiClient.getOpenPullRequests(
      subscription.getRepoFullName(), token);

    for (GitHubPullRequestResponse prResponse : openPrs) {
      try {
        processPullRequest(subscription, prResponse, token);
      } catch (Exception e) {
        log.error("PR 처리 실패: repo={}, prNumber={}", subscription.getRepoFullName(), prResponse.getNumber(), e);
      }
    }
  }

  private void processPullRequest(RepoSubscription subscription, GitHubPullRequestResponse prResponse, String token) {
    String newHeadSha = prResponse.getHead().getSha();

    // PR upsert
    PullRequest pullRequest = pullRequestRepository
      .findBySubscriptionIdAndGithubPrId(subscription.getId(), prResponse.getId())
      .map(existing -> {
        existing.updateState(prResponse.getState(), newHeadSha);
        return existing;
      })
      .orElseGet(() -> pullRequestRepository.save(PullRequest.builder()
        .subscription(subscription)
        .githubPrId(prResponse.getId())
        .prNumber(prResponse.getNumber())
        .title(prResponse.getTitle())
        .author(prResponse.getUser().getLogin())
        .state(prResponse.getState())
        .headSha(newHeadSha)
        .baseBranch(prResponse.getBase().getRef())
        .headBranch(prResponse.getHead().getRef())
        .openedAt(prResponse.getCreatedAt().atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime())
        .build()));

    // headSha 변경 시 스냅샷 생성
    if (!prSnapshotRepository.existsByPullRequestIdAndHeadSha(pullRequest.getId(), newHeadSha)) {
      String diff = gitHubApiClient.getPullRequestDiff(
        subscription.getRepoFullName(), pullRequest.getPrNumber(), token);

      int fileCount = countFiles(diff);
      int additions = countLines(diff, "+");
      int deletions = countLines(diff, "-");

      PrSnapshot snapshot = prSnapshotRepository.save(PrSnapshot.builder()
        .pullRequest(pullRequest)
        .headSha(newHeadSha)
        .diffText(diff)
        .fileCount(fileCount)
        .additions(additions)
        .deletions(deletions)
        .build());

      // 리뷰 트리거
      reviewService.triggerReview(snapshot);
    }
  }

  private int countFiles(String diff) {
    if (diff == null) return 0;
    return (int) diff.lines().filter(line -> line.startsWith("diff --git")).count();
  }

  private int countLines(String diff, String prefix) {
    if (diff == null) return 0;
    String fullPrefix = prefix.equals("+") ? "+" : "-";
    return (int) diff.lines()
      .filter(line -> line.startsWith(fullPrefix) && !line.startsWith(fullPrefix + fullPrefix + fullPrefix))
      .count();
  }
}
