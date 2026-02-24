# PR Reviewer

GitHub PR을 AI(OpenAI)로 자동 리뷰하는 Spring Boot 애플리케이션.

사용자가 GitHub OAuth2로 로그인하고 저장소를 구독하면, 주기적으로 Open PR을 폴링하여 diff를 분석하고 코드 리뷰 코멘트를 자동 생성합니다.

## 주요 기능

- **GitHub OAuth2 인증** - GitHub 계정으로 로그인
- **저장소 구독** - 리뷰 대상 저장소를 구독/해제
- **자동 PR 폴링** - 10분 간격으로 Open PR 감지 및 diff 수집
- **AI 코드 리뷰** - OpenAI(GPT-4o)를 활용한 자동 리뷰 (CRITICAL/WARNING/INFO 심각도)
- **수동 리뷰 트리거** - API를 통해 특정 PR에 대해 즉시 리뷰 요청
- **일일 리포트** - 매일 09:00에 전날 리뷰 결과를 구독별로 집계

## 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.4.1 |
| Build | Gradle |
| Auth | Spring Security + GitHub OAuth2 |
| DB | MySQL 8.0 + Spring Data JPA |
| Migration | Flyway |
| AI | OpenAI API (GPT-4o) |
| HTTP Client | Spring WebFlux (WebClient) |
| API 문서 | SpringDoc OpenAPI (Swagger) |
| 테스트 | JUnit 5 + Testcontainers (MySQL) |

## 시작하기

### 사전 요구사항

- Java 21
- Docker (MySQL 실행용)
- GitHub OAuth App ([생성 가이드](https://docs.github.com/en/apps/oauth-apps/building-oauth-apps/creating-an-oauth-app))
- OpenAI API Key

### 1. 환경 변수 설정

```bash
export GITHUB_CLIENT_ID=your_github_client_id
export GITHUB_CLIENT_SECRET=your_github_client_secret
export OPENAI_API_KEY=your_openai_api_key
export ENCRYPTION_SECRET_KEY=your_32_character_secret_key
```

### 2. DB 실행

```bash
docker compose up -d
```

MySQL이 `localhost:3307`에서 실행됩니다.

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

### 4. 접속

- 애플리케이션: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

## API 엔드포인트

### 인증
| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/me` | 로그인 사용자 정보 조회 |

### 구독
| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/repos` | GitHub 저장소 목록 조회 |
| GET | `/api/subscriptions` | 내 구독 목록 조회 |
| POST | `/api/subscriptions` | 저장소 구독 등록 |
| DELETE | `/api/subscriptions/{id}` | 구독 해제 |

### PR & 리뷰
| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/subscriptions/{id}/pull-requests` | 구독별 PR 목록 |
| GET | `/api/pull-requests/{id}/reviews` | PR별 리뷰 이력 |
| POST | `/api/pull-requests/{id}/review` | 수동 리뷰 트리거 |
| GET | `/api/reviews/{id}` | 리뷰 상세 (코멘트 포함) |

### 일일 리포트
| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/subscriptions/{id}/reports` | 구독별 리포트 목록 |
| GET | `/api/reports/{id}` | 리포트 상세 |

### 공개 엔드포인트
| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/health` | 헬스 체크 |

## 도메인 모델

```
users
 └── oauth_tokens (1:1)
 └── repo_subscriptions (1:N)
      └── pull_requests (1:N)
      │    └── pr_snapshots (1:N)
      │         └── review_runs (1:N)
      │              └── review_comments (1:N)
      └── daily_reports (1:N)
           └── daily_report_diffs (1:N)
```

## 핵심 플로우

### 자동 리뷰 플로우

```
[10분 간격 스케줄러]
    ↓
활성 구독 목록 조회
    ↓
구독별 GitHub Open PR 폴링
    ↓
PR upsert (신규 생성 / 상태 업데이트)
    ↓
headSha 변경 감지 → diff 스냅샷 생성
    ↓
OpenAI API로 코드 리뷰 요청
    ↓
리뷰 코멘트 저장 (severity: CRITICAL/WARNING/INFO)
```

### 일일 리포트 플로우

```
[매일 09:00 스케줄러]
    ↓
전날 COMPLETED 상태의 리뷰 조회
    ↓
구독별 → PR별 그룹핑
    ↓
DailyReport + DailyReportDiff 생성
```

## 환경 변수

| 변수 | 설명 | 기본값 (로컬) |
|------|------|--------------|
| `GITHUB_CLIENT_ID` | GitHub OAuth App Client ID | - |
| `GITHUB_CLIENT_SECRET` | GitHub OAuth App Client Secret | - |
| `OPENAI_API_KEY` | OpenAI API Key | - |
| `OPENAI_MODEL` | OpenAI 모델명 | `gpt-4o` |
| `ENCRYPTION_SECRET_KEY` | OAuth 토큰 암호화 키 (32자) | `default-dev-key-change-me-32ch` |
| `DB_HOST` | MySQL 호스트 | `localhost` |
| `DB_PORT` | MySQL 포트 | `3306` |
| `DB_NAME` | 데이터베이스명 | `pr_reviewer` |
| `DB_USERNAME` | DB 사용자명 | `app` |
| `DB_PASSWORD` | DB 비밀번호 | `app1234` |
| `SERVER_PORT` | 서버 포트 | `8080` |
