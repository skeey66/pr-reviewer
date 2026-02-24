package com.coderev.review.service;

import com.coderev.review.dto.OpenAiReviewResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiClient {

  private final WebClient openaiWebClient;
  private final ObjectMapper objectMapper;

  @Value("${app.openai.api-key}")
  private String apiKey;

  @Value("${app.openai.model}")
  private String model;

  private static final String SYSTEM_PROMPT = """
      당신은 시니어 소프트웨어 엔지니어이자 코드 리뷰어입니다.
      주어진 PR diff를 분석하여 코드 리뷰 코멘트를 JSON 형식으로 반환하세요.

      반환 형식:
      {
        "comments": [
          {
            "filePath": "파일 경로",
            "startLine": 시작 라인 번호,
            "endLine": 종료 라인 번호,
            "severity": "CRITICAL | WARNING | INFO",
            "category": "bug | security | performance | style | maintainability",
            "body": "리뷰 코멘트 내용"
          }
        ]
      }

      규칙:
      - severity는 CRITICAL(버그/보안), WARNING(개선필요), INFO(제안) 중 하나
      - 실질적이고 구체적인 피드백만 제공
      - 불필요한 스타일 지적은 최소화
      """;

  // OpenAI API 호출하여 코드 리뷰 수행
  public ReviewResult review(String diff, String language) {
    String userPrompt = "리뷰 언어: " + language + "\n\n다음 PR diff를 리뷰해주세요:\n\n" + diff;

    Map<String, Object> requestBody = Map.of(
      "model", model,
      "response_format", Map.of("type", "json_object"),
      "messages", List.of(
        Map.of("role", "system", "content", SYSTEM_PROMPT),
        Map.of("role", "user", "content", userPrompt)
      )
    );

    String responseBody = openaiWebClient.post()
      .uri("/v1/chat/completions")
      .header("Authorization", "Bearer " + apiKey)
      .header("Content-Type", "application/json")
      .bodyValue(requestBody)
      .retrieve()
      .bodyToMono(String.class)
      .block();

    return parseResponse(responseBody);
  }

  private ReviewResult parseResponse(String responseBody) {
    try {
      JsonNode root = objectMapper.readTree(responseBody);

      // 토큰 사용량 추출
      JsonNode usage = root.path("usage");
      int promptTokens = usage.path("prompt_tokens").asInt(0);
      int completionTokens = usage.path("completion_tokens").asInt(0);
      int totalTokens = usage.path("total_tokens").asInt(0);

      // 코멘트 파싱
      String content = root.path("choices").get(0).path("message").path("content").asText();
      OpenAiReviewResponse reviewResponse = objectMapper.readValue(content, OpenAiReviewResponse.class);

      return new ReviewResult(reviewResponse, promptTokens, completionTokens, totalTokens);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("OpenAI 응답 파싱 실패", e);
    }
  }

  @Getter
  @RequiredArgsConstructor
  public static class ReviewResult {
    private final OpenAiReviewResponse response;
    private final int promptTokens;
    private final int completionTokens;
    private final int totalTokens;
  }
}
