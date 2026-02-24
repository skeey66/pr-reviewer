package com.coderev.auth.service;

import com.coderev.auth.entity.OAuthToken;
import com.coderev.auth.entity.User;
import com.coderev.auth.repository.OAuthTokenRepository;
import com.coderev.auth.repository.UserRepository;
import com.coderev.common.config.EncryptionConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

  private final UserRepository userRepository;
  private final OAuthTokenRepository oAuthTokenRepository;
  private final OAuth2AuthorizedClientService authorizedClientService;
  private final EncryptionConfig encryptionConfig;

  public OAuth2LoginSuccessHandler(
      UserRepository userRepository,
      OAuthTokenRepository oAuthTokenRepository,
      @Lazy OAuth2AuthorizedClientService authorizedClientService,
      EncryptionConfig encryptionConfig) {
    this.userRepository = userRepository;
    this.oAuthTokenRepository = oAuthTokenRepository;
    this.authorizedClientService = authorizedClientService;
    this.encryptionConfig = encryptionConfig;
  }

  @Override
  @Transactional
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
    OAuth2User oAuth2User = oauthToken.getPrincipal();

    Long githubId = ((Number) oAuth2User.getAttributes().get("id")).longValue();
    User user = userRepository.findByGithubId(githubId)
      .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + githubId));

    // OAuth2 클라이언트에서 access token 추출
    OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
      oauthToken.getAuthorizedClientRegistrationId(),
      oauthToken.getName()
    );

    String accessToken = client.getAccessToken().getTokenValue();
    String encryptedToken = encryptionConfig.encrypt(accessToken);
    String scope = String.join(",", client.getAccessToken().getScopes());

    // OAuthToken upsert
    oAuthTokenRepository.findByUserId(user.getId())
      .ifPresentOrElse(
        token -> token.updateToken(encryptedToken, scope),
        () -> oAuthTokenRepository.save(OAuthToken.builder()
          .user(user)
          .accessToken(encryptedToken)
          .scope(scope)
          .build())
      );

    response.sendRedirect("/api/me");
  }
}
