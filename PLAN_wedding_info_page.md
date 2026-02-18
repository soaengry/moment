# Wedding Info Page 구현 계획서

## 1. 개요

- **경로**: `/wedding/:weddingId`
- **접근 권한**: Public (로그인 불필요)
- **백엔드 API**: `GET /api/weddings/{weddingId}/info` → `WeddingInfoResponse`
- **목적**: 모바일 청첩장 — 한 페이지에 결혼식 전체 정보를 섹션별로 표시

---

## 2. 파일 구조

```
src/domain/wedding/
├── api/
│   └── weddingApi.ts              # API 호출 함수
├── components/
│   ├── CoupleSection.tsx          # 신랑신부 소개
│   ├── DateVenueSection.tsx       # 예식 일시 및 장소
│   ├── LocationSection.tsx        # 오시는 길 (카카오맵 + 약도 이미지)
│   ├── ScheduleSection.tsx        # 식순 및 주요 일정
│   ├── DressCodeSection.tsx       # 드레스 코드 및 유의사항
│   ├── AccountSection.tsx         # 계좌번호 (그룹별)
│   ├── AnnouncementSection.tsx    # 공지사항 (토글 오픈)
│   └── index.ts                   # Barrel export
├── pages/
│   ├── WeddingInfoPage.tsx        # 메인 페이지 (섹션 조합)
│   └── index.ts
├── types.ts                       # 타입 정의 (백엔드 응답 매핑)
├── wedding.constants.ts           # API 경로, 카카오맵 키 등
└── index.ts                       # Barrel export
```

---

## 3. 타입 정의 (`types.ts`)

백엔드 `WeddingInfoResponse`를 기반으로 프론트엔드 타입 매핑:

```typescript
// ─── Enums ───
type CoupleRole = "GROOM" | "BRIDE";
type AccountSide = "GROOM" | "BRIDE" | "BOTH";
type TransportType = "SUBWAY" | "BUS" | "SHUTTLE";

// ─── Response ───
interface WeddingResponse {
  id: number;
  title: string;
  weddingDate: string;           // ISO DateTime
  venueName: string;
  venueAddress: string;
  venueDetail: string | null;
  venueLat: number | null;       // 카카오맵용 위도
  venueLng: number | null;       // 카카오맵용 경도
  venuePhone: string | null;
  mapImageUrl: string | null;    // 약도 이미지
  dressCode: string | null;
  notice: string | null;
  parkingInfo: string | null;
  mealInfo: string | null;
  createdAt: string;
  updatedAt: string;
}

interface CoupleResponse {
  id: number;
  weddingId: number;
  role: CoupleRole;
  name: string;
  fatherName: string | null;
  motherName: string | null;
  isFatherAlive: boolean;
  isMotherAlive: boolean;
  contact: string | null;
  profileImageUrl: string | null;
  introduction: string | null;
}

interface ScheduleResponse {
  id: number;
  weddingId: number;
  time: string;                  // "HH:mm:ss"
  title: string;
  description: string | null;
  orderIndex: number;
}

interface AccountGroupResponse {
  id: number;
  weddingId: number;
  side: AccountSide;
  groupName: string;
  orderIndex: number;
}

interface AccountResponse {
  id: number;
  accountGroupId: number;
  bankName: string;
  bankCode: string | null;
  accountNumber: string;
  accountHolder: string;
  kakaoPayUrl: string | null;    // 카카오톡 송금 URL
  orderIndex: number;
}

interface AccountGroupWithAccounts {
  group: AccountGroupResponse;
  accounts: AccountResponse[];
}

interface TransportationResponse {
  id: number;
  weddingId: number;
  type: TransportType;
  title: string;
  description: string | null;
  orderIndex: number;
}

interface AccommodationResponse { ... }
interface GalleryResponse { ... }

interface AnnouncementResponse {
  id: number;
  weddingId: number;
  title: string;
  content: string;
  isPinned: boolean;
  createdAt: string;
  updatedAt: string;
}

// ─── Aggregated ───
interface WeddingInfoResponse {
  wedding: WeddingResponse;
  couples: CoupleResponse[];
  schedules: ScheduleResponse[];
  accountGroups: AccountGroupWithAccounts[];
  gallery: GalleryResponse[];
  transportation: TransportationResponse[];
  accommodation: AccommodationResponse[];
  announcements: AnnouncementResponse[];
}
```

---

## 4. API 레이어 (`weddingApi.ts`)

```typescript
import axiosInstance from "../../global/api/axiosInstance";
import { WEDDING_API } from "../wedding.constants";
import type { WeddingInfoResponse } from "../types";

export const weddingApi = {
  getWeddingInfo: async (weddingId: number): Promise<WeddingInfoResponse> => {
    const { data } = await axiosInstance.get<WeddingInfoResponse>(
      WEDDING_API.INFO(weddingId)
    );
    return data;
  },
};
```

**상수** (`wedding.constants.ts`):
```typescript
export const WEDDING_API = {
  INFO: (id: number) => `/api/weddings/${id}/info`,
} as const;
```

> axiosInstance 사용 — 로그인 사용자의 경우 토큰이 자동 첨부되지만,
> 비로그인 사용자도 401 없이 정상 응답 받아야 하므로 **백엔드에서 해당 엔드포인트를 permitAll 처리 확인 필요**.

---

## 5. 메인 페이지 (`WeddingInfoPage.tsx`)

```
역할: useParams에서 weddingId 추출 → API 호출 → 로딩/에러 처리 → 섹션 컴포넌트에 props 전달

상태:
- data: WeddingInfoResponse | null
- isLoading: boolean
- error: string | null

흐름:
1. useEffect에서 weddingApi.getWeddingInfo(weddingId) 호출
2. 로딩 중 → 스켈레톤 or 스피너
3. 에러 (404 등) → "초대장을 찾을 수 없습니다" 메시지
4. 성공 → 아래 섹션 순서대로 렌더링

섹션 렌더링 순서:
┌─────────────────────────────────┐
│  1. CoupleSection               │ ← 신랑신부 소개
│  2. DateVenueSection            │ ← 예식 일시 및 장소
│  3. LocationSection             │ ← 오시는 길 (카카오맵)
│  4. ScheduleSection             │ ← 식순
│  5. DressCodeSection            │ ← 드레스 코드 + 유의사항
│  6. AccountSection              │ ← 계좌번호
│  7. AnnouncementSection (FAB)   │ ← 공지사항 버튼
└─────────────────────────────────┘

각 섹션은 데이터가 비어있으면 렌더링하지 않음 (조건부).
```

---

## 6. 컴포넌트 상세 설계

### 6-1. CoupleSection

```
Props: couples: CoupleResponse[]

동작:
- couples에서 GROOM / BRIDE를 분리
- 좌측 신랑, 우측 신부 배치 (모바일 가로 2열)

UI 구조:
┌──────────┬──────────┐
│ [사진]    │ [사진]    │
│ 신랑 이름 │ 신부 이름 │
│ 아버지 ○○│ 아버지 ○○│
│ 어머니 ○○│ 어머니 ○○│
│ (소개글)  │ (소개글)  │
└──────────┴──────────┘

- isFatherAlive/isMotherAlive가 false이면 이름 앞에 "故" 표시
- profileImageUrl이 없으면 기본 아바타 표시
- contact 있으면 전화 아이콘 표시 (tel: 링크)
```

### 6-2. DateVenueSection

```
Props: wedding: WeddingResponse

동작:
- weddingDate를 한국어 포맷으로 표시
  예: "2026년 3월 15일 토요일 오후 2시"
- venueName, venueAddress, venueDetail 표시
- venuePhone 있으면 전화 링크

UI 구조:
┌─────────────────────┐
│ 2026. 03. 15 (토)   │
│ 오후 2:00           │
│                     │
│ ○○호텔 그랜드홀     │
│ 서울시 강남구 ○○로   │
│ (상세 위치)          │
│ ☎ 02-1234-5678      │
└─────────────────────┘
```

### 6-3. LocationSection (카카오맵 연동)

```
Props: wedding: WeddingResponse (venueLat, venueLng, venueAddress, mapImageUrl)

동작:
1. venueLat/venueLng 존재 → 카카오맵 SDK 로드 → 지도 렌더링 + 마커
2. venueLat/venueLng 없음 + mapImageUrl 있음 → 약도 이미지만 표시
3. 둘 다 없음 → 섹션 미표시

카카오맵 연동:
- <script> 태그로 SDK 동적 로드 (또는 index.html에 추가)
- useEffect에서 kakao.maps.Map 초기화
- 마커 + 인포윈도우 (venueName)
- "카카오맵에서 보기" 버튼 → 외부 링크 오픈
- "길찾기" 버튼 → 카카오맵 길찾기 URL

약도 이미지:
- mapImageUrl이 있으면 지도 아래에 이미지 표시

UI 구조:
┌─────────────────────┐
│ [카카오맵 지도]       │
│                     │
│ [카카오맵에서 보기]   │
│ [길찾기]  [주소복사]  │
│                     │
│ [약도 이미지 (선택)]  │
└─────────────────────┘

외부 의존성: 카카오맵 JavaScript SDK
- 환경변수: VITE_KAKAO_MAP_KEY → env.ts에 추가
```

### 6-4. ScheduleSection

```
Props: schedules: ScheduleResponse[]

동작:
- orderIndex 기준 정렬
- 시간(HH:mm) + 제목 + 설명을 타임라인 형태로 표시

UI 구조:
┌─────────────────────┐
│ ● 13:30  하객 입장   │
│ │        (설명...)   │
│ ● 14:00  개식        │
│ │                    │
│ ● 14:30  예식        │
│ │        (설명...)   │
│ ● 15:00  폐식        │
└─────────────────────┘

- 세로 타임라인 점(●) + 연결선(│) 디자인
- 시간은 "HH:mm" 포맷으로 표시 (초 제거)
```

### 6-5. DressCodeSection

```
Props: wedding: WeddingResponse (dressCode, notice, parkingInfo, mealInfo)

동작:
- 각 필드가 존재하면 카드로 표시
- null이면 해당 항목 미표시

UI 구조:
┌─────────────────────┐
│ 👔 드레스 코드        │
│ "세미 포멀"          │
│                     │
│ 📝 유의사항           │
│ "주차 안내..."       │
│                     │
│ 🅿️ 주차 안내         │
│ "지하 2층 무료주차"   │
│                     │
│ 🍽️ 식사 안내         │
│ "2층 뷔페 홀"        │
└─────────────────────┘
```

### 6-6. AccountSection

```
Props: accountGroups: AccountGroupWithAccounts[]

동작:
- side(GROOM/BRIDE/BOTH)별 그룹 분리 표시
- 각 그룹 내 계좌를 카드로 표시
- 계좌번호 클릭 → 클립보드 복사 + 토스트 알림
- kakaoPayUrl 존재 시 "카카오페이 송금" 버튼 표시

클립보드 복사: navigator.clipboard.writeText()
토스트: react-toastify (이미 의존성에 포함)
카카오페이: kakaoPayUrl로 외부 링크 오픈 (window.open)

UI 구조:
┌─────────────────────────┐
│ 💍 신랑측               │
│ ┌─────────────────────┐ │
│ │ ○○은행 123-456-789  │ │
│ │ 홍길동              │ │
│ │ [복사] [카카오페이]   │ │
│ └─────────────────────┘ │
│ ┌─────────────────────┐ │
│ │ ○○은행 987-654-321  │ │
│ │ 홍아버지             │ │
│ │ [복사]              │ │
│ └─────────────────────┘ │
│                         │
│ 💐 신부측               │
│ ┌─────────────────────┐ │
│ │ ...                 │ │
│ └─────────────────────┘ │
└─────────────────────────┘

- 그룹 이름 (groupName) 표시
- orderIndex 기준 정렬
```

### 6-7. AnnouncementSection

```
Props: announcements: AnnouncementResponse[]

동작:
- 화면 하단 고정 FAB 버튼 "공지사항"
- 클릭 시 모달/바텀시트 오픈
- isPinned=true인 공지가 상단에 고정 표시
- createdAt 기준 최신순 정렬

UI 구조:

[고정 FAB 버튼: 공지사항 📢]

모달/바텀시트:
┌─────────────────────┐
│ 공지사항         [X] │
│─────────────────────│
│ 📌 중요 공지         │
│ 내용...              │
│ 2026.03.01          │
│─────────────────────│
│ 일반 공지            │
│ 내용...              │
│ 2026.02.28          │
└─────────────────────┘

- 공지 없으면 FAB 버튼 미표시
- isPinned 공지는 📌 아이콘 + 상단 고정
```

---

## 7. 라우트 등록

`AppRouter.tsx`에 추가:

```tsx
import { WeddingInfoPage } from "../../domain/wedding/pages";

// Public 라우트 (로그인 불필요, Layout 없음 — 전용 디자인)
<Route path="/wedding/:weddingId" element={<WeddingInfoPage />} />
```

> Layout(Header) 미사용 — 청첩장은 독립적인 전체 화면 디자인.

---

## 8. 외부 의존성

| 패키지 | 용도 | 설치 필요 |
|--------|------|----------|
| react-toastify | 계좌 복사 토스트 | 이미 설치됨 |
| 카카오맵 SDK | 지도 표시 | `<script>` 동적 로드 |

**카카오맵 SDK 연동 방식:**
- `index.html`에 `<script>` 태그 추가 또는 `LocationSection`에서 동적 로드
- `VITE_KAKAO_MAP_KEY` 환경변수 추가
- `global/config/env.ts`에 `KAKAO_MAP_KEY` 추가
- `window.kakao.maps` 타입 선언 (global.d.ts 또는 types에)

---

## 9. 백엔드 확인 필요 사항

1. **SecurityConfig**: `GET /api/weddings/{id}/info`를 `.permitAll()` 처리해야 함
   - 현재는 인증 필수로 설정되어 있을 가능성 있음
2. **CORS**: 비로그인 사용자의 요청도 허용되는지 확인

---

## 10. 구현 순서

```
Step 1: 기반 구조 생성
  - domain/wedding/ 디렉토리 + types.ts + wedding.constants.ts
  - weddingApi.ts
  - WeddingInfoPage.tsx (로딩/에러 처리 + API 연결)
  - AppRouter.tsx에 라우트 추가

Step 2: 핵심 섹션 구현
  - CoupleSection.tsx
  - DateVenueSection.tsx
  - ScheduleSection.tsx

Step 3: 카카오맵 연동
  - 환경변수 추가 (VITE_KAKAO_MAP_KEY)
  - LocationSection.tsx (SDK 로드 + 지도 + 약도 이미지)

Step 4: 계좌 & 인터랙션
  - AccountSection.tsx (그룹별 분리, 복사, 카카오페이)
  - DressCodeSection.tsx

Step 5: 공지사항
  - AnnouncementSection.tsx (FAB + 모달/바텀시트)

Step 6: 최종 조합 & 스타일링
  - WeddingInfoPage에서 전체 섹션 조합
  - 모바일 최적화 (max-w-lg mx-auto 패턴)
  - 섹션 간 구분선/여백 정리
```

---

## 11. 디자인 방향

- **모바일 퍼스트**: `max-w-lg mx-auto` (기존 패턴)
- **색상**: primary(#6B9F33) 기반, gold(#F0C434) 강조
- **각 섹션**: 흰색 카드 (`bg-white rounded-2xl shadow-lg p-6`)
- **Layout 미사용**: 청첩장 전용 풀스크린 디자인
- **폰트**: 우아한 느낌 (serif 계열 고려 가능, 기본은 sans-serif 유지)
