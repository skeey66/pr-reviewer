package com.coderev.common.config;

import com.coderev.auth.service.CustomOAuth2UserService;
import com.coderev.auth.service.OAuth2LoginSuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Map;

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
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:5173"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(AbstractHttpConfigurer::disable)
      .cors(cors -> cors.configurationSource(corsConfigurationSource()));

    if (customOAuth2UserService != null) {
      // OAuth2가 설정된 환경
      http
        .authorizeHttpRequests(auth -> auth
          .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
          .anyRequest().authenticated()
        )
        .exceptionHandling(ex -> ex
          .authenticationEntryPoint((request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            ObjectMapper mapper = new ObjectMapper();
            response.getWriter().write(mapper.writeValueAsString(
              Map.of("success", false, "message", "인증이 필요합니다.")
            ));
          })
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
