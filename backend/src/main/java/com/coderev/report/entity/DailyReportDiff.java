package com.coderev.report.entity;

import com.coderev.pr.entity.PullRequest;
import com.coderev.review.entity.ReviewRun;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "daily_report_diffs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DailyReportDiff {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "daily_report_id", nullable = false)
  private DailyReport dailyReport;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pull_request_id", nullable = false)
  private PullRequest pullRequest;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_run_id")
  private ReviewRun reviewRun;

  @Column(name = "comment_count", nullable = false)
  @Builder.Default
  private Integer commentCount = 0;

  @Column(name = "critical_count", nullable = false)
  @Builder.Default
  private Integer criticalCount = 0;

  @Column(name = "warning_count", nullable = false)
  @Builder.Default
  private Integer warningCount = 0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
