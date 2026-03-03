# Moment Backend - Skills & Recipes

## 새 도메인 모듈 추가

1. `domain/{module}/` 하위에 패키지 생성:
   - `controller/`, `service/`, `repository/`, `entity/`
   - `dto/request/`, `dto/response/`
   - `exception/`
2. Entity: `@Entity`, `@Getter`, `@NoArgsConstructor(access = PROTECTED)`, `@Builder`
   - `create()` static 팩토리 메서드 + `update()` 인스턴스 메서드
   - `@PrePersist`, `@PreUpdate`로 createdAt/updatedAt 관리
3. DTO: Java Record + Jakarta Validation (`@NotBlank`, `@Email`, `@Size` 등)
4. Repository: `JpaRepository<Entity, Long>` 확장
5. Service: `@Service`, `@Transactional`, `@RequiredArgsConstructor`
   - 읽기 전용 메서드는 `@Transactional(readOnly = true)`
6. Controller: `@RestController`, `@RequestMapping("/api/{module}")`
   - 반환 타입 `ResponseEntity<ApiResponse<?>>` 고정
7. Exception: `{Module}ErrorCode` enum + `{Module}Exception` 클래스 생성 (아래 템플릿 참고)
8. **`GlobalExceptionHandler`에 `@ExceptionHandler({Module}Exception.class)` 반드시 추가**

**참고**: 기존 도메인(user, email, wedding) 구조를 참고.

### ErrorCode enum 템플릿

```java
package com.soaengry.moment.domain.{module}.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum {Module}ErrorCode {

    // 네이밍 규칙에 따라 HttpStatus 자동 결정됨 (CLAUDE.md HttpStatus 매핑 표 참고)
    {MODULE}_NOT_FOUND("~을(를) 찾을 수 없습니다"),           // → 404
    DUPLICATE_{MODULE}("이미 존재하는 ~입니다"),               // → 409
    AUTH_{MODULE}_DENIED("~에 대한 인증이 필요합니다"),        // → 401
    {MODULE}_UNAUTHORIZED("~에 대한 권한이 없습니다"),         // → 403
    VALIDATION_{MODULE}_INVALID("~의 입력값이 올바르지 않습니다"), // → 400
    {MODULE}_LIMIT_EXCEEDED("~의 최대 개수를 초과했습니다");   // → 400

    private final String message;
}
```

### Exception 클래스 템플릿

```java
package com.soaengry.moment.domain.{module}.exception;

import lombok.Getter;

@Getter
public class {Module}Exception extends RuntimeException {

    private final {Module}ErrorCode errorCode;

    public {Module}Exception({Module}ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
```

### GlobalExceptionHandler 핸들러 등록 템플릿

```java
// GlobalExceptionHandler.java 에 추가
@ExceptionHandler({Module}Exception.class)
public ResponseEntity<ApiResponse<?>> handle{Module}Exception({Module}Exception e) {
    log.warn("{Module} Exception: {} - {}", e.getErrorCode().name(), e.getMessage());
    HttpStatus status = determineHttpStatusFromCode(e.getErrorCode().name());
    ErrorCode errorCode = ErrorCode.from(e.getErrorCode().name(), e.getMessage(), status);
    return ResponseEntity.status(status).body(ApiResponse.error(errorCode));
}
```

## 새 도메인의 DB 선택
- **MySQL**: 사용자, 게시글, 방명록, 결혼식 정보 등 핵심 데이터
- **MongoDB**: 채팅 메시지, 실시간 데이터
- **Redis**: 알림, 세션 관리, 캐싱

## 새 에러 코드 추가

1. 해당 도메인의 `{Module}ErrorCode` enum에 추가
2. 네이밍 규칙에 따라 HttpStatus 자동 결정:

| 패턴 | HttpStatus |
|------|-----------|
| `AUTH*` | 401 Unauthorized |
| `UNAUTHORIZED_ACCESS` | 401 Unauthorized |
| `INVALID_PASSWORD` 또는 `*UNAUTHORIZED` (AUTH 제외) | 403 Forbidden |
| `DUPLICATE*` | 409 Conflict |
| `*NOT_FOUND` | 404 Not Found |
| `VALIDATION*` | 400 Bad Request |
| `*LIMIT_EXCEEDED` | 400 Bad Request |
| 기타 | 400 Bad Request |

3. 새 HttpStatus 분기가 필요하면 `GlobalExceptionHandler.determineHttpStatusFromCode()`에 조건 추가

## 새 API 엔드포인트 추가

1. Controller에 메서드 추가 (`@PostMapping`, `@GetMapping` 등)
2. Request/Response DTO를 Java Record로 생성
   - Request → `dto/request/`
   - Response → `dto/response/`
3. `@Valid @RequestBody`로 입력 검증
4. Service 메서드 구현
5. `SecurityConfig`에서 인증 필요 여부 설정:
   - 공개: `.requestMatchers("/api/...").permitAll()`
   - 인증 필요: 기본 (`.anyRequest().authenticated()`)
6. 테스트: Service 단위 테스트 + Controller 통합 테스트 작성

## 새 Entity 추가

1. `entity/` 패키지에 JPA Entity 클래스 생성
2. 필수 어노테이션: `@Entity`, `@Table`, `@Getter`, `@NoArgsConstructor(access = PROTECTED)`, `@Builder`
3. `@PrePersist`/`@PreUpdate`로 audit 필드 관리
4. `create()` static 메서드 + `update()` 인스턴스 메서드 패턴 사용
5. `JpaRepository` 인터페이스 생성
6. `ddl-auto: validate`이므로 DB 스키마를 직접 변경해야 함

```java
// Entity 템플릿
@Entity
@Table(name = "examples")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Example {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Example create(String name) {
        return Example.builder().name(name).build();
    }

    public void update(String name) {
        this.name = name;
    }

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}
```

## S3 파일 업로드 추가

1. `S3Service` 참고하여 업로드 메서드 추가
2. 허용 타입: `image/jpeg`, `image/jpg`, `image/png`, `image/webp`
3. 최대 크기: 10MB
4. S3 키 경로: `{purpose}/{uuid}.{ext}` (예: `profiles/uuid.jpg`)
5. Controller에서 `@RequestPart("file") MultipartFile` 수신
6. 에러: `CustomException` + `ErrorCode` (FILE_EMPTY, FILE_SIZE_EXCEEDED, FILE_UNSUPPORTED_FORMAT)

## OAuth2 프로바이더 추가

1. `global/security/oauth2/{Provider}OAuth2UserInfo.java` 생성 (OAuth2UserInfo 구현)
2. `OAuth2UserInfoFactory`에 case 추가
3. `application-dev.yml`에 provider 설정 추가 (client-id, client-secret, scope, endpoints)
4. `SecurityConfig`의 oauth2Login 설정은 자동 적용

## Redis 기반 기능 추가

1. `RefreshTokenRepository` 패턴 참고
2. Key 네이밍: `{feature}:{id}:{sub_id}`
3. TTL 설정 필수 (TimeUnit.DAYS, TimeUnit.HOURS 등)
4. 민감 데이터는 `TokenHashUtil.hashToken()`으로 SHA-256 해싱
5. `StringRedisTemplate` 사용

## 비즈니스 제한 추가

1. `{Module}ErrorCode`에 `*_LIMIT_EXCEEDED` 에러 코드 정의
2. Service에서 count 조회 후 제한 검증
3. 참고 사례:
   - AccountGroup: 웨딩당 최대 4개
   - Account: 그룹당 최대 3개
   - Device: 사용자당 최대 5대

## 테스트 작성

```java
// 서비스 테스트 템플릿
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ExampleServiceTest {

    @Autowired
    private ExampleService exampleService;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
    }

    @Test
    @DisplayName("예시 생성 성공")
    void create_Success() {
        // given
        ExampleRequest request = new ExampleRequest("name");

        // when
        ExampleResponse response = exampleService.create(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("name");
    }

    @Test
    @DisplayName("중복 시 생성 실패")
    void create_Fail_Duplicate() {
        // given
        // ...setup duplicate...

        // when & then
        assertThatThrownBy(() -> exampleService.create(request))
            .isInstanceOf(ExampleException.class);
    }
}
```
