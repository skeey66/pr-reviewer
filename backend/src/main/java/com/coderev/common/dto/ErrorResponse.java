package com.coderev.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

  private final int status;
  private final String error;
  private final String message;
  private final Map<String, String> fieldErrors;
  @Builder.Default
  private final LocalDateTime timestamp = LocalDateTime.now();
}
