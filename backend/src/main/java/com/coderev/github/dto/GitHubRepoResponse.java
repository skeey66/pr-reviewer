package com.coderev.github.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GitHubRepoResponse {

  private Long id;
  private String fullName;
  private String name;
  private boolean isPrivate;
  private String description;
  private String language;
  private String htmlUrl;
}
