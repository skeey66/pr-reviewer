-- 사용자 테이블
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  github_id BIGINT NOT NULL UNIQUE,
  login VARCHAR(100) NOT NULL,
  email VARCHAR(255),
  avatar_url VARCHAR(500),
  role VARCHAR(20) NOT NULL DEFAULT 'USER',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- OAuth 토큰 테이블
CREATE TABLE oauth_tokens (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  access_token TEXT NOT NULL,
  refresh_token TEXT,
  token_type VARCHAR(50) NOT NULL DEFAULT 'bearer',
  scope VARCHAR(500),
  expires_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_oauth_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE UNIQUE INDEX idx_oauth_tokens_user_id ON oauth_tokens (user_id);

-- 저장소 구독 테이블
CREATE TABLE repo_subscriptions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  repo_full_name VARCHAR(255) NOT NULL,
  repo_id BIGINT NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  webhook_id BIGINT,
  review_language VARCHAR(10) NOT NULL DEFAULT 'ko',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_repo_subscriptions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE UNIQUE INDEX idx_repo_sub_user_repo ON repo_subscriptions (user_id, repo_full_name);

-- PR 테이블
CREATE TABLE pull_requests (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  subscription_id BIGINT NOT NULL,
  github_pr_id BIGINT NOT NULL,
  pr_number INT NOT NULL,
  title VARCHAR(500) NOT NULL,
  author VARCHAR(100) NOT NULL,
  state VARCHAR(20) NOT NULL DEFAULT 'open',
  head_sha VARCHAR(40) NOT NULL,
  base_branch VARCHAR(255) NOT NULL,
  head_branch VARCHAR(255) NOT NULL,
  opened_at DATETIME NOT NULL,
  closed_at DATETIME,
  merged_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_pull_requests_subscription FOREIGN KEY (subscription_id) REFERENCES repo_subscriptions (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE UNIQUE INDEX idx_pr_sub_github_pr ON pull_requests (subscription_id, github_pr_id);

-- PR 스냅샷 테이블
CREATE TABLE pr_snapshots (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  pull_request_id BIGINT NOT NULL,
  head_sha VARCHAR(40) NOT NULL,
  diff_text LONGTEXT,
  file_count INT NOT NULL DEFAULT 0,
  additions INT NOT NULL DEFAULT 0,
  deletions INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_pr_snapshots_pr FOREIGN KEY (pull_request_id) REFERENCES pull_requests (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 리뷰 실행 테이블
CREATE TABLE review_runs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  snapshot_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  model VARCHAR(50) NOT NULL,
  prompt_tokens INT,
  completion_tokens INT,
  total_tokens INT,
  started_at DATETIME,
  completed_at DATETIME,
  error_message TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_review_runs_snapshot FOREIGN KEY (snapshot_id) REFERENCES pr_snapshots (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 리뷰 코멘트 테이블
CREATE TABLE review_comments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  review_run_id BIGINT NOT NULL,
  file_path VARCHAR(500) NOT NULL,
  start_line INT,
  end_line INT,
  severity VARCHAR(20) NOT NULL DEFAULT 'INFO',
  category VARCHAR(50),
  body TEXT NOT NULL,
  github_comment_id BIGINT,
  posted_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_review_comments_run FOREIGN KEY (review_run_id) REFERENCES review_runs (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 일일 리포트 테이블
CREATE TABLE daily_reports (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  subscription_id BIGINT NOT NULL,
  report_date DATE NOT NULL,
  summary TEXT,
  total_prs INT NOT NULL DEFAULT 0,
  total_comments INT NOT NULL DEFAULT 0,
  critical_count INT NOT NULL DEFAULT 0,
  warning_count INT NOT NULL DEFAULT 0,
  info_count INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_daily_reports_subscription FOREIGN KEY (subscription_id) REFERENCES repo_subscriptions (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE UNIQUE INDEX idx_daily_report_sub_date ON daily_reports (subscription_id, report_date);

-- 일일 리포트 diff 테이블
CREATE TABLE daily_report_diffs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  daily_report_id BIGINT NOT NULL,
  pull_request_id BIGINT NOT NULL,
  review_run_id BIGINT,
  comment_count INT NOT NULL DEFAULT 0,
  critical_count INT NOT NULL DEFAULT 0,
  warning_count INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_daily_report_diffs_report FOREIGN KEY (daily_report_id) REFERENCES daily_reports (id) ON DELETE CASCADE,
  CONSTRAINT fk_daily_report_diffs_pr FOREIGN KEY (pull_request_id) REFERENCES pull_requests (id) ON DELETE CASCADE,
  CONSTRAINT fk_daily_report_diffs_run FOREIGN KEY (review_run_id) REFERENCES review_runs (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
