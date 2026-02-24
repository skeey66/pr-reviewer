package com.coderev.common.exception;

import com.coderev.common.dto.ErrorResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
    Map<String, String> fieldErrors = new HashMap<>();
    for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
      fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }

    ErrorResponse response = ErrorResponse.builder()
      .status(HttpStatus.BAD_REQUEST.value())
      .error("Validation Failed")
      .message("입력값 검증에 실패했습니다.")
      .fieldErrors(fieldErrors)
      .build();

    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException e) {
    ErrorResponse response = ErrorResponse.builder()
      .status(HttpStatus.NOT_FOUND.value())
      .error("Not Found")
      .message(e.getMessage())
      .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
    ErrorResponse response = ErrorResponse.builder()
      .status(HttpStatus.BAD_REQUEST.value())
      .error("Bad Request")
      .message(e.getMessage())
      .build();

    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    log.error("처리되지 않은 예외 발생", e);

    ErrorResponse response = ErrorResponse.builder()
      .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
      .error("Internal Server Error")
      .message("서버 내부 오류가 발생했습니다.")
      .build();

    return ResponseEntity.internalServerError().body(response);
  }
}
