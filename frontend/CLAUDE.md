# Moment Frontend

React 19 + TypeScript + Vite 기반 웨딩 초대장 플랫폼 SPA.

## Tech Stack

| 분류 | 라이브러리 | 버전 | 용도 |
|------|-----------|------|------|
| UI | React | 19.2.0 | 컴포넌트 |
| 언어 | TypeScript | 5.9.3 | 타입 안전성 |
| 빌드 | Vite | 7 | 번들러 + 프록시 |
| 상태관리 | Zustand | 5.0.11 | 글로벌 상태 (auth) |
| 폼 | React Hook Form | 7 | 폼 관리 |
| 폼 검증 | Zod | 4 | 스키마 검증 |
| HTTP | Axios | 1.13.5 | API 호출 + 인터셉터 |
| 라우팅 | React Router | 7 | SPA 라우팅 |
| 스타일링 | Tailwind CSS | 3.4 | 유틸리티 CSS |
| 알림 | React Toastify | 11 | 토스트 메시지 |
| 린터 | ESLint | 9 | 코드 품질 |

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
│   │   │   ├── LoginForm.tsx
│   │   │   ├── SignUpForm.tsx
│   │   │   ├── SocialLoginButtons.tsx
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
│   ├── user/
│   │   └── pages/
│   │       ├── MyPage.tsx
│   │       ├── EditProfilePage.tsx
│   │       ├── ChangePasswordPage.tsx
│   │       ├── DeleteAccountPage.tsx
│   │       └── index.ts
│   │
│   ├── guestbook/                  # 방명록
│   ├── chat/                       # 채팅 (WebSocket)
│   └── schedule/                   # 일정
│
└── global/
    ├── api/axiosInstance.ts         # Axios 인스턴스 (인터셉터, 자동 토큰 갱신)
    ├── components/
    │   ├── Header.tsx
    │   └── Layout.tsx
    ├── config/env.ts                # 환경변수 (VITE_API_BASE_URL, VITE_OAUTH2_BASE_URL)
    └── pages/
        ├── HomePage.tsx
        └── index.ts
```

## Architecture & Conventions

- **DDD 구조**: `domain/{module}/` (api, store, components, pages, types)
- **상태관리**: Zustand — `useAuthStore` (user, tokens, isAuthenticated)
- **폼**: React Hook Form + Zod (`zodResolver`)
- **HTTP**: Axios — `axiosInstance` (인터셉터로 토큰 자동 첨부/갱신)
- **스타일**: Tailwind CSS 유틸리티 클래스만 사용 (인라인 `style` 속성 금지)
- **라우팅**: React Router v7 — `AppRouter` + `ProtectedRoute`
- **타입**: 도메인별 `types.ts`에 인터페이스 정의
- **컴포넌트**: 함수형 컴포넌트 (`FC` 타입)
- **Barrel Export**: 각 모듈의 `index.ts`에서 re-export
- **UI 텍스트**: 한국어

## Coding Conventions

### ⚠️ 사용되지 않는 변수 절대 금지

**선언했지만 사용하지 않는 변수, 파라미터, import는 절대 작성하지 않는다.**

```typescript
// ❌ 금지 — unused variable
const unused = someApi.get();
import { SomeComponent } from "./SomeComponent"; // 사용 안 함

// ❌ 금지 — unused parameter
function handler(_event: React.ChangeEvent) {  // 사용 안 하면 선언 자체 삭제
  doSomething();
}

// ✅ 올바른 예
const data = await someApi.get();
console.log(data);
```

ESLint(`no-unused-vars`)가 강제하며, `npm run lint` 실패의 가장 흔한 원인이다.
코드 작성 후 반드시 `npm run lint`를 실행해 미사용 변수가 없는지 확인한다.

### 기타 컨벤션

- **컴포넌트**: `const Component: FC<Props> = () => {}` 형식
- **타입**: `interface`로 정의, `types.ts`에 위치
- **API 상수**: `{module}.constants.ts`에 `as const` 객체로 정의
- **에러 처리**: `isAxiosError(err)` + 상태코드별 분기
- **로딩 상태**: `isSubmitting` / `isSending` 플래그로 버튼 `disabled` 처리
- **import 정렬**: 외부 라이브러리 → 내부 모듈 순서

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

- **API 호출**: `{module}Api.ts` → `axiosInstance` → 백엔드
- **에러 처리**: `try/catch` + `isAxiosError` + 상태코드별 분기
- **폼 검증**: Zod 스키마 + `onBlur` 비동기 중복 체크 (이메일, 닉네임)
- **로딩 상태**: `isSubmitting`/`isSending` 플래그 + 버튼 `disabled`
- **파일 업로드**: `FormData` + `axiosInstance` (EditProfilePage 참고)
- **OAuth2 플로우**: SocialLoginButtons → 백엔드 OAuth2 URL → 콜백에서 토큰 수신

## Rules for AI Assistants

### 새 도메인 생성 시 필수 체크리스트

1. `domain/{module}/` 하위에 api, pages, types 구조 생성 (store, components는 필요 시 추가)
2. 각 서브 폴더에 `index.ts` barrel export 유지
3. `AppRouter.tsx`에 라우트 추가
4. Protected 페이지는 `ProtectedRoute`로 반드시 감싸기
5. 상태관리가 필요하면 `domain/{module}/store/use{Module}Store.ts` 생성
6. 전역 컴포넌트는 `global/components/`에 배치

### 코딩 규칙 (필수 준수)

- **사용되지 않는 변수 절대 금지** — import, const, let, 함수 파라미터 모두 해당. 선언 후 미사용 시 삭제
- **폼은 반드시 React Hook Form + Zod 사용** — 로컬 state로 폼 관리 금지
- **스타일은 Tailwind 유틸리티 클래스만** — 인라인 `style` 속성 금지
- **환경변수는 `VITE_` 접두사** — `global/config/env.ts`에서 `ENV` 객체로 관리
- **타입은 `interface`로 도메인별 `types.ts`에 정의** — 컴포넌트 파일 내 타입 선언 최소화
- **API 경로 상수는 `{module}.constants.ts` 패턴** (예: `auth.constants.ts`) 따름
- **코드 작성 후 `npm run lint` 실행** — lint 오류 없이 완료해야 작업 완료로 간주
