package com.coderev.review.repository;

import com.coderev.review.entity.ReviewComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {

  List<ReviewComment> findByReviewRunId(Long reviewRunId);

  long countByReviewRunIdAndSeverity(Long reviewRunId, String severity);
}
