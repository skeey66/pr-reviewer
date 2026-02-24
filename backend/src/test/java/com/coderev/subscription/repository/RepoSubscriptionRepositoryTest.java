package com.coderev.subscription.repository;

import com.coderev.auth.entity.User;
import com.coderev.subscription.entity.RepoSubscription;
import com.coderev.support.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RepoSubscriptionRepositoryTest extends IntegrationTestBase {

  @Test
  @DisplayName("findByUserIdAndIsActiveTrue - 활성 구독만 반환")
  void findByUserIdAndIsActiveTrue() {
    User user = createUser("testuser", 1L);
    createSubscription(user, "owner/repo1");
    RepoSubscription inactive = createSubscription(user, "owner/repo2");
    inactive.deactivate();
    repoSubscriptionRepository.save(inactive);

    List<RepoSubscription> result = repoSubscriptionRepository.findByUserIdAndIsActiveTrue(user.getId());

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getRepoFullName()).isEqualTo("owner/repo1");
  }

  @Test
  @DisplayName("existsByUserIdAndRepoFullName - 존재 여부 확인")
  void existsByUserIdAndRepoFullName() {
    User user = createUser("testuser", 1L);
    createSubscription(user, "owner/repo");

    assertThat(repoSubscriptionRepository.existsByUserIdAndRepoFullName(user.getId(), "owner/repo")).isTrue();
    assertThat(repoSubscriptionRepository.existsByUserIdAndRepoFullName(user.getId(), "owner/other")).isFalse();
  }

  @Test
  @DisplayName("findByIsActiveTrue - 모든 활성 구독 반환")
  void findByIsActiveTrue() {
    User user1 = createUser("user1", 1L);
    User user2 = createUser("user2", 2L);
    createSubscription(user1, "owner/repo1");
    createSubscription(user2, "owner/repo2");
    RepoSubscription inactive = createSubscription(user1, "owner/repo3");
    inactive.deactivate();
    repoSubscriptionRepository.save(inactive);

    List<RepoSubscription> result = repoSubscriptionRepository.findByIsActiveTrue();

    assertThat(result).hasSize(2);
  }
}
