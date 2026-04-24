# Moment — Frontend

React 19 + TypeScript 기반 Moment 플랫폼의 SPA 클라이언트입니다.  
DDD(Domain-Driven Design) 구조로 설계되었으며, Zustand 상태 관리, Axios 인터셉터 기반 자동 토큰 갱신, WebSocket 채팅을 지원합니다.

![React](https://img.shields.io/badge/React_19-61DAFB?style=flat-square&logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript_5.9-3178C6?style=flat-square&logo=typescript&logoColor=white)
![Vite](https://img.shields.io/badge/Vite_7-646CFF?style=flat-square&logo=vite&logoColor=white)
![React Router](https://img.shields.io/badge/React_Router_7-CA4245?style=flat-square&logo=reactrouter&logoColor=white)
![Zustand](https://img.shields.io/badge/Zustand_5-433E38?style=flat-square&logo=zustand&logoColor=white)
![React Hook Form](https://img.shields.io/badge/React_Hook_Form_7-EC5990?style=flat-square&logo=reacthookform&logoColor=white)
![Zod](https://img.shields.io/badge/Zod_4-3E67B1?style=flat-square&logo=zod&logoColor=white)
![Axios](https://img.shields.io/badge/Axios_1-5A29E4?style=flat-square&logo=axios&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS_3-06B6D4?style=flat-square&logo=tailwindcss&logoColor=white)
![Framer Motion](https://img.shields.io/badge/Framer_Motion_12-0055FF?style=flat-square&logo=framer&logoColor=white)
![ESLint](https://img.shields.io/badge/ESLint_9-4B32C3?style=flat-square&logo=eslint&logoColor=white)

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Framework | React 19 |
| Language | TypeScript 5.9 |
| Build | Vite 7 |
| Routing | React Router 7 |
| State | Zustand 5 |
| Forms | React Hook Form 7 + Zod 4 |
| HTTP | Axios 1 (인터셉터 기반 자동 토큰 갱신) |
| Styling | Tailwind CSS 3 |
| Animations | Framer Motion 12 |
| WebSocket | SockJS + STOMP 7 |
| Toast | React Toastify 11 |
| Icons | React Icons 5 |
| 주소 검색 | React Daum Postcode 3 |
| Lint | ESLint 9 |

---

## 프로젝트 구조

```
frontend/src/
├── main.tsx                    # 앱 진입점
├── index.css                   # Tailwind 전역 스타일
│
├── app/
│   ├── App.tsx                 # 토큰 복구 및 앱 초기화
│   └── routes/
│       ├── AppRouter.tsx       # 라우트 정의 (React Router v7)
│       └── ProtectedRoute.tsx  # 인증 보호 라우트
│
├── domain/                     # 기능 모듈 (DDD)
│   ├── auth/                   # 로그인, 회원가입, OAuth2 콜백
│   ├── user/                   # 마이페이지, 프로필 수정, 회원 탈퇴
│   ├── event/                  # 이벤트 조회, 생성, 수정
│   ├── attendance/             # 내 일정 (RSVP)
│   ├── feed/                   # 글로벌 피드
│   ├── chat/                   # 실시간 채팅
│   └── guestbook/              # 방명록
│
└── global/                     # 공통 모듈
    ├── api/
    │   └── axiosInstance.ts    # Axios + 토큰 자동 갱신 인터셉터
    ├── components/             # Layout, Header, BottomNav 등 공통 UI
    ├── config/
    │   └── env.ts              # 환경변수 래퍼
    ├── pages/
    │   └── HomePage.tsx
    ├── hooks/                  # 공통 커스텀 훅
    ├── types/                  # 공통 타입 정의
    ├── constants/              # 공통 상수
    └── utils/                  # 유틸리티 함수
```

각 도메인은 `pages / api / store / types / constants` 구조로 구성됩니다.  
도메인의 공개 인터페이스는 `index.ts` 배럴 익스포트로 노출합니다.

---

## 설치 및 실행

### 사전 요구사항

- Node.js 18+

### 의존성 설치

```bash
cd frontend
npm install
```

### 환경변수 설정

`.env.local` 파일을 생성합니다.

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

프로덕션 빌드에는 `.env.production` 파일을 사용합니다.

```env
VITE_API_BASE_URL=https://api.your-domain.com/api
```

### 개발 서버 실행

```bash
npm run dev
```

`http://localhost:3000` 에서 확인할 수 있습니다.  
`/api` 경로는 `http://localhost:8080` 으로 프록시됩니다.

---

## 주요 명령어

```bash
npm run dev       # 개발 서버 실행 (포트 3000)
npm run build     # 프로덕션 빌드 (tsc -b && vite build)
npm run preview   # 빌드 결과물 미리보기
npm run lint      # ESLint 검사
```

---

## 페이지 구조

### 공개 라우트 (비로그인 접근 가능)

| 경로 | 페이지 |
|------|--------|
| `/` | 홈페이지 |
| `/login` | 로그인 |
| `/signup` | 회원가입 |
| `/verify-email` | 이메일 인증 |
| `/restore-account` | 계정 복구 |
| `/oauth2/callback` | OAuth2 콜백 (Kakao / Naver / Google) |
| `/event/:slug` | 이벤트 상세 (읽기 전용) |
| `/event/:slug/feed` | 이벤트 피드 |
| `/event/:slug/guestbook` | 방명록 |

### 보호 라우트 (로그인 필요)

| 경로 | 페이지 |
|------|--------|
| `/event/create` | 이벤트 생성 |
| `/event/:slug/edit` | 이벤트 수정 |
| `/event/:slug/chat` | 실시간 채팅 |
| `/feed` | 글로벌 피드 |
| `/my-schedule` | 내 일정 (RSVP) |
| `/my-page` | 마이페이지 |
| `/my-page/posts` | 내 게시물 |
| `/my-page/bookmarks` | 북마크 |
| `/my-page/likes` | 좋아요 |
| `/my-page/comments` | 내 댓글 |
| `/my-page/past-schedules` | 지난 일정 |
| `/edit-profile` | 프로필 수정 |
| `/delete-account` | 회원 탈퇴 |

---

## 인증 흐름

### 토큰 자동 갱신

`global/api/axiosInstance.ts`의 Axios 인터셉터가 401 응답 시 자동으로 토큰을 갱신합니다.  
동시 요청이 발생한 경우 갱신이 완료될 때까지 큐에 보관했다가 일괄 처리합니다.

```ts
// 모든 API 요청에 axiosInstance를 사용하면 자동으로 처리됩니다.
import axiosInstance from '@/global/api/axiosInstance';

const response = await axiosInstance.get('/users/me');
```

### OAuth2 소셜 로그인

백엔드 OAuth2 엔드포인트로 리디렉트한 뒤 콜백 페이지에서 토큰을 처리합니다.

```ts
// auth.constants.ts에 정의된 상수 사용
window.location.href = `${BASE_URL}/oauth2/authorization/kakao`;
```

---

## 상태 관리

Zustand를 사용합니다. 각 도메인의 `store/` 디렉토리에 스토어가 위치합니다.

```ts
// 예시: 인증 상태
import { useAuthStore } from '@/domain/auth';

const { user, isAuthenticated, logout } = useAuthStore();
```

---

## 폼 처리

React Hook Form + Zod로 타입-safe 폼 검증을 구현합니다.

```tsx
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

const schema = z.object({
  email: z.string().email('올바른 이메일을 입력해주세요.'),
  password: z.string().min(8, '비밀번호는 8자 이상이어야 합니다.'),
});

type FormData = z.infer<typeof schema>;

const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
  resolver: zodResolver(schema),
});
```

---

## 디자인 시스템

### 색상 (Tailwind 커스텀)

| 토큰 | 값 | 용도 |
|------|----|------|
| `primary` | `#75bd28` | 메인 버튼, 강조 색상 |
| `primaryHover` | `#5f9920` | 버튼 hover 상태 |
| `bgPrimary` | `#FAFFF4` | 배경 색상 |
| `gold` | `#F0C434` | 포인트 색상 |
| `rose` | `#E6A5A5` | 웨딩 포인트 색상 |
| `success` | `#16A34A` | 성공 메시지 |
| `error` | `#FD5B5B` | 오류 메시지 |

### 폰트

Pretendard Variable (한국어 최적화 폰트, CDN으로 로드)

### 애니메이션 클래스

```
animate-slide-up       # 위로 슬라이드 (0.3s)
animate-fade-in        # 페이드인 (0.5s)
animate-slide-in-right # 우측에서 슬라이드 (0.4s)
animate-bounce-in      # 바운스 효과 (0.5s)
```

---

## 빌드 및 배포

```bash
# 프로덕션 빌드
npm run build

# dist/ 폴더가 생성됩니다.
```

### Vercel 배포

`vercel.json`이 이미 설정되어 있어 바로 배포할 수 있습니다.  
모든 경로를 `index.html`로 rewrite해 SPA 라우팅을 지원합니다.

```bash
vercel --prod
```

### 정적 호스팅 (Nginx 예시)

```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

---

## Lint 규칙

커밋 전 반드시 통과해야 합니다.

```bash
npm run lint
```

- 미사용 변수 금지 (`no-unused-vars`)
- React Hooks 규칙 준수
- TypeScript strict 모드
