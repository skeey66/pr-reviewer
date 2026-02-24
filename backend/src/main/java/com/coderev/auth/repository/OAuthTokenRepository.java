package com.coderev.auth.repository;

import com.coderev.auth.entity.OAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthTokenRepository extends JpaRepository<OAuthToken, Long> {

  Optional<OAuthToken> findByUserId(Long userId);
}
