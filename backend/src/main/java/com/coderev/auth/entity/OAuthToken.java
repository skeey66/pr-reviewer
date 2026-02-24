package com.coderev.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "oauth_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OAuthToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @Column(name = "access_token", nullable = false, columnDefinition = "TEXT")
  private String accessToken;

  @Column(name = "refresh_token", columnDefinition = "TEXT")
  private String refreshToken;

  @Column(name = "token_type", nullable = false, length = 50)
  @Builder.Default
  private String tokenType = "bearer";

  @Column(length = 500)
  private String scope;

  @Column(name = "expires_at")
  private LocalDateTime expiresAt;

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

  // 토큰 갱신
  public void updateToken(String accessToken, String scope) {
    this.accessToken = accessToken;
    this.scope = scope;
  }
}
