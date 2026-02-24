package com.coderev.pr.dto;

import com.coderev.pr.entity.PullRequest;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PullRequestResponse {

  private Long id;
  private Long githubPrId;
  private Integer prNumber;
  private String title;
  private String author;
  private String state;
  private String headSha;
  private String baseBranch;
  private String headBranch;
  private LocalDateTime openedAt;
  private LocalDateTime createdAt;

  public static PullRequestResponse from(PullRequest entity) {
    return PullRequestResponse.builder()
      .id(entity.getId())
      .githubPrId(entity.getGithubPrId())
      .prNumber(entity.getPrNumber())
      .title(entity.getTitle())
      .author(entity.getAuthor())
      .state(entity.getState())
      .headSha(entity.getHeadSha())
      .baseBranch(entity.getBaseBranch())
      .headBranch(entity.getHeadBranch())
      .openedAt(entity.getOpenedAt())
      .createdAt(entity.getCreatedAt())
      .build();
  }
}
