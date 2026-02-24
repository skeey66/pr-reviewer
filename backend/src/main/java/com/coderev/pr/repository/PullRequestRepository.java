package com.coderev.pr.repository;

import com.coderev.pr.entity.PullRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PullRequestRepository extends JpaRepository<PullRequest, Long> {

  Optional<PullRequest> findBySubscriptionIdAndGithubPrId(Long subscriptionId, Long githubPrId);

  List<PullRequest> findBySubscriptionId(Long subscriptionId);
}
