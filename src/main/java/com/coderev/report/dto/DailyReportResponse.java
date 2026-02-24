package com.coderev.report.dto;

import com.coderev.report.entity.DailyReport;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class DailyReportResponse {

  private Long id;
  private Long subscriptionId;
  private LocalDate reportDate;
  private String summary;
  private Integer totalPrs;
  private Integer totalComments;
  private Integer criticalCount;
  private Integer warningCount;
  private Integer infoCount;
  private LocalDateTime createdAt;

  public static DailyReportResponse from(DailyReport entity) {
    return DailyReportResponse.builder()
      .id(entity.getId())
      .subscriptionId(entity.getSubscription().getId())
      .reportDate(entity.getReportDate())
      .summary(entity.getSummary())
      .totalPrs(entity.getTotalPrs())
      .totalComments(entity.getTotalComments())
      .criticalCount(entity.getCriticalCount())
      .warningCount(entity.getWarningCount())
      .infoCount(entity.getInfoCount())
      .createdAt(entity.getCreatedAt())
      .build();
  }
}
