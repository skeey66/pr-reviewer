package com.coderev.github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GitHubRepoResponse {

  private Long id;

  @JsonProperty("full_name")
  private String fullName;

  private String name;

  @JsonProperty("private")
  private Boolean isPrivate;

  private String description;

  private String language;

  @JsonProperty("html_url")
  private String htmlUrl;
}
