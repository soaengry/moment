# Moment Backend

Spring Boot 3.5.10 기반 웨딩 초대장 플랫폼 REST API.

## Tech Stack

| 분류 | 기술 | 버전 |
|------|------|------|
| 언어 | Java | 17 |
| 프레임워크 | Spring Boot | 3.5.10 |
| 빌드 | Gradle | - |
| 주 DB | MySQL | - |
| 캐시/세션 | Redis | - |
| 문서 DB | MongoDB | - (채팅) |
| 파일 스토리지 | AWS S3 | - |
| 인증 | JWT (HS256) + OAuth2 | jjwt 0.12.3 |
| 테스트 | JUnit 5 + AssertJ + MockMvc | - |

## Quick Commands

```bash
./gradlew build          # 빌드
./gradlew bootRun        # 실행
./gradlew test           # 테스트
./gradlew clean build    # 클린 빌드
```

## Project Structure

```
com.soaengry.moment/
├── domain/
│   ├── user/               # 인증/사용자 관리
│   │   ├── controller/     (AuthController, UserController)
│   │   ├── dto/
│   │   │   ├── request/    (LoginRequest, SignupRequest, VerifyEmailRequest, etc.)
│   │   │   └── response/   (SignupResponse, TokenResponse, UserResponse)
│   │   ├── entity/         (User, RefreshToken, PasswordResetToken)
│   │   ├── exception/      (UserErrorCode, UserException)
│   │   ├── repository/     (UserRepository, RefreshTokenRepository, PasswordResetTokenRepository)
│   │   └── service/        (AuthService, UserService)
│   │
│   ├── email/              # 이메일 인증
│   │   ├── entity/         (EmailVerification)
│   │   ├── exception/      (EmailErrorCode, EmailException)
│   │   ├── repository/     (EmailVerificationRepository)
│   │   └── service/        (EmailService)
│   │
│   ├── wedding/            # 웨딩 초대장 (핵심 도메인)
│   │   ├── controller/     (WeddingController)
│   │   ├── dto/
│   │   │   ├── request/    (WeddingRequest, CoupleRequest, ScheduleRequest, etc.)
│   │   │   └── response/   (WeddingResponse, WeddingInfoResponse, CoupleResponse, etc.)
│   │   ├── entity/         (Wedding, Couple, Schedule, AccountGroup, Account,
│   │   │                    Gallery, Transportation, Accommodation, Announcement)
│   │   ├── exception/      (WeddingErrorCode, WeddingException)
│   │   ├── repository/     (9개 리포지토리)
│   │   └── service/        (WeddingService)
│   │
│   ├── guestbook/          # 방명록
│   ├── attendance/         # 참석 관리
│   ├── chat/               # 채팅 (WebSocket + MongoDB)
│   ├── feed/               # 피드
│   └── bank/               # 계좌 정보
│
└── global/
    ├── common/             (ApiResponse, SuccessCode)
    ├── config/             (SecurityConfig, CorsConfig, S3Config, RedisConfig, JwtProperties)
    ├── exception/          (CustomException, ErrorCode, FileException, GlobalExceptionHandler)
    ├── security/
    │   ├── JwtProvider, JwtAuthenticationFilter, JwtAuthenticationEntryPoint
    │   └── oauth2/         (CustomOAuth2User, CustomOAuth2UserService,
    │                        Google/Kakao/NaverOAuth2UserInfo, OAuth2UserInfoFactory,
    │                        OAuth2AuthenticationSuccessHandler)
    ├── service/            (S3Service)
    └── util/               (CodeGenerator, PasswordValidator, TokenHashUtil, NicknameGenerator)
```

## Architecture & Conventions

- **DDD 패키지 구조**: `domain/{module}/{layer}` (controller, service, repository, entity, dto, exception)
- **레이어**: Controller → Service → Repository → Entity
- **DTO**: Java Record 사용, request/response 패키지 분리
- **Entity**: Lombok (`@Getter`, `@Builder`, `@NoArgsConstructor(access = PROTECTED)`)
- **DI**: 생성자 주입 (`@RequiredArgsConstructor`)
- **트랜잭션**: 클래스 레벨 `@Transactional`, 읽기 전용은 `@Transactional(readOnly = true)`
- **Soft Delete**: `deletedAt` 필드 사용 (User, 30일 복구 기간)
- **낙관적 잠금**: `@Version` (User entity)
- **Entity 메서드 패턴**: `create()` static 팩토리 + `update()` 인스턴스 메서드
- **Audit 필드**: `@PrePersist`, `@PreUpdate`로 createdAt/updatedAt 자동 관리

## ApiResponse 공통 응답 규칙

모든 API는 `ApiResponse<T>`로 응답한다. 직접 객체를 반환하거나 다른 래퍼를 사용하지 않는다.

```java
// 구조
ApiResponse<T> {
    ApiStatus status;  // code(int) + message(String)
    T data;            // null 이면 JSON에서 제외 (@JsonInclude(NON_NULL))
}
```

**사용 패턴:**

```java
// 성공 응답 — SuccessCode enum 사용
return ResponseEntity.ok(ApiResponse.ok(SuccessCode.CREATED, responseDto));

// 에러 응답 — ErrorCode 사용 (GlobalExceptionHandler에서 자동 처리)
throw new UserException(UserErrorCode.USER_NOT_FOUND);
```

**SuccessCode**: `global/common/SuccessCode` enum에 정의. 새 성공 코드 추가 시 여기에만 추가.

**Controller 반환 타입**: 항상 `ResponseEntity<ApiResponse<?>>`.

## Exception System

### 도메인별 ErrorCode + Exception 구조

각 도메인은 `{Module}ErrorCode` enum + `{Module}Exception` 클래스 쌍으로 예외를 관리한다.

| 도메인 | ErrorCode enum | Exception 클래스 |
|--------|---------------|-----------------|
| user | `UserErrorCode` | `UserException` |
| email | `EmailErrorCode` | `EmailException` |
| wedding | `WeddingErrorCode` | `WeddingException` |
| guestbook | `GuestbookErrorCode` | `GuestbookException` |
| attendance | `AttendanceErrorCode` | `AttendanceException` |
| feed | `FeedErrorCode` | `FeedException` |
| chat | `ChatErrorCode` | `ChatException` |
| global (파일) | `ErrorCode` (static 상수) | `FileException` |

### HttpStatus 매핑 규칙 (GlobalExceptionHandler.determineHttpStatusFromCode)

ErrorCode enum 이름(code) 패턴에 따라 HTTP 상태가 자동 결정된다:

| 패턴 | HttpStatus | 예시 |
|------|-----------|------|
| `AUTH*` | **401 Unauthorized** | `AUTH_INVALID_CREDENTIALS`, `AUTH_TOKEN_EXPIRED` |
| `UNAUTHORIZED_ACCESS` | **401 Unauthorized** | `UNAUTHORIZED_ACCESS` |
| `INVALID_PASSWORD` 또는 `*UNAUTHORIZED` (AUTH 제외) | **403 Forbidden** | `INVALID_PASSWORD`, `WEDDING_UNAUTHORIZED` |
| `DUPLICATE*` | **409 Conflict** | `DUPLICATE_EMAIL`, `DUPLICATE_NICKNAME` |
| `*NOT_FOUND` | **404 Not Found** | `USER_NOT_FOUND`, `WEDDING_NOT_FOUND` |
| `VALIDATION*` | **400 Bad Request** | `VALIDATION_INVALID_PASSWORD` |
| `*LIMIT_EXCEEDED` | **400 Bad Request** | `ACCOUNT_LIMIT_EXCEEDED`, `IMAGE_LIMIT_EXCEEDED` |
| 나머지 | **400 Bad Request** | `DUPLICATE_ATTENDANCE`, `GEOCODING_FAILED` |

**특수 케이스:**
- `EmailException` → 항상 **500 Internal Server Error** (이메일 발송 실패는 서버 오류)
- `FileException` → `ErrorCode` 정적 상수에 명시된 HttpStatus 직접 사용 (`FILE_EMPTY` → 400, `FILE_UPLOAD_FAILED` → 500)

### 새 ErrorCode 네이밍 규칙

```
{CATEGORY}_{DESCRIPTION}
```

- 패턴에 맞게 이름을 지으면 HttpStatus가 자동 결정됨
- 새 HTTP 상태가 필요하면 `determineHttpStatusFromCode()`에 조건 추가

### GlobalExceptionHandler 등록

새 도메인 Exception 추가 시 반드시 `GlobalExceptionHandler`에 핸들러 메서드 등록:

```java
@ExceptionHandler({Module}Exception.class)
public ResponseEntity<ApiResponse<?>> handle{Module}Exception({Module}Exception e) {
    log.warn("{Module} Exception: {} - {}", e.getErrorCode().name(), e.getMessage());
    HttpStatus status = determineHttpStatusFromCode(e.getErrorCode().name());
    ErrorCode errorCode = ErrorCode.from(e.getErrorCode().name(), e.getMessage(), status);
    return ResponseEntity.status(status).body(ApiResponse.error(errorCode));
}
```

## API Endpoints

### Auth (`/api/auth`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/signup` | 회원가입 |
| POST | `/verify-email` | 이메일 인증 (코드) |
| GET | `/verify-email?token=` | 이메일 인증 (링크) |
| POST | `/resend-verification` | 인증 코드 재발송 |
| POST | `/login` | 로그인 |
| POST | `/refresh` | 토큰 갱신 |
| POST | `/logout` | 로그아웃 |
| POST | `/check-email` | 이메일 중복 확인 |
| POST | `/check-nickname` | 닉네임 중복 확인 |

### User (`/api/users`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/me` | 내 정보 조회 |
| PATCH | `/me` | 프로필 수정 (multipart) |
| PATCH | `/me/password` | 비밀번호 변경 |
| DELETE | `/me` | 회원 탈퇴 |
| POST | `/restore` | 계정 복구 |

### Wedding (`/api/weddings`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/` | 웨딩 생성 |
| GET | `/{id}` | 웨딩 조회 |
| PUT | `/{id}` | 웨딩 수정 |
| DELETE | `/{id}` | 웨딩 삭제 |
| GET | `/{id}/info` | 전체 정보 조회 |

하위 리소스 (`/api/weddings/{id}/...`): couples, schedules, account-groups, accounts, galleries, transportations, accommodations, announcements (각각 CRUD)

## Security

- **JWT**: Access Token(1일) + Refresh Token(7일), HS256
- **JwtAuthenticationFilter**: Bearer 토큰 검증, SecurityContext 설정
- **JwtAuthenticationEntryPoint**: 미인증 시 401 JSON 응답
- **비밀번호**: BCrypt (strength 12)
- **Redis**: RefreshToken SHA-256 해시 저장, 7일 TTL
- **디바이스 제한**: 최대 5대, 초과 시 가장 오래된 토큰 삭제
- **Token Version**: 비밀번호 변경/탈퇴 시 전체 토큰 무효화
- **OAuth2**: Kakao, Naver, Google (CustomOAuth2UserService)
- **CORS**: CorsConfig (기본 localhost:3000)
- **S3**: 프로필 이미지 업로드 (jpeg/jpg/png/webp, 10MB 제한)

## Testing

- **프레임워크**: JUnit 5 + AssertJ + MockMvc
- **설정**: `@SpringBootTest` + `@ActiveProfiles("test")` + `@Transactional`
- **패턴**: Given-When-Then, 한글 `@DisplayName`
- **위치**: `src/test/java/com/soaengry/moment/`
  - `user/controller/` — AuthControllerTest, UserControllerTest
  - `user/service/` — AuthServiceTest, UserServiceTest, EmailServiceTest
  - `wedding/repository/` — 9개 리포지토리 테스트
  - `wedding/service/` — WeddingServiceTest
- **테스트 작성 기준**: 성공 케이스 + 실패 케이스(예외) 필수 작성

## Environment Variables

```env
# Database
DB_USERNAME=
DB_PASSWORD=

# Redis
REDIS_HOST=
REDIS_PORT=
REDIS_USERNAME=
REDIS_PASSWORD=

# JWT
JWT_SECRET=

# Mail
MAIL_HOST=
MAIL_PORT=
MAIL_USERNAME=
MAIL_PASSWORD=

# URLs
FRONTEND_URL=
BASE_URL=

# AWS S3
AWS_ACCESS_KEY=
AWS_SECRET_KEY=
AWS_REGION=

# OAuth2
KAKAO_CLIENT_ID=
KAKAO_CLIENT_SECRET=
NAVER_CLIENT_ID=
NAVER_CLIENT_SECRET=
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
```

## Rules for AI Assistants

### 새 도메인 생성 시 필수 체크리스트

1. `domain/{module}/` 하위에 controller, service, repository, entity, dto/request, dto/response, exception 패키지 생성
2. Entity: `@Entity`, `@Getter`, `@Builder`, `@NoArgsConstructor(access = PROTECTED)` + `create()` static + `update()` 인스턴스 메서드
3. DTO: 반드시 **Java Record** 사용, `dto/request/`와 `dto/response/` 분리
4. Service: `@Service`, `@Transactional` (클래스 레벨), `@RequiredArgsConstructor`
5. Controller: 반환 타입 `ResponseEntity<ApiResponse<?>>` 고정
6. Exception: `{Module}ErrorCode` enum + `{Module}Exception` 클래스 생성
7. **`GlobalExceptionHandler`에 `@ExceptionHandler({Module}Exception.class)` 핸들러 반드시 등록**
8. ErrorCode 이름은 HttpStatus 매핑 패턴에 맞게 작명
9. 로그 메시지는 한국어 사용
10. 테스트: Given-When-Then 패턴 + 한글 `@DisplayName`, 성공/실패 케이스 모두 작성

### 추가 주의사항

- `open-in-view: false` 유지, lazy loading 주의
- `ddl-auto: validate` 모드 — 스키마 변경 시 DB 직접 변경 필요
- 비즈니스 제한은 Service 레이어에서 count 조회 후 검증 (`*_LIMIT_EXCEEDED` 에러 코드 사용)
- 새 DB 선택: MySQL(핵심 데이터), MongoDB(채팅/실시간), Redis(캐시/세션)
