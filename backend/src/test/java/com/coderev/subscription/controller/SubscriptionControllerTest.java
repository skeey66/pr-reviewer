package com.coderev.subscription.controller;

import com.coderev.auth.entity.User;
import com.coderev.auth.repository.UserRepository;
import com.coderev.github.service.GitHubApiClient;
import com.coderev.subscription.dto.SubscriptionResponse;
import com.coderev.subscription.service.SubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class SubscriptionControllerTest {

  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
    .withDatabaseName("pr_reviewer_test")
    .withUsername("test")
    .withPassword("test");

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private SubscriptionService subscriptionService;

  @MockitoBean
  private GitHubApiClient gitHubApiClient;

  @MockitoBean
  private UserRepository userRepository;

  private void mockUser() {
    User mockUser = User.builder().id(1L).githubId(12345L).login("testuser").build();
    when(userRepository.findByGithubId(12345L)).thenReturn(Optional.of(mockUser));
  }

  @Test
  @DisplayName("GET /api/subscriptions - 구독 목록 조회 성공")
  void getSubscriptions_success() throws Exception {
    mockUser();
    SubscriptionResponse response = SubscriptionResponse.builder()
      .id(1L)
      .repoFullName("owner/repo")
      .repoId(100L)
      .isActive(true)
      .reviewLanguage("ko")
      .createdAt(LocalDateTime.now())
      .build();

    when(subscriptionService.getMySubscriptions(anyLong())).thenReturn(List.of(response));

    mockMvc.perform(get("/api/subscriptions")
        .with(oauth2Login().attributes(attrs -> attrs.put("id", 12345))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data[0].repoFullName").value("owner/repo"));
  }

  @Test
  @DisplayName("POST /api/subscriptions - 구독 등록 성공")
  void subscribe_success() throws Exception {
    mockUser();
    SubscriptionResponse response = SubscriptionResponse.builder()
      .id(1L)
      .repoFullName("owner/repo")
      .repoId(100L)
      .isActive(true)
      .reviewLanguage("ko")
      .createdAt(LocalDateTime.now())
      .build();

    when(subscriptionService.subscribe(anyLong(), any())).thenReturn(response);

    String requestBody = """
      {
        "repoId": 100,
        "repoFullName": "owner/repo",
        "reviewLanguage": "ko"
      }
      """;

    mockMvc.perform(post("/api/subscriptions")
        .with(oauth2Login().attributes(attrs -> attrs.put("id", 12345)))
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.repoFullName").value("owner/repo"));
  }

  @Test
  @DisplayName("DELETE /api/subscriptions/{id} - 구독 해제 성공")
  void unsubscribe_success() throws Exception {
    mockUser();
    doNothing().when(subscriptionService).unsubscribe(anyLong(), anyLong());

    mockMvc.perform(delete("/api/subscriptions/1")
        .with(oauth2Login().attributes(attrs -> attrs.put("id", 12345))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @DisplayName("인증 없이 접근 시 리다이렉트")
  void noAuth_redirects() throws Exception {
    mockMvc.perform(get("/api/subscriptions"))
      .andExpect(status().is3xxRedirection());
  }
}
