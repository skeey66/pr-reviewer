package com.coderev;

import com.coderev.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health", description = "서비스 상태 확인")
@RestController
@RequestMapping("/api")
public class HealthController {

  @Operation(summary = "헬스 체크", description = "서비스 정상 동작 여부를 확인합니다.")
  @GetMapping("/health")
  public ApiResponse<Map<String, String>> health() {
    return ApiResponse.ok(Map.of("status", "UP"));
  }
}
