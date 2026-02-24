package com.coderev.pr.entity;

import com.coderev.subscription.entity.RepoSubscription;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pull_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PullRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscription_id", nullable = false)
  private RepoSubscription subscription;

  @Column(name = "github_pr_id", nullable = false)
  private Long githubPrId;

  @Column(name = "pr_number", nullable = false)
  private Integer prNumber;

  @Column(nullable = false, length = 500)
  private String title;

  @Column(nullable = false, length = 100)
  private String author;

  @Column(nullable = false, length = 20)
  @Builder.Default
  private String state = "open";

  @Column(name = "head_sha", nullable = false, length = 40)
  private String headSha;

  @Column(name = "base_branch", nullable = false, length = 255)
  private String baseBranch;

  @Column(name = "head_branch", nullable = false, length = 255)
  private String headBranch;

  @Column(name = "opened_at", nullable = false)
  private LocalDateTime openedAt;

  @Column(name = "closed_at")
  private LocalDateTime closedAt;

  @Column(name = "merged_at")
  private LocalDateTime mergedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  // PR 상태 및 SHA 업데이트
  public void updateState(String state, String headSha) {
    this.state = state;
    this.headSha = headSha;
  }
}
