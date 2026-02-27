package com.coderev.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

  @Bean
  public WebClient githubWebClient(@Value("${app.github.api-url}") String apiUrl) {
    ObjectMapper mapper = new ObjectMapper()
      .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
      .registerModule(new JavaTimeModule())
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    ExchangeStrategies strategies = ExchangeStrategies.builder()
      .codecs(config -> config.defaultCodecs()
        .jackson2JsonDecoder(new Jackson2JsonDecoder(mapper)))
      .build();

    return WebClient.builder()
      .baseUrl(apiUrl)
      .exchangeStrategies(strategies)
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
