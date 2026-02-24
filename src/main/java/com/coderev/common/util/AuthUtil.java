package com.coderev.common.util;

import org.springframework.security.oauth2.core.user.OAuth2User;

public class AuthUtil {

  private AuthUtil() {
  }

  public static Long getGithubId(OAuth2User oAuth2User) {
    return ((Number) oAuth2User.getAttributes().get("id")).longValue();
  }
}
