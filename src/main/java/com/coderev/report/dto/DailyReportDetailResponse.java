package com.coderev.report.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DailyReportDetailResponse {

  private DailyReportResponse report;
  private List<DailyReportDiffResponse> diffs;
}
