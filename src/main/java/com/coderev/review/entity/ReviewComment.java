package com.coderev.review.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReviewComment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_run_id", nullable = false)
  private ReviewRun reviewRun;

  @Column(name = "file_path", nullable = false, length = 500)
  private String filePath;

  @Column(name = "start_line")
  private Integer startLine;

  @Column(name = "end_line")
  private Integer endLine;

  @Column(nullable = false, length = 20)
  @Builder.Default
  private String severity = "INFO";

  @Column(length = 50)
  private String category;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String body;

  @Column(name = "github_comment_id")
  private Long githubCommentId;

  @Column(name = "posted_at")
  private LocalDateTime postedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
