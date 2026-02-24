package com.coderev.pr.repository;

import com.coderev.pr.entity.PrSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrSnapshotRepository extends JpaRepository<PrSnapshot, Long> {

  Optional<PrSnapshot> findTopByPullRequestIdOrderByCreatedAtDesc(Long pullRequestId);

  boolean existsByPullRequestIdAndHeadSha(Long pullRequestId, String headSha);
}
