package com.coderev.review.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OpenAiReviewResponse {

  private List<ReviewCommentItem> comments;

  @Getter
  @NoArgsConstructor
  public static class ReviewCommentItem {
    private String filePath;
    private Integer startLine;
    private Integer endLine;
    private String severity;
    private String category;
    private String body;
  }

  @Getter
  @NoArgsConstructor
  public static class Usage {
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
  }
}
