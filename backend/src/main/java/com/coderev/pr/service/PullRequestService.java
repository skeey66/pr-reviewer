package com.coderev.pr.service;

import com.coderev.pr.dto.PullRequestResponse;
import com.coderev.pr.repository.PullRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PullRequestService {

  private final PullRequestRepository pullRequestRepository;

  @Transactional(readOnly = true)
  public List<PullRequestResponse> getPullRequestsBySubscription(Long subscriptionId) {
    return pullRequestRepository.findBySubscriptionId(subscriptionId).stream()
      .map(PullRequestResponse::from)
      .toList();
  }
}
