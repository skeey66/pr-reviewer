package com.coderev.report.repository;

import com.coderev.report.entity.DailyReportDiff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DailyReportDiffRepository extends JpaRepository<DailyReportDiff, Long> {

  List<DailyReportDiff> findByDailyReportId(Long dailyReportId);
}
