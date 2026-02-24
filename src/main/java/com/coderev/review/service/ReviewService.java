package com.coderev.review.service;

import com.coderev.common.exception.ResourceNotFoundException;
import com.coderev.pr.entity.PrSnapshot;
import com.coderev.pr.repository.PrSnapshotRepository;
import com.coderev.review.dto.*;
import com.coderev.review.entity.ReviewComment;
import com.coderev.review.entity.ReviewRun;
import com.coderev.review.repository.ReviewCommentRepository;
import com.coderev.review.repository.ReviewRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

  private final ReviewRunRepository reviewRunRepository;
  private final ReviewCommentRepository reviewCommentRepository;
  private final PrSnapshotRepository prSnapshotRepository;
  private final OpenAiClient openAiClient;

  @Value("${app.openai.model}")
  private String model;

  // 자동 리뷰 트리거 (폴링 시 호출)
  @Transactional
  public void triggerReview(PrSnapshot snapshot) {
    ReviewRun reviewRun = reviewRunRepository.save(ReviewRun.builder()
      .snapshot(snapshot)
      .model(model)
      .build());

    executeReview(reviewRun, snapshot);
  }

  // 수동 리뷰 트리거
  @Transactional
  public ReviewRunResponse triggerManualReview(Long pullRequestId) {
    PrSnapshot snapshot = prSnapshotRepository.findTopByPullRequestIdOrderByCreatedAtDesc(pullRequestId)
      .orElseThrow(() -> new ResourceNotFoundException("스냅샷을 찾을 수 없습니다. pullRequestId=" + pullRequestId));

    ReviewRun reviewRun = reviewRunRepository.save(ReviewRun.builder()
      .snapshot(snapshot)
      .model(model)
      .build());

    executeReview(reviewRun, snapshot);
    return ReviewRunResponse.from(reviewRun);
  }

  private void executeReview(ReviewRun reviewRun, PrSnapshot snapshot) {
    try {
      reviewRun.start();
      reviewRunRepository.save(reviewRun);

      // 구독의 리뷰 언어 조회
      String language = snapshot.getPullRequest().getSubscription().getReviewLanguage();

      OpenAiClient.ReviewResult result = openAiClient.review(snapshot.getDiffText(), language);

      // 코멘트 저장
      if (result.getResponse().getComments() != null) {
        for (OpenAiReviewResponse.ReviewCommentItem item : result.getResponse().getComments()) {
          reviewCommentRepository.save(ReviewComment.builder()
            .reviewRun(reviewRun)
            .filePath(item.getFilePath())
            .startLine(item.getStartLine())
            .endLine(item.getEndLine())
            .severity(item.getSeverity())
            .category(item.getCategory())
            .body(item.getBody())
            .build());
        }
      }

      reviewRun.complete(result.getPromptTokens(), result.getCompletionTokens(), result.getTotalTokens());
      reviewRunRepository.save(reviewRun);

      log.info("리뷰 완료: reviewRunId={}, tokens={}", reviewRun.getId(), result.getTotalTokens());
    } catch (Exception e) {
      log.error("리뷰 실패: reviewRunId={}", reviewRun.getId(), e);
      reviewRun.fail(e.getMessage());
      reviewRunRepository.save(reviewRun);
    }
  }

  @Transactional(readOnly = true)
  public List<ReviewRunResponse> getReviewsByPullRequest(Long pullRequestId) {
    return reviewRunRepository.findBySnapshotPullRequestId(pullRequestId).stream()
      .map(ReviewRunResponse::from)
      .toList();
  }

  @Transactional(readOnly = true)
  public ReviewDetailResponse getReviewDetail(Long reviewRunId) {
    ReviewRun reviewRun = reviewRunRepository.findById(reviewRunId)
      .orElseThrow(() -> new ResourceNotFoundException("리뷰를 찾을 수 없습니다. id=" + reviewRunId));

    List<ReviewCommentResponse> comments = reviewCommentRepository.findByReviewRunId(reviewRunId).stream()
      .map(ReviewCommentResponse::from)
      .toList();

    return ReviewDetailResponse.builder()
      .reviewRun(ReviewRunResponse.from(reviewRun))
      .comments(comments)
      .build();
  }
}
