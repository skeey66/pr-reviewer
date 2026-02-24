package com.coderev.github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
public class GitHubPullRequestResponse {

  private Long id;

  private Integer number;

  private String title;

  private String state;

  @JsonProperty("created_at")
  private OffsetDateTime createdAt;

  @JsonProperty("closed_at")
  private OffsetDateTime closedAt;

  @JsonProperty("merged_at")
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
