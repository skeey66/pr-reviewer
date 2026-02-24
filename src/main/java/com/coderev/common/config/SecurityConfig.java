package com.coderev.common.config;

import com.coderev.auth.service.CustomOAuth2UserService;
import com.coderev.auth.service.OAuth2LoginSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Autowired(required = false)
  private CustomOAuth2UserService customOAuth2UserService;

  @Autowired(required = false)
  private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

  private static final String[] PUBLIC_ENDPOINTS = {
    "/api/health",
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/v3/api-docs/**"
  };

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable);

    if (customOAuth2UserService != null) {
      // OAuth2가 설정된 환경
      http
        .authorizeHttpRequests(auth -> auth
          .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
          .anyRequest().authenticated()
        )
        .oauth2Login(oauth2 -> oauth2
          .userInfoEndpoint(userInfo -> userInfo
            .userService(customOAuth2UserService)
          )
          .successHandler(oAuth2LoginSuccessHandler)
        );
    } else {
      // OAuth2가 없는 환경
      http
        .authorizeHttpRequests(auth -> auth
          .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
          .anyRequest().permitAll()
        );
    }

    return http.build();
  }
}
