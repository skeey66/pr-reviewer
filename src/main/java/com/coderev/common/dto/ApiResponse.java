package com.coderev.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

  private final boolean success;
  private final T data;
  private final String message;

  public static <T> ApiResponse<T> ok(T data) {
    return ApiResponse.<T>builder()
      .success(true)
      .data(data)
      .build();
  }

  public static ApiResponse<Void> ok() {
    return ApiResponse.<Void>builder()
      .success(true)
      .build();
  }

  public static ApiResponse<Void> error(String message) {
    return ApiResponse.<Void>builder()
      .success(false)
      .message(message)
      .build();
  }
}
