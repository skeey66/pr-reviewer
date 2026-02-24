package com.coderev.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

  @Bean
  public WebClient githubWebClient(@Value("${app.github.api-url}") String apiUrl) {
    return WebClient.builder()
      .baseUrl(apiUrl)
      .defaultHeader("Accept", "application/vnd.github+json")
      .build();
  }

  @Bean
  public WebClient openaiWebClient() {
    return WebClient.builder()
      .baseUrl("https://api.openai.com")
      .build();
  }
}
