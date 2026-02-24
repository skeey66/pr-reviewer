package com.coderev.report.controller;

import com.coderev.common.dto.ApiResponse;
import com.coderev.report.dto.DailyReportDetailResponse;
import com.coderev.report.dto.DailyReportResponse;
import com.coderev.report.service.DailyReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Daily Report", description = "일일 리포트 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DailyReportController {

  private final DailyReportService dailyReportService;

  @Operation(summary = "구독별 리포트 목록 조회")
  @GetMapping("/subscriptions/{subscriptionId}/reports")
  public ApiResponse<List<DailyReportResponse>> getReports(@PathVariable Long subscriptionId) {
    return ApiResponse.ok(dailyReportService.getReportsBySubscription(subscriptionId));
  }

  @Operation(summary = "리포트 상세 조회")
  @GetMapping("/reports/{reportId}")
  public ApiResponse<DailyReportDetailResponse> getReportDetail(@PathVariable Long reportId) {
    return ApiResponse.ok(dailyReportService.getReportDetail(reportId));
  }
}
