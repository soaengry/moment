# Moment Frontend - Skills & Recipes

## 새 도메인 모듈 추가

1. `src/domain/{module}/` 생성
2. 하위 구조:
   ```
   domain/{module}/
   ├── api/{module}Api.ts       # API 호출 함수
   ├── store/use{Module}Store.ts # Zustand (필요 시)
   ├── components/               # 재사용 컴포넌트
   │   └── index.ts
   ├── pages/                    # 라우트 페이지
   │   └── index.ts
   ├── types.ts                  # Request/Response/State 타입
   └── index.ts                  # Barrel export
   ```
3. `AppRouter.tsx`에 라우트 추가
4. 인증 필요 시 `ProtectedRoute`로 감싸기

## 새 페이지 추가

1. `domain/{module}/pages/{PageName}.tsx` 생성 (FC 타입)
2. `pages/index.ts`에 export 추가
3. `AppRouter.tsx`에 Route 추가:
   ```tsx
   // Public
   <Route path="/new-page" element={<NewPage />} />

   // Protected (Layout 포함)
   <Route path="/new-page" element={
     <ProtectedRoute>
       <Layout>
         <NewPage />
       </Layout>
     </ProtectedRoute>
   } />
   ```

## 새 API 엔드포인트 연결

1. 상수 정의 (`{module}.constants.ts` 또는 기존 `auth.constants.ts`):
   ```typescript
   export const MODULE_API = {
     LIST: "/api/{module}",
     DETAIL: "/api/{module}/:id",
   } as const;
   ```

2. API 함수 작성 (`api/{module}Api.ts`):
   ```typescript
   import axiosInstance from "../../global/api/axiosInstance";
   import type { CreateRequest, DetailResponse } from "../types";

   export const moduleApi = {
     getList: () => axiosInstance.get<DetailResponse[]>(MODULE_API.LIST),
     create: (data: CreateRequest) => axiosInstance.post(MODULE_API.LIST, data),
   };
   ```

3. 타입 정의 (`types.ts`):
   ```typescript
   export interface CreateRequest { name: string; }
   export interface DetailResponse { id: number; name: string; }
   ```

4. 에러 처리 패턴:
   ```typescript
   try {
     const { data } = await moduleApi.create(request);
   } catch (err) {
     if (isAxiosError(err)) {
       const status = err.response?.status;
       if (status === 409) { /* 중복 */ }
       else if (status === 400) { /* 검증 실패 */ }
     }
   }
   ```

## 폼 컴포넌트 추가

```tsx
import { useForm } from "react-hook-form";
import { z } from "zod/v4";
import { zodResolver } from "@hookform/resolvers/zod";

// 1. Zod 스키마 정의
const formSchema = z.object({
  name: z.string().min(2, "2자 이상 입력해주세요"),
  email: z.email("이메일 형식이 올바르지 않습니다"),
});

type FormData = z.infer<typeof formSchema>;

// 2. 폼 훅 사용
const { register, handleSubmit, formState: { errors, isSubmitting } } =
  useForm<FormData>({ resolver: zodResolver(formSchema) });

// 3. 에러 표시
{errors.name && <p className="text-rose text-sm">{errors.name.message}</p>}

// 4. 비동기 검증 (onBlur)
const handleEmailBlur = async () => {
  const { data } = await authApi.checkEmail(email);
  if (data.exists) setError("email", { message: "이미 사용 중인 이메일입니다" });
};
```

## Zustand 스토어 추가

```typescript
import { create } from "zustand";

interface ModuleState {
  items: Item[];
  isLoading: boolean;
  setItems: (items: Item[]) => void;
  setLoading: (loading: boolean) => void;
  reset: () => void;
}

export const useModuleStore = create<ModuleState>((set) => ({
  items: [],
  isLoading: false,
  setItems: (items) => set({ items }),
  setLoading: (loading) => set({ isLoading: loading }),
  reset: () => set({ items: [], isLoading: false }),
}));

// 컴포넌트에서 선택적 구독
const items = useModuleStore((s) => s.items);
```

## 전역 컴포넌트 추가

1. `src/global/components/{Component}.tsx` 생성
2. Props 인터페이스 정의
3. Tailwind 유틸리티 클래스로 스타일링
4. 커스텀 컬러는 `tailwind.config.js` 테마 참조:
   - `text-primary`, `bg-bgPrimary`, `text-rose`, `bg-error` 등

## 환경변수 추가

1. `.env`에 `VITE_` 접두사로 변수 추가
2. `src/global/config/env.ts`의 `ENV` 객체에 추가:
   ```typescript
   export const ENV = {
     API_BASE_URL: import.meta.env.VITE_API_BASE_URL ?? "",
     OAUTH2: import.meta.env.VITE_OAUTH2_BASE_URL ?? "/oauth2/authorization",
     NEW_VAR: import.meta.env.VITE_NEW_VAR ?? "default",
   } as const;
   ```
3. 컴포넌트에서 `ENV.NEW_VAR`로 접근

## 파일 업로드 구현

```tsx
// EditProfilePage.tsx 패턴 참고
const handleUpload = async (file: File) => {
  const formData = new FormData();
  formData.append("file", file);

  // Content-Type을 설정하지 않음 (axios가 자동 설정)
  const { data } = await axiosInstance.patch("/api/users/me", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

// 이미지 미리보기
const [preview, setPreview] = useState<string | null>(null);
const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
  const file = e.target.files?.[0];
  if (file) {
    setPreview(URL.createObjectURL(file));
  }
};

// cleanup
useEffect(() => {
  return () => { if (preview) URL.revokeObjectURL(preview); };
}, [preview]);
```

## OAuth2 소셜 로그인 플로우

1. `SocialLoginButtons`에서 백엔드 OAuth2 URL로 리다이렉트:
   ```typescript
   window.location.href = `${ENV.API_BASE_URL}${ENV.OAUTH2}/{provider}`;
   ```
2. 백엔드 인증 완료 후 프론트엔드 `/oauth2/callback`으로 리다이렉트
3. `OAuth2CallbackPage`에서 URL 쿼리 파라미터로 토큰 수신:
   ```typescript
   const params = new URLSearchParams(location.search);
   const accessToken = params.get("accessToken");
   const refreshToken = params.get("refreshToken");
   ```
4. 토큰 저장 → `getMe()` 호출 → `useAuthStore.setAuth()` → 홈으로 이동

## Protected 라우트 추가

`ProtectedRoute`는 `useAuthStore`의 `isAuthenticated`를 확인:
- 인증됨 → children 렌더링
- 미인증 + 로딩 중 → 로딩 표시
- 미인증 + 로딩 완료 → `/login`으로 Navigate
