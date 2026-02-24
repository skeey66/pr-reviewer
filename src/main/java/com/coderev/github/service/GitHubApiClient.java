package com.coderev.github.service;

import com.coderev.auth.repository.OAuthTokenRepository;
import com.coderev.common.config.EncryptionConfig;
import com.coderev.common.exception.ResourceNotFoundException;
import com.coderev.github.dto.GitHubPullRequestResponse;
import com.coderev.github.dto.GitHubRepoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubApiClient {

  private final WebClient githubWebClient;
  private final OAuthTokenRepository oAuthTokenRepository;
  private final EncryptionConfig encryptionConfig;

  // 사용자의 복호화된 GitHub 토큰 조회
  public String getDecryptedToken(Long userId) {
    return oAuthTokenRepository.findByUserId(userId)
      .map(token -> encryptionConfig.decrypt(token.getAccessToken()))
      .orElseThrow(() -> new ResourceNotFoundException("OAuth 토큰을 찾을 수 없습니다. userId=" + userId));
  }

  // 사용자의 GitHub 저장소 목록 조회
  public List<GitHubRepoResponse> getUserRepos(Long userId) {
    String token = getDecryptedToken(userId);

    return githubWebClient.get()
      .uri("/user/repos?per_page=100&sort=updated")
      .header("Authorization", "Bearer " + token)
      .retrieve()
      .bodyToFlux(GitHubRepoResponse.class)
      .collectList()
      .block();
  }

  // 저장소의 open PR 목록 조회
  public List<GitHubPullRequestResponse> getOpenPullRequests(String repoFullName, String token) {
    return githubWebClient.get()
      .uri("/repos/{repoFullName}/pulls?state=open&per_page=100", repoFullName)
      .header("Authorization", "Bearer " + token)
      .retrieve()
      .bodyToFlux(GitHubPullRequestResponse.class)
      .collectList()
      .block();
  }

  // PR diff 조회
  public String getPullRequestDiff(String repoFullName, int prNumber, String token) {
    return githubWebClient.get()
      .uri("/repos/{repoFullName}/pulls/{prNumber}", repoFullName, prNumber)
      .header("Authorization", "Bearer " + token)
      .header("Accept", "application/vnd.github.diff")
      .retrieve()
      .bodyToMono(String.class)
      .block();
  }
}
