package com.coderev.review.repository;

import com.coderev.review.entity.ReviewRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewRunRepository extends JpaRepository<ReviewRun, Long> {

  List<ReviewRun> findBySnapshotPullRequestId(Long pullRequestId);

  List<ReviewRun> findByStatusAndCompletedAtBetween(String status, LocalDateTime start, LocalDateTime end);
}
