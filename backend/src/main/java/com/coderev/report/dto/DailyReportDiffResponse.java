package com.coderev.report.dto;

import com.coderev.report.entity.DailyReportDiff;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DailyReportDiffResponse {

  private Long id;
  private Long pullRequestId;
  private Long reviewRunId;
  private Integer commentCount;
  private Integer criticalCount;
  private Integer warningCount;

  public static DailyReportDiffResponse from(DailyReportDiff entity) {
    return DailyReportDiffResponse.builder()
      .id(entity.getId())
      .pullRequestId(entity.getPullRequest().getId())
      .reviewRunId(entity.getReviewRun() != null ? entity.getReviewRun().getId() : null)
      .commentCount(entity.getCommentCount())
      .criticalCount(entity.getCriticalCount())
      .warningCount(entity.getWarningCount())
      .build();
  }
}
