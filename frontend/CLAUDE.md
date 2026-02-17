# Moment Frontend

React 19 + TypeScript + Vite 기반 웨딩 초대장 플랫폼 SPA.
Zustand (상태관리), React Hook Form + Zod (폼 검증), Tailwind CSS (스타일링).

## Quick Commands

```bash
npm run dev       # 개발 서버 (port 3000, /api → localhost:8080 프록시)
npm run build     # 프로덕션 빌드 (tsc -b && vite build)
npm run lint      # ESLint 검사
npm run preview   # 빌드 미리보기
```

## Project Structure

```
src/
├── main.tsx                        # 진입점 (React root, BrowserRouter X)
├── index.css                       # Tailwind 디렉티브 + 글로벌 CSS
│
├── app/
│   ├── App.tsx                     # 앱 초기화 (토큰 복원, 로딩 상태)
│   └── routes/
│       ├── AppRouter.tsx           # 전체 라우트 정의
│       └── ProtectedRoute.tsx      # 인증 가드 (미인증 → /login)
│
├── domain/
│   ├── auth/
│   │   ├── api/authApi.ts          # 인증/사용자 API 호출 함수
│   │   ├── store/useAuthStore.ts   # Zustand 인증 상태 관리
│   │   ├── components/
│   │   │   ├── LoginForm.tsx       # 로그인 폼 (RHF + Zod)
│   │   │   ├── SignUpForm.tsx      # 회원가입 폼 (이메일/닉네임 중복 체크)
│   │   │   ├── SocialLoginButtons.tsx  # OAuth2 소셜 로그인 버튼
│   │   │   └── index.ts
│   │   ├── pages/
│   │   │   ├── LoginPage.tsx
│   │   │   ├── SignUpPage.tsx
│   │   │   ├── VerifyEmailPage.tsx
│   │   │   ├── OAuth2CallbackPage.tsx
│   │   │   ├── RestoreAccountPage.tsx
│   │   │   └── index.ts
│   │   ├── auth.constants.ts       # API 경로, 스토리지 키, 검증 규칙
│   │   ├── auth.utils.ts           # 토큰/디바이스 스토리지, JWT 파싱
│   │   ├── types.ts                # Request/Response/State 타입
│   │   └── index.ts                # Barrel export
│   │
│   └── user/
│       └── pages/
│           ├── MyPage.tsx          # 프로필 조회
│           ├── EditProfilePage.tsx  # 프로필 수정 (이미지 업로드 포함)
│           ├── ChangePasswordPage.tsx
│           ├── DeleteAccountPage.tsx
│           └── index.ts
│
└── global/
    ├── api/axiosInstance.ts         # Axios 인스턴스 (인터셉터, 자동 토큰 갱신)
    ├── components/
    │   ├── Header.tsx               # 상단 헤더 (스크롤 숨김 동작)
    │   └── Layout.tsx               # 페이지 레이아웃 (Header + children)
    ├── config/env.ts                # 환경변수 (VITE_API_BASE_URL, VITE_OAUTH2_BASE_URL)
    └── pages/
        ├── HomePage.tsx
        └── index.ts
```

## Tech Stack

| 분류 | 라이브러리 | 용도 |
|------|-----------|------|
| UI | React 19 | 컴포넌트 |
| 빌드 | Vite 7 | 번들러 + 프록시 |
| 언어 | TypeScript 5.9 | 타입 안전성 |
| 상태관리 | Zustand 5 | 글로벌 상태 (auth) |
| 폼 | React Hook Form 7 + Zod 4 | 폼 검증 |
| HTTP | Axios | API 호출 + 인터셉터 |
| 라우팅 | React Router 7 | SPA 라우팅 |
| 스타일링 | Tailwind CSS 3.4 | 유틸리티 CSS |
| 알림 | React Toastify | 토스트 메시지 |

## Architecture & Conventions

- **DDD 구조**: `domain/{module}/` (api, store, components, pages, types)
- **상태관리**: Zustand — `useAuthStore` (user, tokens, isAuthenticated)
- **폼**: React Hook Form + Zod (`zodResolver`)
- **HTTP**: Axios — `axiosInstance` (인터셉터로 토큰 자동 첨부/갱신)
- **스타일**: Tailwind CSS 유틸리티 클래스만 사용
- **라우팅**: React Router v7 — `AppRouter` + `ProtectedRoute`
- **타입**: 도메인별 `types.ts`에 인터페이스 정의
- **컴포넌트**: 함수형 컴포넌트 (`FC` 타입)
- **Barrel Export**: 각 모듈의 `index.ts`에서 re-export
- **UI 텍스트**: 한국어

## Color Theme

```
primary:      #6B9F33  (메인 그린)
primaryHover: #5A8A2C  (호버)
bgPrimary:    #FAFFF4  (배경)
gold:         #F0C434  (강조)
rose:         #E6A5A5  (에러 텍스트)
success:      #16A34A  / bgSuccess: #DCFCE7
error:        #FD5B5B  / bgError:   #FDEDED
```

## Token Management

`axiosInstance.ts`에서 자동 관리:

1. **Request 인터셉터**: localStorage의 access token을 Bearer 헤더에 첨부
2. **Response 인터셉터**: 401 응답 시 자동 토큰 갱신
3. **큐잉**: 갱신 중 추가 요청은 큐에 대기 (동시 갱신 방지)
4. **실패 처리**: 갱신 실패 시 토큰 삭제 + `/login`으로 리다이렉트

**스토리지 키** (`auth.constants.ts`):
- `moment_access_token` — Access Token
- `moment_refresh_token` — Refresh Token
- `moment_device_id` — 디바이스 ID

## Routes

### Public (비로그인)
| 경로 | 페이지 |
|------|--------|
| `/` | HomePage |
| `/login` | LoginPage |
| `/signup` | SignUpPage |
| `/verify-email` | VerifyEmailPage |
| `/restore-account` | RestoreAccountPage |
| `/oauth2/callback` | OAuth2CallbackPage |

### Protected (로그인 필요)
| 경로 | 페이지 |
|------|--------|
| `/my-page` | MyPage |
| `/edit-profile` | EditProfilePage |
| `/change-password` | ChangePasswordPage |
| `/delete-account` | DeleteAccountPage |

## API Constants

```typescript
AUTH_API: LOGIN, SIGN_UP, VERIFY_EMAIL, RESEND_VERIFICATION,
          REFRESH, LOGOUT, CHECK_EMAIL, CHECK_NICKNAME
USER_API: ME, PASSWORD, RESTORE
```

## Environment Variables

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_OAUTH2_BASE_URL=/oauth2/authorization
```

`global/config/env.ts`의 `ENV` 객체에서 관리.

## Key Patterns

- **API 호출**: `authApi.ts` → `axiosInstance` → 백엔드
- **에러 처리**: `try/catch` + `isAxiosError` + 상태코드별 분기
- **폼 검증**: Zod 스키마 + `onBlur` 비동기 중복 체크 (이메일, 닉네임)
- **로딩 상태**: `isSubmitting`/`isSending` 플래그 + 버튼 `disabled`
- **파일 업로드**: `FormData` + `axiosInstance` (EditProfilePage 참고)
- **OAuth2 플로우**: SocialLoginButtons → 백엔드 OAuth2 URL → 콜백에서 토큰 수신

## Rules for AI Assistants

- 새 도메인은 `domain/{module}/` 하위에 api, pages, types 구조
- 전역 컴포넌트는 `global/components/`에 배치
- 상태관리는 Zustand store (`domain/{module}/store/`)
- API 호출은 도메인별 api 파일에 집중
- 폼은 반드시 React Hook Form + Zod 사용
- 스타일은 Tailwind 유틸리티 클래스만 사용 (인라인 style 금지)
- 환경변수는 `VITE_` 접두사 + `global/config/env.ts`에서 관리
- Barrel export 유지 (각 모듈 `index.ts`)
- 타입은 도메인별 `types.ts`에 interface로 정의
- API 경로 상수는 `auth.constants.ts` 패턴을 따름
- Protected 페이지는 `ProtectedRoute`로 감싸기
