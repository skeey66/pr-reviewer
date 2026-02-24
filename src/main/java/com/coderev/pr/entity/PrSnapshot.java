package com.coderev.pr.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pr_snapshots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PrSnapshot {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pull_request_id", nullable = false)
  private PullRequest pullRequest;

  @Column(name = "head_sha", nullable = false, length = 40)
  private String headSha;

  @Column(name = "diff_text", columnDefinition = "LONGTEXT")
  private String diffText;

  @Column(name = "file_count", nullable = false)
  @Builder.Default
  private Integer fileCount = 0;

  @Column(nullable = false)
  @Builder.Default
  private Integer additions = 0;

  @Column(nullable = false)
  @Builder.Default
  private Integer deletions = 0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
