package com.coderev.github.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class GitHubPullRequestResponse {

  private Long id;

  private Integer number;

  private String title;

  private String state;

  private OffsetDateTime createdAt;

  private OffsetDateTime closedAt;

  private OffsetDateTime mergedAt;

  private GitHubUserResponse user;

  private GitHubBranchRef head;

  private GitHubBranchRef base;

  @Getter
  @NoArgsConstructor
  public static class GitHubUserResponse {
    private String login;
  }

  @Getter
  @NoArgsConstructor
  public static class GitHubBranchRef {
    private String ref;
    private String sha;
  }
}
