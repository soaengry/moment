# Moment — Backend

Spring Boot 기반 Moment 플랫폼의 REST API 서버입니다.  
DDD(Domain-Driven Design) 구조로 설계되었으며, JWT + OAuth2 인증, WebSocket 채팅, AWS S3 파일 업로드를 지원합니다.

![Java](https://img.shields.io/badge/Java_17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL_8-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=flat-square&logo=mongodb&logoColor=white)
![AWS S3](https://img.shields.io/badge/AWS_S3-569A31?style=flat-square&logo=amazons3&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=flat-square&logo=jsonwebtokens&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket_STOMP-010101?style=flat-square&logo=socket.io&logoColor=white)
![JUnit5](https://img.shields.io/badge/JUnit_5-25A162?style=flat-square&logo=junit5&logoColor=white)

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.5 |
| Build | Gradle |
| ORM | Spring Data JPA (Hibernate) |
| Database | MySQL 8 (HikariCP 커넥션 풀) |
| Cache / Session | Redis (Lettuce) |
| Document DB | MongoDB (채팅 전용) |
| File Storage | AWS S3 (`spring-cloud-aws 3.2`) |
| Auth | JWT (HS256), OAuth2 (Kakao / Naver / Google) |
| WebSocket | Spring WebSocket + STOMP |
| Testing | JUnit 5, MockMvc |

---

## 프로젝트 구조

```
backend/src/main/java/com/soaengry/moment/
├── MomentApplication.java
│
├── domain/                     # 도메인 모듈 (DDD)
│   ├── user/                   # 회원 가입, 로그인, 프로필
│   ├── email/                  # 이메일 인증 및 발송
│   ├── event/                  # 웨딩 이벤트 (핵심 도메인)
│   ├── wedding/                # 웨딩 상세 정보
│   ├── guestbook/              # 방명록
│   ├── attendance/             # 참석 확인 (RSVP)
│   ├── chat/                   # 실시간 채팅 (MongoDB)
│   ├── feed/                   # 이벤트 피드
│   └── bank/                   # 은행 계좌 검증
│
└── global/                     # 공통 모듈
    ├── common/                 # ApiResponse, SuccessCode, BaseTimeEntity
    ├── config/                 # Security, Redis, S3, JWT, CORS 설정
    ├── controller/             # FileController, HealthCheckController
    ├── exception/              # CustomException, ErrorCode, GlobalExceptionHandler
    ├── security/               # JWT 필터, 인증 엔트리포인트
    ├── service/                # S3Service
    └── util/                   # CodeGen, NicknameGen, TokenHash 등
```

각 도메인은 `controller / service / repository / entity / dto / exception` 레이어로 구성됩니다.

---

## 설치 및 실행

### 사전 요구사항

- Java 17+
- MySQL 8.0+
- Redis 7+
- MongoDB 6+

### 환경변수 설정

프로젝트 루트(`backend/`)에 `.env` 파일을 생성합니다.

```env
# Database
MYSQL_URL=jdbc:mysql://localhost:3306/moment
MYSQL_USERNAME=username
MYSQL_PASSWORD=your_password

# MongoDB (채팅)
MONGODB_URI=mongodb://localhost:27017
MONGODB_DATABASE=moment

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_USERNAME=
REDIS_PASSWORD=

# JWT (최소 256-bit 시크릿)
JWT_SECRET=your_jwt_secret_key_at_least_32_characters

# Mail (Gmail 예시)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password

# AWS S3
AWS_BUCKET=your-s3-bucket
AWS_ACCESS_KEY=your_access_key
AWS_SECRET_KEY=your_secret_key
AWS_REGION=ap-northeast-2

# OAuth2
KAKAO_CLIENT_ID=...
KAKAO_CLIENT_SECRET=...
KAKAO_REST_API_KEY=...
NAVER_CLIENT_ID=...
NAVER_CLIENT_SECRET=...
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...

# URL
BASE_URL=http://localhost:8080
FRONT_URL=http://localhost:3000
```

### 데이터베이스 초기화

```sql
-- MySQL에서 데이터베이스 생성
CREATE DATABASE moment CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

JPA `ddl-auto: update` 설정으로 애플리케이션 시작 시 테이블이 자동 생성됩니다.  
초기 은행 데이터를 삽입하려면:

**로컬 환경**
```bash
mysql -u root -p moment < src/main/resources/db/seed_banks.sql
```

**EC2 Docker 환경** (백엔드·MySQL 컨테이너가 분리된 경우)

SQL 파일은 백엔드 컨테이너 JAR 안에 번들되어 있으므로 호스트에서 직접 접근이 불가합니다.

```bash
# 방법 1 — sh로 JAR에서 직접 추출 후 MySQL 컨테이너에 파이프
docker exec moment-dev sh -c \
  "unzip -p /app.jar BOOT-INF/classes/db/seed_banks.sql" \
  | docker exec -i moment-mysql-dev \
    mysql -u root -p<비밀번호> moment_dev

# 방법 2 — JAR를 호스트로 복사 후 추출 (unzip이 컨테이너에 없는 경우)
docker cp moment-dev:/app.jar /tmp/app.jar
unzip -p /tmp/app.jar BOOT-INF/classes/db/seed_banks.sql > /tmp/seed_banks.sql
docker exec -i moment-mysql-dev mysql -u root -p<비밀번호> moment_dev < /tmp/seed_banks.sql
```

> MySQL 비밀번호 확인: `docker exec moment-mysql-dev printenv MYSQL_ROOT_PASSWORD`

개발 환경에서 전체 스키마를 초기화하려면:

**로컬 환경**
```bash
mysql -u root -p moment < src/main/resources/db/reset_schema.sql
```

**EC2 Docker 환경**
```bash
docker cp moment-dev:/app.jar /tmp/app.jar
unzip -p /tmp/app.jar BOOT-INF/classes/db/reset_schema.sql > /tmp/reset_schema.sql
docker exec -i moment-mysql-dev mysql -u root -p<비밀번호> moment_dev < /tmp/reset_schema.sql
```

### 빌드 및 실행

```bash
# 로컬 실행 (기본 프로필)
./gradlew bootRun

# 특정 프로필로 실행
./gradlew bootRun --args='--spring.profiles.active=dev'

# 프로덕션 빌드
./gradlew clean build

# 테스트 실행
./gradlew test

# 테스트 제외 빌드
./gradlew build -x test
```

---

## API 응답 형식

모든 API는 공통 래퍼로 응답합니다.

```json
// 성공
{
  "success": true,
  "code": "USER_001",
  "message": "회원가입이 완료되었습니다.",
  "data": { ... }
}

// 실패
{
  "success": false,
  "code": "AUTH_001",
  "message": "이메일 또는 비밀번호가 올바르지 않습니다.",
  "data": null
}
```

---

## 인증 흐름

### JWT 인증

```
POST /api/auth/login
  → Access Token + Refresh Token 발급

POST /api/auth/refresh
  → Refresh Token으로 Access Token 갱신

POST /api/auth/logout
  → Refresh Token 무효화 (Redis에서 삭제)
```

### OAuth2 소셜 로그인

```
GET /oauth2/authorization/kakao    # Kakao 로그인 시작
GET /oauth2/authorization/naver    # Naver 로그인 시작
GET /oauth2/authorization/google   # Google 로그인 시작

→ 콜백 후 프론트엔드로 토큰과 함께 리디렉트
```

---

## 주요 도메인 API

| 도메인 | 경로 | 설명 |
|--------|------|------|
| 인증 | `/api/auth/**` | 로그인, 회원가입, 토큰 갱신 |
| 사용자 | `/api/users/**` | 프로필 조회/수정, 탈퇴 |
| 이벤트 | `/api/events/**` | 이벤트 CRUD |
| 방명록 | `/api/events/{slug}/guestbook/**` | 방명록 CRUD |
| 참석 | `/api/events/{slug}/attendance/**` | RSVP 관리 |
| 채팅 | `/ws/**` (WebSocket) | 실시간 채팅 |
| 피드 | `/api/feed/**` | 피드 조회 |
| 파일 | `/api/files/**` | S3 이미지 업로드 |

---

## 프로파일

| 프로파일 | 용도 | 포트 |
|----------|------|------|
| `local` | 로컬 개발 | 8080 |
| `dev` | 개발 서버 | 8080 |
| `blue` | 프로덕션 Blue | 8080 |
| `green` | 프로덕션 Green | 8081 |

---

## Docker

```bash
# 이미지 빌드
docker build --build-arg PROFILES=dev --build-arg ENV=dev -t moment-backend .

# 컨테이너 실행
docker run -p 8080:8080 --env-file .env moment-backend
```

---

## 테스트

테스트는 `src/test/` 아래에 도메인별로 구성되어 있습니다.

```bash
# 전체 테스트
./gradlew test

# 특정 테스트 클래스
./gradlew test --tests "com.soaengry.moment.domain.user.*"
```

Given-When-Then 패턴과 `@SpringBootTest` + `MockMvc`를 사용합니다.
