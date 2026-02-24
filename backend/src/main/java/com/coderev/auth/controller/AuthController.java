package com.coderev.auth.controller;

import com.coderev.auth.dto.UserResponse;
import com.coderev.auth.entity.User;
import com.coderev.auth.repository.UserRepository;
import com.coderev.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

  private final UserRepository userRepository;

  @Operation(summary = "현재 로그인 사용자 정보 조회")
  @GetMapping("/me")
  public ApiResponse<UserResponse> me(@AuthenticationPrincipal OAuth2User oAuth2User) {
    Long githubId = ((Number) oAuth2User.getAttributes().get("id")).longValue();

    User user = userRepository.findByGithubId(githubId)
      .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

    return ApiResponse.ok(UserResponse.from(user));
  }
}
