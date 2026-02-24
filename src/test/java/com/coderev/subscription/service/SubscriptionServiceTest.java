package com.coderev.subscription.service;

import com.coderev.auth.entity.User;
import com.coderev.common.exception.ResourceNotFoundException;
import com.coderev.subscription.dto.SubscriptionRequest;
import com.coderev.subscription.dto.SubscriptionResponse;
import com.coderev.subscription.entity.RepoSubscription;
import com.coderev.support.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubscriptionServiceTest extends IntegrationTestBase {

  @Autowired
  private SubscriptionService subscriptionService;

  @Test
  @DisplayName("구독 등록 성공")
  void subscribe_success() {
    User user = createUser("testuser", 1L);
    SubscriptionRequest request = createRequest(100L, "owner/repo");

    SubscriptionResponse response = subscriptionService.subscribe(user.getId(), request);

    assertThat(response.getRepoFullName()).isEqualTo("owner/repo");
    assertThat(response.getIsActive()).isTrue();
    assertThat(response.getReviewLanguage()).isEqualTo("ko");
  }

  @Test
  @DisplayName("중복 구독 시 예외 발생")
  void subscribe_duplicate_throwsException() {
    User user = createUser("testuser", 1L);
    createSubscription(user, "owner/repo");

    SubscriptionRequest request = createRequest(100L, "owner/repo");

    assertThatThrownBy(() -> subscriptionService.subscribe(user.getId(), request))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("이미 구독 중인 저장소");
  }

  @Test
  @DisplayName("구독 해제 성공 (soft delete)")
  void unsubscribe_success() {
    User user = createUser("testuser", 1L);
    RepoSubscription subscription = createSubscription(user, "owner/repo");

    subscriptionService.unsubscribe(user.getId(), subscription.getId());

    RepoSubscription updated = repoSubscriptionRepository.findById(subscription.getId()).orElseThrow();
    assertThat(updated.getIsActive()).isFalse();
  }

  @Test
  @DisplayName("다른 사용자의 구독 해제 시도 시 예외 발생")
  void unsubscribe_otherUser_throwsException() {
    User user1 = createUser("user1", 1L);
    User user2 = createUser("user2", 2L);
    RepoSubscription subscription = createSubscription(user1, "owner/repo");

    assertThatThrownBy(() -> subscriptionService.unsubscribe(user2.getId(), subscription.getId()))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("권한이 없습니다");
  }

  @Test
  @DisplayName("존재하지 않는 구독 해제 시 예외 발생")
  void unsubscribe_notFound_throwsException() {
    User user = createUser("testuser", 1L);

    assertThatThrownBy(() -> subscriptionService.unsubscribe(user.getId(), 999L))
      .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("내 구독 목록 조회 - 활성 구독만 반환")
  void getMySubscriptions_onlyActive() {
    User user = createUser("testuser", 1L);
    createSubscription(user, "owner/repo1");
    RepoSubscription inactive = createSubscription(user, "owner/repo2");
    inactive.deactivate();
    repoSubscriptionRepository.save(inactive);

    List<SubscriptionResponse> result = subscriptionService.getMySubscriptions(user.getId());

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getRepoFullName()).isEqualTo("owner/repo1");
  }

  private SubscriptionRequest createRequest(Long repoId, String repoFullName) {
    SubscriptionRequest request = new SubscriptionRequest();
    ReflectionTestUtils.setField(request, "repoId", repoId);
    ReflectionTestUtils.setField(request, "repoFullName", repoFullName);
    ReflectionTestUtils.setField(request, "reviewLanguage", "ko");
    return request;
  }
}
