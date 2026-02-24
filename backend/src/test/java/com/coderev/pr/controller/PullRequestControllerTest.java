package com.coderev.pr.controller;

import com.coderev.pr.dto.PullRequestResponse;
import com.coderev.pr.service.PullRequestService;
import com.coderev.review.dto.ReviewCommentResponse;
import com.coderev.review.dto.ReviewDetailResponse;
import com.coderev.review.dto.ReviewRunResponse;
import com.coderev.review.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class PullRequestControllerTest {

  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
    .withDatabaseName("pr_reviewer_test")
    .withUsername("test")
    .withPassword("test");

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private PullRequestService pullRequestService;

  @MockitoBean
  private ReviewService reviewService;

  @Test
  @DisplayName("GET /api/subscriptions/{id}/pull-requests - PR 목록 조회")
  void getPullRequests_success() throws Exception {
    PullRequestResponse response = PullRequestResponse.builder()
      .id(1L)
      .githubPrId(100L)
      .prNumber(1)
      .title("Test PR")
      .author("testuser")
      .state("open")
      .headSha("abc123")
      .baseBranch("main")
      .headBranch("feature")
      .openedAt(LocalDateTime.now())
      .createdAt(LocalDateTime.now())
      .build();

    when(pullRequestService.getPullRequestsBySubscription(anyLong())).thenReturn(List.of(response));

    mockMvc.perform(get("/api/subscriptions/1/pull-requests")
        .with(oauth2Login().attributes(attrs -> attrs.put("id", 12345))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data[0].title").value("Test PR"));
  }

  @Test
  @DisplayName("GET /api/pull-requests/{id}/reviews - 리뷰 이력 조회")
  void getReviews_success() throws Exception {
    ReviewRunResponse response = ReviewRunResponse.builder()
      .id(1L)
      .snapshotId(1L)
      .status("COMPLETED")
      .model("gpt-4o")
      .totalTokens(300)
      .createdAt(LocalDateTime.now())
      .build();

    when(reviewService.getReviewsByPullRequest(anyLong())).thenReturn(List.of(response));

    mockMvc.perform(get("/api/pull-requests/1/reviews")
        .with(oauth2Login().attributes(attrs -> attrs.put("id", 12345))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data[0].status").value("COMPLETED"));
  }

  @Test
  @DisplayName("POST /api/pull-requests/{id}/review - 수동 리뷰 트리거")
  void triggerReview_success() throws Exception {
    ReviewRunResponse response = ReviewRunResponse.builder()
      .id(1L)
      .snapshotId(1L)
      .status("COMPLETED")
      .model("gpt-4o")
      .totalTokens(150)
      .createdAt(LocalDateTime.now())
      .build();

    when(reviewService.triggerManualReview(anyLong())).thenReturn(response);

    mockMvc.perform(post("/api/pull-requests/1/review")
        .with(oauth2Login().attributes(attrs -> attrs.put("id", 12345))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.status").value("COMPLETED"));
  }

  @Test
  @DisplayName("GET /api/reviews/{id} - 리뷰 상세 조회")
  void getReviewDetail_success() throws Exception {
    ReviewDetailResponse detail = ReviewDetailResponse.builder()
      .reviewRun(ReviewRunResponse.builder()
        .id(1L).snapshotId(1L).status("COMPLETED").model("gpt-4o")
        .createdAt(LocalDateTime.now()).build())
      .comments(List.of(ReviewCommentResponse.builder()
        .id(1L).filePath("src/Test.java").severity("CRITICAL").category("bug")
        .body("심각한 버그").build()))
      .build();

    when(reviewService.getReviewDetail(anyLong())).thenReturn(detail);

    mockMvc.perform(get("/api/reviews/1")
        .with(oauth2Login().attributes(attrs -> attrs.put("id", 12345))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.comments[0].severity").value("CRITICAL"));
  }
}
