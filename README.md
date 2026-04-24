# Moment

> 소중한 순간을 함께하는 일정 초대장 플랫폼

![Java](https://img.shields.io/badge/Java_17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React_19-61DAFB?style=flat-square&logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=flat-square&logo=typescript&logoColor=white)
![Vite](https://img.shields.io/badge/Vite_7-646CFF?style=flat-square&logo=vite&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=flat-square&logo=mongodb&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white)
![AWS S3](https://img.shields.io/badge/AWS_S3-569A31?style=flat-square&logo=amazons3&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx-009639?style=flat-square&logo=nginx&logoColor=white)

---

## 주요 기능

- **이벤트 관리** — 초대장(이벤트)을 생성하고 상세 정보를 편집
- **방명록** — 하객들이 축하 메시지를 남길 수 있는 게스트북 (청첩장 전용)
- **참석 확인 (RSVP)** — 하객이 참석 여부를 선택하고 일정 관리 (청첩장 전용)
- **실시간 채팅** — WebSocket 기반 이벤트 채팅방
- **피드** — 이벤트 활동 스트림 및 글로벌 피드
- **소셜 로그인** — Kakao, Naver, Google OAuth2 지원
- **파일 업로드** — AWS S3를 이용한 이미지 업로드

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| Backend | Java 17, Spring Boot 3.5, Gradle |
| Frontend | React 19, TypeScript, Vite |
| Database | MySQL, MongoDB (채팅), Redis (세션/캐시) |
| Storage | AWS S3 |
| Auth | JWT, OAuth2 (Kakao / Naver / Google) |
| Infra | Docker, GitHub Actions, Nginx (Blue-Green 배포) |

---

## 프로젝트 구조

```
moment/
├── backend/          # Spring Boot API 서버
├── frontend/         # React SPA 클라이언트
├── .github/
│   └── workflows/
│       └── CICD.yml  # CI/CD 파이프라인
└── README.md
```

---

## 빠른 시작

### 사전 요구사항

- Java 17+
- Node.js 18+
- MySQL 8.0+
- Redis 7+
- MongoDB 6+

### 실행 순서

```bash
# 1. 저장소 클론
git clone https://github.com/soaengry/moment.git
cd moment

# 2. 백엔드 실행
cd backend
cp .env.example .env   # 환경변수 설정
./gradlew bootRun

# 3. 프론트엔드 실행 (새 터미널)
cd frontend
npm install
npm run dev
```

백엔드: `http://localhost:8080`  
프론트엔드: `http://localhost:3000`

---

## 세부 문서

- [Backend README](./backend/README.md) — API 서버 설치, 환경변수, 아키텍처
- [Frontend README](./frontend/README.md) — 클라이언트 설치, 구조, 배포

---

## 브랜치 전략

| 브랜치 | 용도 |
|--------|------|
| `main` | 프로덕션 배포 (Blue-Green) |
| `dev` | 개발 서버 배포 |
| `feat/*` | 기능 개발 |
| `fix/*` | 버그 수정 |
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
