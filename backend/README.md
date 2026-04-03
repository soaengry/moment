# Moment Backend

<p align="left">
  <img src="https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.5.10-6DB33F?style=flat-square&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Gradle-8-02303A?style=flat-square&logo=gradle&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL-8-4479A1?style=flat-square&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Redis-7-DC382D?style=flat-square&logo=redis&logoColor=white" />
  <img src="https://img.shields.io/badge/MongoDB-6-47A248?style=flat-square&logo=mongodb&logoColor=white" />
  <img src="https://img.shields.io/badge/AWS_S3-FF9900?style=flat-square&logo=amazons3&logoColor=white" />
  <img src="https://img.shields.io/badge/JWT-HS256-000000?style=flat-square&logo=jsonwebtokens&logoColor=white" />
</p>

Spring Boot 3.5.10 기반 웨딩 초대장 플랫폼 REST API.

---

## 목차

- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [설치 및 실행](#설치-및-실행)
- [환경 변수](#환경-변수)
- [API 문서](#api-문서)
- [인증 흐름](#인증-흐름)
- [개발 컨벤션](#개발-컨벤션)
- [테스트](#테스트)

---

## 기술 스택

| 분류 | 기술 | 버전 |
|------|------|------|
| 언어 | Java | 17 |
| 프레임워크 | Spring Boot | 3.5.10 |
| 빌드 | Gradle | - |
| 주 DB | MySQL | 8+ |
| 캐시/세션 | Redis | 7+ |
| 문서 DB | MongoDB | 6+ (채팅) |
| 파일 스토리지 | AWS S3 | - |
| 인증 | JWT (HS256) + OAuth2 | jjwt 0.12.3 |
| 실시간 | WebSocket + STOMP | - |
| 테스트 | JUnit 5 + AssertJ + MockMvc | - |

---

## 프로젝트 구조

```
src/main/java/com/soaengry/moment/
├── MomentApplication.java
│
├── domain/
│   ├── user/               # 인증 · 회원 관리
│   │   ├── controller/     (AuthController, UserController)
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/         (User, Role, AuthProvider)
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   └── response/
│   │   └── exception/      (UserErrorCode, UserException)
│   │
│   ├── wedding/            # 웨딩 초대장 (핵심 도메인)
│   │   └── ...             # couple, schedule, gallery, account 등 8개 서비스
│   ├── guestbook/
│   ├── attendance/
│   ├── chat/               # WebSocket + MongoDB
│   ├── feed/
│   ├── email/
│   └── bank/
│
└── global/
    ├── common/             # ApiResponse, SuccessCode
    ├── config/             # Security, CORS, S3, Redis, JWT 설정
    ├── exception/          # CustomException, ErrorCode, GlobalExceptionHandler
    ├── security/           # JWT 필터, OAuth2 핸들러
    ├── service/            # S3Service
    └── util/               # CodeGenerator, PasswordValidator, TokenHashUtil
```

각 도메인 내부 구조:
```
domain/{module}/
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
│   ├── request/
│   └── response/
└── exception/
```

---

## 설치 및 실행

### 사전 요구사항

- Java 17+
- MySQL 8+
- Redis 7+
- MongoDB 6+ (채팅 기능 사용 시)

### 1. 데이터베이스 생성

```sql
CREATE DATABASE moment CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 환경 변수 설정

```bash
cp .env.example .env
# .env 파일을 열어 모든 값 설정
```

> `application.yml`은 `ddl-auto: validate` 모드입니다. 첫 실행 전에 스키마를 직접 생성해야 합니다.

### 3. 빌드 및 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun

# 클린 빌드
./gradlew clean build

# 테스트
./gradlew test
```

서버는 `http://localhost:8080`에서 실행됩니다.

---

## 환경 변수

`backend/.env` 파일에 아래 값들을 설정합니다.

```env
# ── Database ──────────────────────────────
DB_USERNAME=root
DB_PASSWORD=your_password

# ── Redis ─────────────────────────────────
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_USERNAME=
REDIS_PASSWORD=

# ── JWT ───────────────────────────────────
JWT_SECRET=your_jwt_secret_key_min_32_chars

# ── Mail ──────────────────────────────────
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your@gmail.com
MAIL_PASSWORD=your_app_password

# ── URLs ──────────────────────────────────
FRONTEND_URL=http://localhost:3000
BASE_URL=http://localhost:8080

# ── AWS S3 ────────────────────────────────
AWS_ACCESS_KEY=your_access_key
AWS_SECRET_KEY=your_secret_key
AWS_REGION=ap-northeast-2

# ── OAuth2 ────────────────────────────────
KAKAO_CLIENT_ID=
KAKAO_CLIENT_SECRET=
NAVER_CLIENT_ID=
NAVER_CLIENT_SECRET=
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
```

---

## API 문서

모든 응답은 아래 형식으로 반환됩니다:

```json
{
  "status": {
    "code": "SUCCESS",
    "message": "요청이 성공적으로 처리되었습니다."
  },
  "data": { ... }
}
```

---

### Auth `/api/auth`

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| `POST` | `/signup` | ✗ | 회원가입 |
| `POST` | `/login` | ✗ | 로그인 (JWT 발급) |
| `POST` | `/refresh` | ✗ | 액세스 토큰 갱신 |
| `POST` | `/logout` | ✓ | 로그아웃 (Redis 토큰 무효화) |
| `POST` | `/verify-email` | ✗ | 이메일 인증 코드 확인 |
| `GET` | `/check-email` | ✗ | 이메일 중복 확인 |
| `GET` | `/check-nickname` | ✗ | 닉네임 중복 확인 |

---

### User `/api/users`

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| `GET` | `/me` | ✓ | 내 프로필 조회 |
| `PATCH` | `/me` | ✓ | 프로필 수정 |
| `PATCH` | `/me/password` | ✓ | 비밀번호 변경 |
| `DELETE` | `/me` | ✓ | 회원 탈퇴 (소프트 삭제) |
| `POST` | `/restore` | ✗ | 탈퇴 계정 복구 (30일 이내) |

---

### Wedding `/api/weddings`

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| `POST` | `/` | ✓ | 웨딩 초대장 생성 |
| `GET` | `/:invitationId` | ✗ | 초대장 조회 (공개) |
| `PATCH` | `/:id` | ✓ | 초대장 수정 |
| `DELETE` | `/:id` | ✓ | 초대장 삭제 |
| `POST/PUT/DELETE` | `/:id/couples` | ✓ | 커플 정보 관리 |
| `POST/PUT/DELETE` | `/:id/schedules` | ✓ | 일정 관리 |
| `POST/PUT/DELETE` | `/:id/accounts` | ✓ | 계좌 정보 관리 |
| `POST/PUT/DELETE` | `/:id/galleries` | ✓ | 갤러리 이미지 관리 |
| `POST/PUT/DELETE` | `/:id/transportation` | ✓ | 오시는 길 관리 |
| `POST/PUT/DELETE` | `/:id/accommodation` | ✓ | 숙소 정보 관리 |
| `POST/PUT/DELETE` | `/:id/announcements` | ✓ | 공지사항 관리 |

---

### Guestbook `/api/weddings/:weddingId/guestbook`

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| `POST` | `/` | ✗ | 방명록 작성 |
| `GET` | `/` | ✗ | 방명록 목록 조회 |
| `PATCH` | `/:id` | ✗ | 방명록 수정 (비밀번호 필요) |
| `DELETE` | `/:id` | ✗ | 방명록 삭제 (비밀번호 필요) |
| `POST` | `/:id/verify` | ✗ | 방명록 비밀번호 확인 |

---

### Feed `/api/feed`

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| `POST` | `/` | ✓ | 게시글 작성 |
| `GET` | `/` | ✗ | 피드 목록 조회 (페이지네이션) |
| `GET` | `/:id` | ✗ | 게시글 상세 조회 |
| `PATCH` | `/:id` | ✓ | 게시글 수정 |
| `DELETE` | `/:id` | ✓ | 게시글 삭제 |
| `POST` | `/:id/comments` | ✓ | 댓글 작성 |
| `DELETE` | `/:id/comments/:commentId` | ✓ | 댓글 삭제 |
| `POST` | `/:id/likes` | ✓ | 좋아요 토글 |
| `POST` | `/:id/bookmarks` | ✓ | 북마크 토글 |

---

### Chat `/api/weddings/:weddingId/chat`

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| `GET` | `/messages` | ✓ | 채팅 메시지 조회 |
| `POST` | `/images` | ✓ | 채팅 이미지 업로드 |

**WebSocket** (STOMP)

| Destination | 설명 |
|-------------|------|
| `/chat.sendMessage` | 메시지 전송 |
| `/topic/wedding/{id}` | 메시지 수신 구독 |

---

### Attendance `/api/attendances`

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| `POST` | `/` | ✗ | 참석 여부 등록 |
| `GET` | `/:weddingId` | ✓ | 참석자 목록 조회 |
| `DELETE` | `/:id` | ✓ | 참석 정보 삭제 |

---

### File `/api/files`

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| `POST` | `/image` | ✓ | 단일 이미지 업로드 (S3) |
| `POST` | `/images` | ✓ | 다중 이미지 업로드 (S3) |

---

### Bank `/api/banks`

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| `GET` | `/` | ✗ | 은행 목록 조회 |
| `POST` | `/detect` | ✗ | 계좌번호 은행 자동 감지 |

---

### Health `/hc`

| Method | Endpoint | 설명 |
|--------|----------|------|
| `GET` | `/hc` | 헬스 체크 |
| `GET` | `/env` | 환경 정보 확인 |

---

## 인증 흐름

### JWT 인증

```
1. POST /api/auth/login
   → { accessToken, refreshToken } 반환

2. 이후 요청: Authorization: Bearer {accessToken} 헤더 첨부

3. 액세스 토큰 만료 시 POST /api/auth/refresh
   → 새 accessToken 발급

4. POST /api/auth/logout
   → Redis에서 refreshToken 무효화
```

### OAuth2 소셜 로그인

```
1. 프론트엔드: /oauth2/authorization/{provider} 리다이렉트
   (provider: kakao | naver | google)

2. 콜백: /login/oauth2/code/{provider}
   → 서버에서 일회성 코드 발급 (Redis, 5분 TTL)

3. 프론트엔드: POST /api/auth/oauth2/callback?code={code}
   → { accessToken, refreshToken } 반환
```

---

## 개발 컨벤션

### 응답 형식

모든 API는 `ApiResponse<T>` 래퍼로 응답합니다:

```java
return ResponseEntity.ok(ApiResponse.of(SuccessCode.OK, data));
```

### 예외 처리

도메인별 `ErrorCode` → `Exception` → `GlobalExceptionHandler` 구조:

```java
// 1. ErrorCode 정의
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL("이미 사용 중인 이메일입니다.");
    ...
}

// 2. 예외 던지기
throw new UserException(UserErrorCode.USER_NOT_FOUND);

// 3. GlobalExceptionHandler에 핸들러 등록 필수
@ExceptionHandler(UserException.class)
public ResponseEntity<ApiResponse<?>> handleUserException(UserException e) { ... }
```

### Entity 패턴

```java
@Entity @Getter @Builder
@NoArgsConstructor(access = PROTECTED)
public class Example extends BaseTimeEntity {
    // static 팩토리
    public static Example create(...) { ... }
    // 업데이트 메서드
    public void update(...) { ... }
}
```

### DTO

모든 DTO는 **Java Record** 사용:

```java
public record CreateWeddingRequest(
    @NotBlank String title,
    @NotNull LocalDate weddingDate
) {}
```

---

## 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 클래스 테스트
./gradlew test --tests "com.soaengry.moment.domain.user.*"
```

테스트는 JUnit 5 + MockMvc + Given-When-Then 패턴을 따릅니다:

```java
@DisplayName("회원가입 - 성공")
@Test
void signup_success() {
    // given
    SignupRequest request = ...;

    // when
    ResultActions result = mockMvc.perform(post("/api/auth/signup")...);

    // then
    result.andExpect(status().isOk())
          .andExpect(jsonPath("$.data.email").value(request.email()));
}
```
