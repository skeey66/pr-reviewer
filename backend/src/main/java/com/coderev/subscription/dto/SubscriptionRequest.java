package com.coderev.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SubscriptionRequest {

  @NotNull(message = "저장소 ID는 필수입니다.")
  private Long repoId;

  @NotBlank(message = "저장소 전체 이름은 필수입니다.")
  private String repoFullName;

  private String reviewLanguage = "ko";
}
