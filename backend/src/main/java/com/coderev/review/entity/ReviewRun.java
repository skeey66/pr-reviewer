package com.coderev.review.entity;

import com.coderev.pr.entity.PrSnapshot;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_runs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReviewRun {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "snapshot_id", nullable = false)
  private PrSnapshot snapshot;

  @Column(nullable = false, length = 20)
  @Builder.Default
  private String status = "PENDING";

  @Column(nullable = false, length = 50)
  private String model;

  @Column(name = "prompt_tokens")
  private Integer promptTokens;

  @Column(name = "completion_tokens")
  private Integer completionTokens;

  @Column(name = "total_tokens")
  private Integer totalTokens;

  @Column(name = "started_at")
  private LocalDateTime startedAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  public void start() {
    this.status = "RUNNING";
    this.startedAt = LocalDateTime.now();
  }

  public void complete(Integer promptTokens, Integer completionTokens, Integer totalTokens) {
    this.status = "COMPLETED";
    this.promptTokens = promptTokens;
    this.completionTokens = completionTokens;
    this.totalTokens = totalTokens;
    this.completedAt = LocalDateTime.now();
  }

  public void fail(String errorMessage) {
    this.status = "FAILED";
    this.errorMessage = errorMessage;
    this.completedAt = LocalDateTime.now();
  }
}
