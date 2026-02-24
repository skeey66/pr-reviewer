package com.coderev.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "github_id", nullable = false, unique = true)
  private Long githubId;

  @Column(nullable = false, length = 100)
  private String login;

  @Column(length = 255)
  private String email;

  @Column(name = "avatar_url", length = 500)
  private String avatarUrl;

  @Column(nullable = false, length = 20)
  @Builder.Default
  private String role = "USER";

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

  // GitHub 프로필 정보 업데이트
  public void updateProfile(String login, String email, String avatarUrl) {
    this.login = login;
    this.email = email;
    this.avatarUrl = avatarUrl;
  }
}
