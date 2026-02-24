package com.coderev.report.service;

import com.coderev.common.exception.ResourceNotFoundException;
import com.coderev.report.dto.DailyReportDetailResponse;
import com.coderev.report.dto.DailyReportDiffResponse;
import com.coderev.report.dto.DailyReportResponse;
import com.coderev.report.entity.DailyReport;
import com.coderev.report.entity.DailyReportDiff;
import com.coderev.report.repository.DailyReportDiffRepository;
import com.coderev.report.repository.DailyReportRepository;
import com.coderev.review.entity.ReviewRun;
import com.coderev.review.repository.ReviewCommentRepository;
import com.coderev.review.repository.ReviewRunRepository;
import com.coderev.subscription.entity.RepoSubscription;
import com.coderev.subscription.repository.RepoSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyReportService {

  private final DailyReportRepository dailyReportRepository;
  private final DailyReportDiffRepository dailyReportDiffRepository;
  private final RepoSubscriptionRepository repoSubscriptionRepository;
  private final ReviewRunRepository reviewRunRepository;
  private final ReviewCommentRepository reviewCommentRepository;

  @Scheduled(cron = "${app.scheduler.daily-report-cron}")
  public void generateDailyReports() {
    log.info("일일 리포트 생성 시작");
    LocalDate yesterday = LocalDate.now().minusDays(1);
    List<RepoSubscription> activeSubscriptions = repoSubscriptionRepository.findByIsActiveTrue();

    for (RepoSubscription subscription : activeSubscriptions) {
      try {
        generateReport(subscription, yesterday);
      } catch (Exception e) {
        log.error("리포트 생성 실패: subscriptionId={}", subscription.getId(), e);
      }
    }
    log.info("일일 리포트 생성 완료: 구독 {}개 처리", activeSubscriptions.size());
  }

  @Transactional
  public void generateReport(RepoSubscription subscription, LocalDate reportDate) {
    // 해당 날짜의 완료된 ReviewRun 조회
    LocalDateTime startOfDay = reportDate.atStartOfDay();
    LocalDateTime endOfDay = reportDate.atTime(LocalTime.MAX);

    List<ReviewRun> completedRuns = reviewRunRepository.findByStatusAndCompletedAtBetween(
      "COMPLETED", startOfDay, endOfDay);

    // 해당 구독의 리뷰만 필터링
    List<ReviewRun> subscriptionRuns = completedRuns.stream()
      .filter(run -> run.getSnapshot().getPullRequest().getSubscription().getId().equals(subscription.getId()))
      .toList();

    if (subscriptionRuns.isEmpty()) return;

    // PR별 그룹핑
    Map<Long, List<ReviewRun>> runsByPr = subscriptionRuns.stream()
      .collect(Collectors.groupingBy(run -> run.getSnapshot().getPullRequest().getId()));

    int totalComments = 0;
    int totalCritical = 0;
    int totalWarning = 0;
    int totalInfo = 0;

    DailyReport report = dailyReportRepository.save(DailyReport.builder()
      .subscription(subscription)
      .reportDate(reportDate)
      .totalPrs(runsByPr.size())
      .build());

    // PR별 diff 생성
    for (Map.Entry<Long, List<ReviewRun>> entry : runsByPr.entrySet()) {
      List<ReviewRun> prRuns = entry.getValue();
      ReviewRun latestRun = prRuns.get(prRuns.size() - 1);

      int critical = (int) reviewCommentRepository.countByReviewRunIdAndSeverity(latestRun.getId(), "CRITICAL");
      int warning = (int) reviewCommentRepository.countByReviewRunIdAndSeverity(latestRun.getId(), "WARNING");
      int commentCount = critical + warning +
        (int) reviewCommentRepository.countByReviewRunIdAndSeverity(latestRun.getId(), "INFO");

      dailyReportDiffRepository.save(DailyReportDiff.builder()
        .dailyReport(report)
        .pullRequest(latestRun.getSnapshot().getPullRequest())
        .reviewRun(latestRun)
        .commentCount(commentCount)
        .criticalCount(critical)
        .warningCount(warning)
        .build());

      totalComments += commentCount;
      totalCritical += critical;
      totalWarning += warning;
      totalInfo += commentCount - critical - warning;
    }

    // 집계 업데이트를 위한 리포트 재생성 (immutable 필드이므로 새로 저장)
    report = dailyReportRepository.save(DailyReport.builder()
      .id(report.getId())
      .subscription(subscription)
      .reportDate(reportDate)
      .summary(String.format("%s: PR %d개, 코멘트 %d개 (Critical: %d, Warning: %d, Info: %d)",
        subscription.getRepoFullName(), runsByPr.size(), totalComments, totalCritical, totalWarning, totalInfo))
      .totalPrs(runsByPr.size())
      .totalComments(totalComments)
      .criticalCount(totalCritical)
      .warningCount(totalWarning)
      .infoCount(totalInfo)
      .build());

    log.info("리포트 생성: subscriptionId={}, date={}, prs={}, comments={}",
      subscription.getId(), reportDate, runsByPr.size(), totalComments);
  }

  @Transactional(readOnly = true)
  public List<DailyReportResponse> getReportsBySubscription(Long subscriptionId) {
    return dailyReportRepository.findBySubscriptionIdOrderByReportDateDesc(subscriptionId).stream()
      .map(DailyReportResponse::from)
      .toList();
  }

  @Transactional(readOnly = true)
  public DailyReportDetailResponse getReportDetail(Long reportId) {
    DailyReport report = dailyReportRepository.findById(reportId)
      .orElseThrow(() -> new ResourceNotFoundException("리포트를 찾을 수 없습니다. id=" + reportId));

    List<DailyReportDiffResponse> diffs = dailyReportDiffRepository.findByDailyReportId(reportId).stream()
      .map(DailyReportDiffResponse::from)
      .toList();

    return DailyReportDetailResponse.builder()
      .report(DailyReportResponse.from(report))
      .diffs(diffs)
      .build();
  }
}
