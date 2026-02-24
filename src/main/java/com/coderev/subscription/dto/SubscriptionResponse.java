package com.coderev.subscription.dto;

import com.coderev.subscription.entity.RepoSubscription;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SubscriptionResponse {

  private Long id;
  private String repoFullName;
  private Long repoId;
  private Boolean isActive;
  private String reviewLanguage;
  private LocalDateTime createdAt;

  public static SubscriptionResponse from(RepoSubscription entity) {
    return SubscriptionResponse.builder()
      .id(entity.getId())
      .repoFullName(entity.getRepoFullName())
      .repoId(entity.getRepoId())
      .isActive(entity.getIsActive())
      .reviewLanguage(entity.getReviewLanguage())
      .createdAt(entity.getCreatedAt())
      .build();
  }
}
