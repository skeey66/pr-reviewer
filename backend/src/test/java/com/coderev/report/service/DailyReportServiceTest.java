package com.coderev.report.service;

import com.coderev.auth.entity.User;
import com.coderev.common.exception.ResourceNotFoundException;
import com.coderev.pr.entity.PrSnapshot;
import com.coderev.pr.entity.PullRequest;
import com.coderev.report.dto.DailyReportDetailResponse;
import com.coderev.report.dto.DailyReportResponse;
import com.coderev.report.entity.DailyReport;
import com.coderev.review.entity.ReviewRun;
import com.coderev.subscription.entity.RepoSubscription;
import com.coderev.support.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DailyReportServiceTest extends IntegrationTestBase {

  @Autowired
  private DailyReportService dailyReportService;

  @Test
  @DisplayName("리포트 생성 - 전날 완료된 리뷰 기준 집계")
  void generateReport_success() {
    User user = createUser("testuser", 1L);
    RepoSubscription sub = createSubscription(user, "owner/repo");
    PullRequest pr = createPullRequest(sub, "abc123");
    PrSnapshot snapshot = createSnapshot(pr, "abc123");
    ReviewRun run = createReviewRun(snapshot, "COMPLETED");
    createReviewComment(run, "CRITICAL");
    createReviewComment(run, "WARNING");
    createReviewComment(run, "INFO");

    LocalDate today = LocalDate.now();
    dailyReportService.generateReport(sub, today);

    List<DailyReport> reports = dailyReportRepository.findAll();
    assertThat(reports).hasSize(1);
    assertThat(reports.get(0).getTotalPrs()).isEqualTo(1);
    assertThat(reports.get(0).getTotalComments()).isEqualTo(3);
    assertThat(reports.get(0).getCriticalCount()).isEqualTo(1);
    assertThat(reports.get(0).getWarningCount()).isEqualTo(1);
    assertThat(reports.get(0).getInfoCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("리뷰 없으면 리포트 미생성")
  void generateReport_noReviews_noReport() {
    User user = createUser("testuser", 1L);
    RepoSubscription sub = createSubscription(user, "owner/repo");

    dailyReportService.generateReport(sub, LocalDate.now());

    assertThat(dailyReportRepository.findAll()).isEmpty();
  }

  @Test
  @DisplayName("리포트 목록 조회")
  void getReportsBySubscription() {
    User user = createUser("testuser", 1L);
    RepoSubscription sub = createSubscription(user, "owner/repo");
    PullRequest pr = createPullRequest(sub, "abc123");
    PrSnapshot snapshot = createSnapshot(pr, "abc123");
    createReviewRun(snapshot, "COMPLETED");

    dailyReportService.generateReport(sub, LocalDate.now());

    List<DailyReportResponse> reports = dailyReportService.getReportsBySubscription(sub.getId());
    assertThat(reports).hasSize(1);
  }

  @Test
  @DisplayName("리포트 상세 조회 - diff 포함")
  void getReportDetail_withDiffs() {
    User user = createUser("testuser", 1L);
    RepoSubscription sub = createSubscription(user, "owner/repo");
    PullRequest pr = createPullRequest(sub, "abc123");
    PrSnapshot snapshot = createSnapshot(pr, "abc123");
    createReviewRun(snapshot, "COMPLETED");

    dailyReportService.generateReport(sub, LocalDate.now());

    DailyReport report = dailyReportRepository.findAll().get(0);
    DailyReportDetailResponse detail = dailyReportService.getReportDetail(report.getId());

    assertThat(detail.getReport()).isNotNull();
    assertThat(detail.getDiffs()).hasSize(1);
  }

  @Test
  @DisplayName("존재하지 않는 리포트 상세 조회 시 예외")
  void getReportDetail_notFound() {
    assertThatThrownBy(() -> dailyReportService.getReportDetail(999L))
      .isInstanceOf(ResourceNotFoundException.class);
  }
}
