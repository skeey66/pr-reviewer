package com.coderev.auth.dto;

import com.coderev.auth.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

  private Long githubId;
  private String login;
  private String email;
  private String avatarUrl;

  public static UserResponse from(User user) {
    return UserResponse.builder()
      .githubId(user.getGithubId())
      .login(user.getLogin())
      .email(user.getEmail())
      .avatarUrl(user.getAvatarUrl())
      .build();
  }
}
