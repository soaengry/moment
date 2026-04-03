# Moment — 웨딩 초대장 플랫폼

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.5.10-6DB33F?style=flat-square&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/React-19-61DAFB?style=flat-square&logo=react&logoColor=black" />
  <img src="https://img.shields.io/badge/TypeScript-5.9-3178C6?style=flat-square&logo=typescript&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL-8-4479A1?style=flat-square&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Redis-7-DC382D?style=flat-square&logo=redis&logoColor=white" />
  <img src="https://img.shields.io/badge/AWS_S3-Storage-FF9900?style=flat-square&logo=amazons3&logoColor=white" />
  <img src="https://img.shields.io/github/license/soaengry/moment?style=flat-square" />
</p>

<p align="center">
  온라인 웨딩 초대장을 쉽고 아름답게 만들고 공유하는 플랫폼입니다.<br/>
  커플 정보·일정·갤러리 관리부터 실시간 채팅·방명록까지 한 곳에서.
</p>

---

## 목차

- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [아키텍처](#아키텍처)
- [시작하기](#시작하기)
- [환경 변수](#환경-변수)
- [프로젝트 구조](#프로젝트-구조)
- [기여 가이드](#기여-가이드)

---

## 주요 기능

| 기능 | 설명 |
|------|------|
| **웨딩 초대장** | 커플 정보, 일정, 오시는 길, 갤러리, 계좌 정보를 담은 디지털 청첩장 |
| **방명록** | 비밀번호 보호 방명록 — 하객이 축하 메시지 작성 |
| **실시간 채팅** | WebSocket(STOMP) 기반 웨딩 채팅방 |
| **피드** | 웨딩 관련 게시글, 댓글, 좋아요, 북마크 |
| **소셜 로그인** | Kakao · Naver · Google OAuth2 지원 |
| **이메일 인증** | 회원가입 및 비밀번호 재설정 이메일 인증 |
| **파일 업로드** | AWS S3 이미지 업로드 (갤러리, 프로필) |
| **계좌 인식** | 계좌번호 자동 감지 기능 |

---

## 기술 스택

### Backend
| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.5.10 |
| Database | MySQL, MongoDB (채팅) |
| Cache/Session | Redis |
| Auth | JWT (HS256) + OAuth2 |
| Storage | AWS S3 |
| Real-time | WebSocket + STOMP |

### Frontend
| 분류 | 기술 |
|------|------|
| Framework | React 19 + TypeScript 5.9 |
| Build | Vite 7 |
| State | Zustand 5 |
| HTTP | Axios 1.13 (자동 토큰 갱신) |
| Form | React Hook Form 7 + Zod 4 |
| Style | Tailwind CSS 3.4 |

---

## 아키텍처

```
moment/
├── backend/    # Spring Boot REST API (DDD 패키지 구조)
└── frontend/   # React 19 SPA (도메인 주도 구조)
```

두 서비스는 독립적으로 실행되며, 프론트엔드 개발 서버의 `/api` 프록시로 통신합니다.

---

## 시작하기

### 사전 요구사항

- Java 17+
- Node.js 20+
- MySQL 8+
- Redis 7+
- MongoDB 6+ (채팅 기능 사용 시)

### 1. 저장소 클론

```bash
git clone https://github.com/soaengry/moment.git
cd moment
```

### 2. Backend 실행

```bash
cd backend
cp .env.example .env      # 환경 변수 설정
./gradlew bootRun
# → http://localhost:8080
```

자세한 설정은 [backend/README.md](./backend/README.md)를 참고하세요.

### 3. Frontend 실행

```bash
cd frontend
cp .env.example .env      # 환경 변수 설정
npm install
npm run dev
# → http://localhost:3000
```

자세한 설정은 [frontend/README.md](./frontend/README.md)를 참고하세요.

---

## 환경 변수

| 위치 | 파일 | 문서 |
|------|------|------|
| Backend | `backend/.env` | [backend/README.md#환경-변수](./backend/README.md#환경-변수) |
| Frontend | `frontend/.env` | [frontend/README.md#환경-변수](./frontend/README.md#환경-변수) |

---

## 프로젝트 구조

```
moment/
├── backend/
│   └── src/main/java/com/soaengry/moment/
│       ├── domain/          # 비즈니스 도메인 (DDD)
│       │   ├── user/        # 인증 · 회원 관리
│       │   ├── wedding/     # 웨딩 초대장 (핵심)
│       │   ├── guestbook/   # 방명록
│       │   ├── attendance/  # 참석 관리
│       │   ├── chat/        # 실시간 채팅
│       │   ├── feed/        # 피드 · 게시글
│       │   ├── email/       # 이메일 인증
│       │   └── bank/        # 계좌 정보
│       └── global/          # 공통 설정 · 예외 · 보안
└── frontend/
    └── src/
        ├── app/             # 앱 초기화 · 라우터
        ├── domain/          # 도메인별 페이지 · API · 스토어
        └── global/          # 공통 컴포넌트 · Axios 인스턴스
```

---

## 기여 가이드

### 브랜치 전략

| 브랜치 | 용도 |
|--------|------|
| `main` | 프로덕션 배포 브랜치 |
| `dev` | 개발 통합 브랜치 |
| `feat/<name>` | 신규 기능 |
| `fix/<name>` | 버그 수정 |
| `refactor/<name>` | 리팩토링 |

### 커밋 메시지 규칙

```
<type>: <subject>

타입 목록:
  feat     새로운 기능
  fix      버그 수정
  refactor 코드 리팩토링
  test     테스트 추가/수정
  docs     문서 수정
  chore    빌드 · 설정 변경
```

예시:
```
feat: 웨딩 갤러리 이미지 다중 업로드 기능 추가
fix: 토큰 갱신 중 동시 요청 처리 오류 수정
```

### PR 절차

1. `dev` 브랜치에서 작업 브랜치 생성
2. 기능 구현 및 테스트 작성
3. PR 생성 → `dev` 대상
4. 코드 리뷰 후 머지

### 코딩 컨벤션

- Backend: [backend/README.md#개발-컨벤션](./backend/README.md#개발-컨벤션) 참고
- Frontend: [frontend/README.md#개발-컨벤션](./frontend/README.md#개발-컨벤션) 참고

---

## 라이선스

이 프로젝트는 [MIT License](./LICENSE) 하에 배포됩니다.
