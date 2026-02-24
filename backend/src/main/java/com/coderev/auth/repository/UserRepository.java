package com.coderev.auth.repository;

import com.coderev.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByGithubId(Long githubId);
}
