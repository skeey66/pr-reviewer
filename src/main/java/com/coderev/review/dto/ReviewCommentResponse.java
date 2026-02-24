package com.coderev.review.dto;

import com.coderev.review.entity.ReviewComment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewCommentResponse {

  private Long id;
  private String filePath;
  private Integer startLine;
  private Integer endLine;
  private String severity;
  private String category;
  private String body;

  public static ReviewCommentResponse from(ReviewComment entity) {
    return ReviewCommentResponse.builder()
      .id(entity.getId())
      .filePath(entity.getFilePath())
      .startLine(entity.getStartLine())
      .endLine(entity.getEndLine())
      .severity(entity.getSeverity())
      .category(entity.getCategory())
      .body(entity.getBody())
      .build();
  }
}
