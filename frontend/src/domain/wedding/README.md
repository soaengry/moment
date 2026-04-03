# Wedding Domain

웨딩 초대장 생성, 조회, 수정 기능을 담당하는 도메인입니다.

## 기술 스택

- React 19 + TypeScript
- React Router DOM 7 (라우팅)
- Zustand (상태 관리)
- React Hook Form + Zod (폼 처리 및 유효성 검증)
- Framer Motion (애니메이션)
- Axios (HTTP 클라이언트)
- Kakao Maps SDK (지도)
- Daum Postcode (주소 검색)

## 디렉토리 구조

```
wedding/
├── pages/
│   ├── WeddingCreatePage.tsx       # 초대장 생성 (5단계 위자드)
│   ├── WeddingEditPage.tsx         # 초대장 수정
│   ├── WeddingInfoPage.tsx         # 초대장 공개 조회 (탭 기반)
│   └── index.ts
├── components/
│   ├── create/                     # 생성 단계별 컴포넌트
│   │   ├── BasicInfoStep.tsx       # 1단계: 기본 정보
│   │   ├── CoupleStep.tsx          # 2단계: 신랑/신부 정보
│   │   ├── ScheduleStep.tsx        # 3단계: 식순
│   │   ├── AccountStep.tsx         # 4단계: 축의금 계좌
│   │   └── ExtraInfoStep.tsx       # 5단계: 교통/공지/드레스코드
│   ├── LandingSection.tsx          # 히어로 캐러셀 (Ken Burns 효과)
│   ├── CoupleSection.tsx           # 신랑/신부 프로필
│   ├── DateVenueSection.tsx        # 일시 및 예식장 정보
│   ├── LocationSection.tsx         # 카카오맵 기반 위치 안내
│   ├── ScheduleSection.tsx         # 식순 타임라인(시간 오름차순)
│   ├── DressCodeSection.tsx        # 드레스코드 및 교통편
│   ├── AccountSection.tsx          # 축의금 계좌 (카카오페이/토스 지원)
│   ├── AnnouncementSection.tsx     # 공지사항 모달
│   └── index.ts
├── api/
│   └── weddingApi.ts               # API 서비스 레이어
├── types.ts                        # 타입 정의
├── wedding.constants.ts            # API 엔드포인트 및 유효성 규칙
└── index.ts
```

## 라우팅

| 경로                 | 컴포넌트          | 인증   | 설명                    |
| -------------------- | ----------------- | ------ | ----------------------- |
| `/create`            | WeddingCreatePage | 필요   | 초대장 생성             |
| `/wedding`           | WeddingInfoPage   | 불필요 | 초대장 조회 (정보 탭)   |
| `/wedding/gifts`     | WeddingInfoPage   | 불필요 | 초대장 조회 (축의금 탭) |
| `/wedding/guestbook` | WeddingInfoPage   | 불필요 | 초대장 조회 (방명록 탭) |
| `/wedding/gallery`   | WeddingInfoPage   | 불필요 | 초대장 조회 (갤러리 탭) |
| `/wedding/upload`    | WeddingInfoPage   | 불필요 | 초대장 조회 (업로드 탭) |
| `/wedding/edit`      | WeddingEditPage   | 필요   | 초대장 수정             |

## 페이지 상세

### WeddingCreatePage

5단계 위자드 폼으로 초대장을 생성합니다.

1. **BasicInfoStep** — 제목, 초대장 URL 슬러그(4~20자, 영문/숫자/하이픈), 일시, 예식장 정보, 주소(다음 우편번호 검색)
2. **CoupleStep** — 신랑/신부 이름, 연락처, 부모님 정보(고인 여부 포함), 소개글, 프로필 이미지, 랜딩 캐러셀 사진 업로드
3. **ScheduleStep** — 식순 타임라인 항목 추가/정렬
4. **AccountStep** — 축의금 계좌 그룹(신랑측/신부측/가족측) 및 개별 계좌(은행명, 계좌번호, 예금주, 카카오페이 URL)
5. **ExtraInfoStep** — 교통편(지하철/버스/셔틀), 공지사항(고정 가능), 드레스코드, 주차/식사 안내

각 단계의 데이터를 클라이언트에서 축적한 후, 최종 제출 시 부모 리소스 생성 → 하위 리소스 순차 생성 방식으로 API를 호출합니다.

### WeddingInfoPage

공개 초대장 페이지입니다. 하단 탭 네비게이션으로 4개 탭을 제공합니다.

- **정보(Info)** — LandingSection, CoupleSection, DateVenueSection, LocationSection, ScheduleSection, AccountSection
- **축의금(Gifts)** — 축의금 전달 계좌 정보 조회
- **갤러리(Gallery)** — 웨딩 사진
- **방명록(Guestbook)** — 축하 메시지
- **사진 업로드(Upload)** — 구글 드라이브 링크 연결

JWT에서 사용자 ID를 추출하여 ADMIN, GROOM, BRIDE인지 판단하고, 이들에게만 수정 버튼을 노출

### WeddingEditPage

기존 초대장 데이터를 불러와 동일한 5단계 위자드에서 수정합니다. 하위 리소스(커플, 식순, 계좌 등)는 전체 삭제 후 재생성 방식으로 일관성을 유지합니다.

## API (weddingApi.ts)

18개 이상의 비동기 메서드로 구성되어 있으며, 주요 기능은 다음과 같습니다.

- Wedding CRUD
- Couple 생성/수정/삭제
- Schedule 관리
- AccountGroup 및 Account 관리
- Transportation 옵션 관리
- Announcement 관리
- Gallery 이미지 관리
- S3 파일 업로드

## 타입 (types.ts)

주요 인터페이스 및 Enum:

- `WeddingResponse` / `WeddingRequest` — 웨딩 정보
- `CoupleResponse` / `CoupleRequest` — 신랑/신부 정보
- `ScheduleResponse` / `ScheduleRequest` — 식순
- `AccountGroupResponse` / `AccountResponse` — 축의금 계좌
- `TransportationResponse` — 교통편
- `AnnouncementResponse` — 공지사항
- `GalleryResponse` — 갤러리
- `CoupleRole` — `GROOM` | `BRIDE`
- `AccountSide` — 신랑측/신부측/신랑가족/신부가족
- `TransportType` — 지하철/버스/셔틀

## 주요 기술 패턴

- **멀티 스텝 폼** — 클라이언트 측 단계별 데이터 축적 + 단계별 유효성 검증
- **중첩 리소스 생성** — 부모 리소스 생성 후 하위 리소스 순차 호출
- **주소 검색** — 다음 우편번호 서비스 연동
- **지도** — Kakao Maps SDK 동적 로딩 및 지연 초기화
- **파일 업로드** — API 경유 S3 직접 업로드 + 미리보기 생성
- **탭 라우팅** — URL 경로 기반 활성 탭 결정
