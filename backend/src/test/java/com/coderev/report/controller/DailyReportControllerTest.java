package com.coderev.report.controller;

import com.coderev.report.dto.DailyReportDetailResponse;
import com.coderev.report.dto.DailyReportDiffResponse;
import com.coderev.report.dto.DailyReportResponse;
import com.coderev.report.service.DailyReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class DailyReportControllerTest {

  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
    .withDatabaseName("pr_reviewer_test")
    .withUsername("test")
    .withPassword("test");

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private DailyReportService dailyReportService;

  @Test
  @DisplayName("GET /api/subscriptions/{id}/reports - 리포트 목록 조회")
  void getReports_success() throws Exception {
    DailyReportResponse response = DailyReportResponse.builder()
      .id(1L)
      .subscriptionId(1L)
      .reportDate(LocalDate.of(2026, 2, 24))
      .summary("owner/repo: PR 1개, 코멘트 3개")
      .totalPrs(1)
      .totalComments(3)
      .criticalCount(1)
      .warningCount(1)
      .infoCount(1)
      .createdAt(LocalDateTime.now())
      .build();

    when(dailyReportService.getReportsBySubscription(anyLong())).thenReturn(List.of(response));

    mockMvc.perform(get("/api/subscriptions/1/reports")
        .with(oauth2Login().attributes(attrs -> attrs.put("id", 12345))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data[0].totalPrs").value(1))
      .andExpect(jsonPath("$.data[0].totalComments").value(3));
  }

  @Test
  @DisplayName("GET /api/reports/{id} - 리포트 상세 조회")
  void getReportDetail_success() throws Exception {
    DailyReportDetailResponse detail = DailyReportDetailResponse.builder()
      .report(DailyReportResponse.builder()
        .id(1L).subscriptionId(1L).reportDate(LocalDate.of(2026, 2, 24))
        .totalPrs(1).totalComments(3).criticalCount(1).warningCount(1).infoCount(1)
        .createdAt(LocalDateTime.now()).build())
      .diffs(List.of(DailyReportDiffResponse.builder()
        .id(1L).pullRequestId(1L).reviewRunId(1L)
        .commentCount(3).criticalCount(1).warningCount(1)
        .build()))
      .build();

    when(dailyReportService.getReportDetail(anyLong())).thenReturn(detail);

    mockMvc.perform(get("/api/reports/1")
        .with(oauth2Login().attributes(attrs -> attrs.put("id", 12345))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.report.totalPrs").value(1))
      .andExpect(jsonPath("$.data.diffs[0].commentCount").value(3));
  }
}
