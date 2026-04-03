# Moment Frontend

<p align="left">
  <img src="https://img.shields.io/badge/React-19-61DAFB?style=flat-square&logo=react&logoColor=black" />
  <img src="https://img.shields.io/badge/TypeScript-5.9-3178C6?style=flat-square&logo=typescript&logoColor=white" />
  <img src="https://img.shields.io/badge/Vite-7-646CFF?style=flat-square&logo=vite&logoColor=white" />
  <img src="https://img.shields.io/badge/Zustand-5-433e38?style=flat-square" />
  <img src="https://img.shields.io/badge/Tailwind_CSS-3.4-06B6D4?style=flat-square&logo=tailwindcss&logoColor=white" />
  <img src="https://img.shields.io/badge/React_Router-7-CA4245?style=flat-square&logo=reactrouter&logoColor=white" />
  <img src="https://img.shields.io/badge/Axios-1.13-5A29E4?style=flat-square&logo=axios&logoColor=white" />
</p>

React 19 + TypeScript + Vite 기반 웨딩 초대장 플랫폼 SPA.

---

## 목차

- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [설치 및 실행](#설치-및-실행)
- [환경 변수](#환경-변수)
- [라우트 구조](#라우트-구조)
- [상태 관리](#상태-관리)
- [토큰 관리](#토큰-관리)
- [개발 컨벤션](#개발-컨벤션)

---

## 기술 스택

| 분류 | 라이브러리 | 버전 | 용도 |
|------|-----------|------|------|
| UI | React | 19.2.0 | 컴포넌트 |
| 언어 | TypeScript | 5.9.3 | 타입 안전성 |
| 빌드 | Vite | 7 | 번들러 + 개발 서버 |
| 상태관리 | Zustand | 5.0.11 | 글로벌 상태 (인증) |
| 폼 | React Hook Form | 7 | 폼 관리 |
| 폼 검증 | Zod | 4 | 스키마 검증 |
| HTTP | Axios | 1.13.5 | API 호출 + 인터셉터 |
| 라우팅 | React Router | 7 | SPA 라우팅 |
| 스타일링 | Tailwind CSS | 3.4 | 유틸리티 CSS |
| 알림 | React Toastify | 11 | 토스트 메시지 |
| 실시간 | STOMP + SockJS | 7.3.0 | WebSocket 채팅 |
| 린터 | ESLint | 9 | 코드 품질 |

---

## 프로젝트 구조

```
src/
├── main.tsx                          # 진입점
├── index.css                         # Tailwind 디렉티브 + 글로벌 CSS
│
├── app/
│   ├── App.tsx                       # 앱 초기화 (토큰 복원, 로딩 상태)
│   └── routes/
│       ├── AppRouter.tsx             # 전체 라우트 정의
│       └── ProtectedRoute.tsx        # 인증 가드 (미인증 → /login)
│
├── domain/
│   ├── auth/
│   │   ├── api/authApi.ts
│   │   ├── store/useAuthStore.ts
│   │   ├── components/
│   │   ├── pages/                    # LoginPage, SignUpPage 등
│   │   ├── auth.constants.ts
│   │   ├── auth.utils.ts
│   │   ├── types.ts
│   │   └── index.ts                  # barrel export
│   ├── user/                         # 프로필 · 계정 관리
│   ├── wedding/                      # 초대장 생성 · 수정 · 조회
│   ├── feed/                         # 게시글 · 댓글 · 좋아요
│   ├── chat/                         # 실시간 채팅 (WebSocket)
│   ├── guestbook/                    # 방명록
│   └── schedule/                     # 일정 관리
│
└── global/
    ├── api/axiosInstance.ts          # Axios 인터셉터 (토큰 자동 갱신)
    ├── components/
    │   ├── Header.tsx
    │   ├── Layout.tsx
    │   └── BottomNav.tsx
    ├── config/env.ts                 # 환경 변수 관리
    ├── constants/
    ├── hooks/
    ├── pages/
    │   └── HomePage.tsx
    └── types/
```

---

## 설치 및 실행

### 사전 요구사항

- Node.js 20+
- npm 10+

### 1. 의존성 설치

```bash
cd frontend
npm install
```

### 2. 환경 변수 설정

```bash
cp .env.example .env
# .env 파일을 열어 값 설정
```

### 3. 개발 서버 실행

```bash
npm run dev
# → http://localhost:3000
# /api 요청은 http://localhost:8080 으로 프록시됨
```

### 기타 명령어

```bash
npm run build     # 프로덕션 빌드 (tsc -b && vite build)
npm run lint      # ESLint 검사
npm run preview   # 빌드 결과 미리보기
```

---

## 환경 변수

`frontend/.env` 파일에 아래 값들을 설정합니다.

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_OAUTH2_BASE_URL=/oauth2/authorization
```

환경 변수는 `src/global/config/env.ts`의 `ENV` 객체로 관리됩니다:

```typescript
export const ENV = {
  API_BASE_URL: import.meta.env.VITE_API_BASE_URL,
  OAUTH2_BASE_URL: import.meta.env.VITE_OAUTH2_BASE_URL,
};
```

> 새 환경 변수 추가 시 반드시 `env.ts`의 `ENV` 객체에도 등록하세요.

---

## 라우트 구조

### Public (비로그인 접근 가능)

| 경로 | 페이지 | 설명 |
|------|--------|------|
| `/` | HomePage | 메인 페이지 |
| `/login` | LoginPage | 로그인 |
| `/signup` | SignUpPage | 회원가입 |
| `/verify-email` | VerifyEmailPage | 이메일 인증 |
| `/restore-account` | RestoreAccountPage | 탈퇴 계정 복구 |
| `/oauth2/callback` | OAuth2CallbackPage | 소셜 로그인 콜백 |
| `/wedding/:invitationId` | WeddingInfoPage | 초대장 조회 |
| `/wedding/:invitationId/feed` | WeddingFeedPage | 웨딩 피드 |
| `/wedding/:invitationId/guestbook` | GuestbookPage | 방명록 |

### Protected (로그인 필요)

| 경로 | 페이지 | 설명 |
|------|--------|------|
| `/wedding/create` | WeddingCreatePage | 초대장 생성 |
| `/wedding/:id/edit` | WeddingEditPage | 초대장 수정 |
| `/wedding/:id/chat` | ChatPage | 실시간 채팅 |
| `/feed` | FeedPage | 전체 피드 |
| `/my-page` | MyPage | 마이 페이지 |
| `/my-page/posts` | MyPostsPage | 내 게시글 |
| `/my-page/bookmarks` | MyBookmarksPage | 북마크 |
| `/my-page/likes` | MyLikesPage | 좋아요 |
| `/my-page/comments` | MyCommentsPage | 댓글 |
| `/my-page/past-schedules` | PastSchedulesPage | 지난 일정 |
| `/edit-profile` | EditProfilePage | 프로필 수정 |
| `/delete-account` | DeleteAccountPage | 회원 탈퇴 |
| `/my-schedule` | MySchedulePage | 일정 관리 |

---

## 상태 관리

Zustand의 `useAuthStore`로 인증 상태를 관리합니다.

```typescript
const { user, accessToken, isAuthenticated, setAuth, clearAuth } = useAuthStore();
```

| 상태 | 타입 | 설명 |
|------|------|------|
| `user` | `User \| null` | 로그인된 사용자 정보 |
| `accessToken` | `string \| null` | 현재 액세스 토큰 |
| `isAuthenticated` | `boolean` | 인증 여부 |

앱 초기화 시 `App.tsx`에서 localStorage의 토큰을 복원합니다.

---

## 토큰 관리

`src/global/api/axiosInstance.ts`의 Axios 인터셉터가 토큰 갱신을 자동 처리합니다.

```
요청 인터셉터:   Authorization: Bearer {accessToken} 자동 첨부
응답 인터셉터:   401 응답 시 /api/auth/refresh 호출 → 새 토큰으로 원본 요청 재시도
                 갱신 중 대기 요청은 큐에 보관 → 갱신 완료 후 일괄 처리
```

토큰 저장:
- `accessToken` → Zustand store (메모리)
- `refreshToken` → `localStorage`

---

## 개발 컨벤션

### 컴포넌트 패턴

```typescript
// 함수형 컴포넌트 (FC 타입 명시)
const MyComponent: FC<Props> = ({ prop }) => {
  return <div>{prop}</div>;
};

export default MyComponent;
```

### 폼 처리

```typescript
const schema = z.object({
  email: z.string().email('올바른 이메일을 입력해주세요.'),
  password: z.string().min(8, '비밀번호는 8자 이상이어야 합니다.'),
});

const { register, handleSubmit, formState: { errors } } = useForm({
  resolver: zodResolver(schema),
});
```

### API 호출

```typescript
// domain/{module}/api/{module}Api.ts
export const getWedding = async (id: string): Promise<Wedding> => {
  const { data } = await axiosInstance.get(`/api/weddings/${id}`);
  return data.data;
};
```

### Barrel Export

각 도메인 `index.ts`에서 re-export:

```typescript
// domain/auth/index.ts
export { default as LoginPage } from './pages/LoginPage';
export { useAuthStore } from './store/useAuthStore';
export type { User, LoginRequest } from './types';
```

### 스타일링 규칙

- Tailwind CSS 유틸리티 클래스만 사용
- 인라인 `style` 속성 **사용 금지**
- UI 텍스트는 **한국어** 사용

### ESLint 규칙

```bash
npm run lint   # 검사
```

- 미사용 변수/임포트 **금지** (`no-unused-vars`)
- `any` 타입 **지양** (`@typescript-eslint/no-explicit-any`)
- 빌드 전 lint 오류 **0개** 유지
