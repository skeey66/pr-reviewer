# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

GitHub PR을 AI(OpenAI)로 자동 리뷰하는 Spring Boot 애플리케이션. GitHub OAuth2로 인증하고, 사용자가 구독한 저장소의 PR diff를 분석하여 리뷰 코멘트를 생성한다.

## 모노레포 구조

```
pr-reviewer/
├── backend/       # Spring Boot 백엔드
├── frontend/      # React + Vite 프론트엔드
├── docker-compose.yml
├── CLAUDE.md
└── README.md
```

## 빌드 & 실행 커맨드

### 백엔드
```bash
# 빌드
cd backend && ./gradlew build

# 테스트 실행
cd backend && ./gradlew test

# 단일 테스트 실행
cd backend && ./gradlew test --tests "com.coderev.SomeTest.methodName"

# 애플리케이션 실행 (로컬)
cd backend && ./gradlew bootRun --args='--spring.profiles.active=local'

# DB 실행 (MySQL 8.0)
docker compose up -d
```

### 프론트엔드
```bash
# 의존성 설치
cd frontend && npm install

# 개발 서버
cd frontend && npm run dev

# 빌드
cd frontend && npm run build
```

## 기술 스택

### 백엔드
- Java 21, Spring Boot 3.4.1, Gradle
- Spring Security + GitHub OAuth2
- Spring Data JPA + MySQL 8.0 + Flyway 마이그레이션
- Lombok, SpringDoc OpenAPI (Swagger)

### 프론트엔드
- React 19, TypeScript, Vite
- React Router v7, Axios
- Tailwind CSS v4, Lucide React

## 아키텍처

- **패키지 루트**: `com.coderev`
- **레이어드 아키텍처**: Controller → Service → Repository
- **공통 모듈** (`common/`): config, dto, exception
- **API 응답 형식**: 모든 API는 `ApiResponse<T>`로 래핑 (`success`, `data`, `message` 필드)
- **에러 응답**: `ErrorResponse` + `GlobalExceptionHandler`로 일괄 처리
- **DB 마이그레이션**: `backend/src/main/resources/db/migration/` (Flyway, `V{번호}__설명.sql`)
- **JPA ddl-auto**: `validate` (스키마 변경은 반드시 Flyway 마이그레이션으로)

## 핵심 도메인 모델

`users` → `oauth_tokens` (1:1) → `repo_subscriptions` (1:N) → `pull_requests` (1:N) → `pr_snapshots` (1:N) → `review_runs` (1:N) → `review_comments` (1:N)

별도로 `daily_reports` / `daily_report_diffs` 테이블이 구독별 일일 리포트를 관리.

## 컨벤션

- 들여쓰기: 2칸
- 네이밍: camelCase
- 주석: 한국어 (비즈니스 로직만)
- 변수명: 영어
- Swagger 어노테이션(`@Tag`, `@Operation`)으로 API 문서화
- 퍼블릭 엔드포인트: `/api/health`, `/swagger-ui/**`, `/v3/api-docs/**`

## 환경 변수

로컬 개발 시 기본값이 설정되어 있으나, 운영 환경에서는 다음 환경 변수 필요:
`GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`, `OPENAI_API_KEY`, `ENCRYPTION_SECRET_KEY`, `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
