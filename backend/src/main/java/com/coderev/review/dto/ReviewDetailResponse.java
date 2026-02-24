package com.coderev.review.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReviewDetailResponse {

  private ReviewRunResponse reviewRun;
  private List<ReviewCommentResponse> comments;
}
