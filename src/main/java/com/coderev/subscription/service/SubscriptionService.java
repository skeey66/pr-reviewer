package com.coderev.subscription.service;

import com.coderev.auth.entity.User;
import com.coderev.auth.repository.UserRepository;
import com.coderev.common.exception.ResourceNotFoundException;
import com.coderev.subscription.dto.SubscriptionRequest;
import com.coderev.subscription.dto.SubscriptionResponse;
import com.coderev.subscription.entity.RepoSubscription;
import com.coderev.subscription.repository.RepoSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

  private final RepoSubscriptionRepository repoSubscriptionRepository;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public List<SubscriptionResponse> getMySubscriptions(Long userId) {
    return repoSubscriptionRepository.findByUserIdAndIsActiveTrue(userId).stream()
      .map(SubscriptionResponse::from)
      .toList();
  }

  @Transactional
  public SubscriptionResponse subscribe(Long userId, SubscriptionRequest request) {
    // 중복 검사
    if (repoSubscriptionRepository.existsByUserIdAndRepoFullName(userId, request.getRepoFullName())) {
      throw new IllegalArgumentException("이미 구독 중인 저장소입니다: " + request.getRepoFullName());
    }

    User user = userRepository.findById(userId)
      .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다. userId=" + userId));

    RepoSubscription subscription = RepoSubscription.builder()
      .user(user)
      .repoId(request.getRepoId())
      .repoFullName(request.getRepoFullName())
      .reviewLanguage(request.getReviewLanguage())
      .build();

    return SubscriptionResponse.from(repoSubscriptionRepository.save(subscription));
  }

  @Transactional
  public void unsubscribe(Long userId, Long subscriptionId) {
    RepoSubscription subscription = repoSubscriptionRepository.findById(subscriptionId)
      .orElseThrow(() -> new ResourceNotFoundException("구독을 찾을 수 없습니다. id=" + subscriptionId));

    // 소유권 검증
    if (!subscription.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("해당 구독에 대한 권한이 없습니다.");
    }

    subscription.deactivate();
  }
}
