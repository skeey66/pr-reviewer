package com.coderev.report.entity;

import com.coderev.subscription.entity.RepoSubscription;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DailyReport {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscription_id", nullable = false)
  private RepoSubscription subscription;

  @Column(name = "report_date", nullable = false)
  private LocalDate reportDate;

  @Column(columnDefinition = "TEXT")
  private String summary;

  @Column(name = "total_prs", nullable = false)
  @Builder.Default
  private Integer totalPrs = 0;

  @Column(name = "total_comments", nullable = false)
  @Builder.Default
  private Integer totalComments = 0;

  @Column(name = "critical_count", nullable = false)
  @Builder.Default
  private Integer criticalCount = 0;

  @Column(name = "warning_count", nullable = false)
  @Builder.Default
  private Integer warningCount = 0;

  @Column(name = "info_count", nullable = false)
  @Builder.Default
  private Integer infoCount = 0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
