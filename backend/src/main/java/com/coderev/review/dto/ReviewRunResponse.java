package com.coderev.review.dto;

import com.coderev.review.entity.ReviewRun;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewRunResponse {

  private Long id;
  private Long snapshotId;
  private String status;
  private String model;
  private Integer totalTokens;
  private LocalDateTime startedAt;
  private LocalDateTime completedAt;
  private String errorMessage;
  private LocalDateTime createdAt;

  public static ReviewRunResponse from(ReviewRun entity) {
    return ReviewRunResponse.builder()
      .id(entity.getId())
      .snapshotId(entity.getSnapshot().getId())
      .status(entity.getStatus())
      .model(entity.getModel())
      .totalTokens(entity.getTotalTokens())
      .startedAt(entity.getStartedAt())
      .completedAt(entity.getCompletedAt())
      .errorMessage(entity.getErrorMessage())
      .createdAt(entity.getCreatedAt())
      .build();
  }
}
