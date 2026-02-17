# Moment Backend

Spring Boot 3.5.10 기반 웨딩 초대장 플랫폼 REST API.
Java 17, Gradle, MySQL, Redis, AWS S3.

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
│   └── wedding/            # 웨딩 초대장 (핵심 도메인)
│       ├── controller/     (WeddingController)
│       ├── dto/
│       │   ├── request/    (WeddingRequest, CoupleRequest, ScheduleRequest, etc.)
│       │   └── response/   (WeddingResponse, WeddingInfoResponse, CoupleResponse, etc.)
│       ├── entity/         (Wedding, Couple, Schedule, AccountGroup, Account,
│       │                    Gallery, Transportation, Accommodation, Announcement)
│       ├── exception/      (WeddingErrorCode, WeddingException)
│       ├── repository/     (9개 리포지토리)
│       └── service/        (WeddingService)
│
└── global/
    ├── common/             (ApiResponse)
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

## Exception System

각 도메인은 자체 ErrorCode enum + Exception 클래스를 가짐:

| 도메인 | ErrorCode | Exception | 코드 예시 |
|--------|-----------|-----------|-----------|
| user | `UserErrorCode` | `UserException` | AUTH_INVALID_CREDENTIALS, DUPLICATE_EMAIL, USER_NOT_FOUND |
| email | `EmailErrorCode` | `EmailException` | EMAIL_SEND_FAILED |
| wedding | `WeddingErrorCode` | `WeddingException` | WEDDING_NOT_FOUND, ACCOUNT_LIMIT_EXCEEDED |
| global | `ErrorCode` | `CustomException`, `FileException` | FILE_UPLOAD_FAILED, FILE_SIZE_EXCEEDED |

**HTTP 상태 매핑** (GlobalExceptionHandler):
- `AUTH*` → 401 Unauthorized
- `DUPLICATE*` → 409 Conflict
- `*NOT_FOUND` → 404 Not Found
- `VALIDATION*`, `*LIMIT_EXCEEDED` → 400 Bad Request
- 기타 → 500 Internal Server Error

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

- 새 도메인 모듈 추가 시 `domain/{module}/` 하위 구조를 따를 것
- DTO는 반드시 Java Record 사용, `dto/request/`와 `dto/response/` 분리
- 도메인별 ErrorCode enum + Exception 클래스 쌍으로 생성
- 로그 메시지는 한국어 사용
- 테스트는 Given-When-Then 패턴 + 한글 `@DisplayName`
- `open-in-view: false` 유지, lazy loading 주의
- `ddl-auto: validate` 모드 — 스키마 변경 시 DB 직접 변경 필요
- Entity에 `create()` static 메서드 + `update()` 인스턴스 메서드 패턴 사용
- 비즈니스 제한은 Service 레이어에서 count 조회 후 검증
