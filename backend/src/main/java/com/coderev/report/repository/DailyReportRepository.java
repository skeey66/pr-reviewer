package com.coderev.report.repository;

import com.coderev.report.entity.DailyReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyReportRepository extends JpaRepository<DailyReport, Long> {

  List<DailyReport> findBySubscriptionIdOrderByReportDateDesc(Long subscriptionId);

  Optional<DailyReport> findBySubscriptionIdAndReportDate(Long subscriptionId, LocalDate reportDate);
}
