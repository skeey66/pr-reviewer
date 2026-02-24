package com.coderev.subscription.repository;

import com.coderev.subscription.entity.RepoSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepoSubscriptionRepository extends JpaRepository<RepoSubscription, Long> {

  List<RepoSubscription> findByUserIdAndIsActiveTrue(Long userId);

  List<RepoSubscription> findByIsActiveTrue();

  boolean existsByUserIdAndRepoFullName(Long userId, String repoFullName);
}
