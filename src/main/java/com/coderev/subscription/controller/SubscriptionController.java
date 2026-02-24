package com.coderev.subscription.controller;

import com.coderev.auth.entity.User;
import com.coderev.auth.repository.UserRepository;
import com.coderev.common.dto.ApiResponse;
import com.coderev.common.exception.ResourceNotFoundException;
import com.coderev.common.util.AuthUtil;
import com.coderev.github.dto.GitHubRepoResponse;
import com.coderev.github.service.GitHubApiClient;
import com.coderev.subscription.dto.SubscriptionRequest;
import com.coderev.subscription.dto.SubscriptionResponse;
import com.coderev.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Subscription", description = "저장소 구독 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubscriptionController {

  private final SubscriptionService subscriptionService;
  private final GitHubApiClient gitHubApiClient;
  private final UserRepository userRepository;

  @Operation(summary = "GitHub 저장소 목록 조회")
  @GetMapping("/repos")
  public ApiResponse<List<GitHubRepoResponse>> getRepos(@AuthenticationPrincipal OAuth2User oAuth2User) {
    Long userId = getUserId(oAuth2User);
    return ApiResponse.ok(gitHubApiClient.getUserRepos(userId));
  }

  @Operation(summary = "내 구독 목록 조회")
  @GetMapping("/subscriptions")
  public ApiResponse<List<SubscriptionResponse>> getSubscriptions(@AuthenticationPrincipal OAuth2User oAuth2User) {
    Long userId = getUserId(oAuth2User);
    return ApiResponse.ok(subscriptionService.getMySubscriptions(userId));
  }

  @Operation(summary = "저장소 구독 등록")
  @PostMapping("/subscriptions")
  public ApiResponse<SubscriptionResponse> subscribe(
      @AuthenticationPrincipal OAuth2User oAuth2User,
      @Valid @RequestBody SubscriptionRequest request) {
    Long userId = getUserId(oAuth2User);
    return ApiResponse.ok(subscriptionService.subscribe(userId, request));
  }

  @Operation(summary = "구독 해제")
  @DeleteMapping("/subscriptions/{id}")
  public ApiResponse<Void> unsubscribe(
      @AuthenticationPrincipal OAuth2User oAuth2User,
      @PathVariable Long id) {
    Long userId = getUserId(oAuth2User);
    subscriptionService.unsubscribe(userId, id);
    return ApiResponse.ok();
  }

  private Long getUserId(OAuth2User oAuth2User) {
    Long githubId = AuthUtil.getGithubId(oAuth2User);
    User user = userRepository.findByGithubId(githubId)
      .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
    return user.getId();
  }
}
