// ─── Enums ───

export type HostRole = "GROOM" | "BRIDE" | "HOST";
export type EventType = "WEDDING" | "GATHERING";
export type TransportType = "SUBWAY" | "BUS" | "SHUTTLE";
export type InvitationStatus = "INVITED" | "ACCEPTED" | "DECLINED";

// ─── Response ───

export interface EventResponse {
  id: number;
  userId: number;
  title: string;
  slug: string;
  type: EventType;
  date: string;
  locationName: string;
  locationAddress: string;
  locationDetail: string | null;
  locationLat: number | null;
  locationLng: number | null;
  isPublic: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface HostResponse {
  id: number;
  eventId: number;
  userId: number | null;
  role: HostRole;
  name: string;
  email: string;
  contact: string | null;
  profileImageUrl: string | null;
  introduction: string | null;
}

/** Wedding 이벤트의 호스트 — 부모님 정보가 flat하게 포함됨 */
export interface WeddingHostCombinedResponse {
  id: number;
  eventId: number;
  userId: number | null;
  role: HostRole;
  name: string;
  email: string;
  contact: string | null;
  profileImageUrl: string | null;
  introduction: string | null;
  fatherName: string | null;
  motherName: string | null;
  isFatherAlive: boolean | null;
  isMotherAlive: boolean | null;
}

export interface ScheduleResponse {
  id: number;
  eventId: number;
  title: string;
  description: string | null;
  orderIndex: number;
}

export interface AccountGroupResponse {
  id: number;
  eventId: number;
  groupName: string;
  orderIndex: number;
}

export interface AccountResponse {
  id: number;
  accountGroupId: number;
  bankName: string;
  bankCode: string | null;
  accountNumber: string;
  accountHolder: string;
  kakaoPayUrl: string | null;
  orderIndex: number;
}

export interface AccountGroupWithAccounts {
  group: AccountGroupResponse;
  accounts: AccountResponse[];
}

export interface HeroImageResponse {
  id: number;
  eventId: number;
  imageUrl: string;
  thumbnailUrl: string | null;
  orderIndex: number;
  createdAt: string;
}

export interface TransportationResponse {
  id: number;
  eventId: number;
  type: TransportType;
  title: string;
  description: string | null;
  orderIndex: number;
}

export interface AnnouncementResponse {
  id: number;
  eventId: number;
  title: string;
  content: string;
  isPinned: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface InvitationResponse {
  id: number;
  eventId: number;
  userId: number;
  status: InvitationStatus;
}

export interface CheckSlugResponse {
  exists: boolean;
}

// ─── Detail (polymorphic) ───

export interface WeddingDetailResponse {
  weddingId: number;
  notice: string | null;
  parkingInfo: string | null;
  mealInfo: string | null;
  greeting: string | null;
  hosts: WeddingHostCombinedResponse[];
}

export interface GatheringDetailResponse {
  hosts: HostResponse[];
}

export type EventDetailResponse = WeddingDetailResponse | GatheringDetailResponse;

export function isWeddingDetail(detail: EventDetailResponse | null | undefined): detail is WeddingDetailResponse {
  return detail != null && "weddingId" in detail;
}

// ─── Request ───

export interface EventRequest {
  title: string;
  slug: string;
  type?: EventType;
  date: string;
  locationName: string;
  locationAddress: string;
  locationDetail?: string;
  isPublic?: boolean;
  // Wedding-specific (optional — only sent for WEDDING type)
  notice?: string;
  parkingInfo?: string;
  mealInfo?: string;
  greeting?: string;
}

export interface WeddingHostData {
  fatherName?: string;
  motherName?: string;
  isFatherAlive?: boolean;
  isMotherAlive?: boolean;
}

export interface HostRequest {
  role: HostRole;
  name: string;
  email: string;
  contact?: string;
  profileImageUrl?: string;
  introduction?: string;
  weddingHostData?: WeddingHostData;
}

export interface ScheduleRequest {
  title: string;
  description?: string;
  orderIndex: number;
}

export interface AccountGroupRequest {
  groupName: string;
  orderIndex: number;
}

export interface AccountRequest {
  bankName: string;
  bankCode: string;
  accountNumber: string;
  accountHolder: string;
  kakaoPayUrl?: string;
  orderIndex: number;
}

export interface HeroImageRequest {
  imageUrl: string;
  thumbnailUrl?: string;
  orderIndex: number;
}

export interface TransportationRequest {
  type: TransportType;
  title: string;
  description?: string;
  orderIndex: number;
}

export interface AnnouncementRequest {
  title: string;
  content: string;
  isPinned: boolean;
}

// ─── Combined Create ───

export interface AccountGroupWithAccountsRequest {
  groupName: string;
  orderIndex: number;
  accounts: AccountRequest[];
}

/** POST /api/events — event + 모든 하위 도메인을 단일 트랜잭션으로 생성 */
export interface EventFullCreateRequest {
  event: EventRequest;
  heroImages?: HeroImageRequest[];
  schedules?: ScheduleRequest[];
  accountGroups?: AccountGroupWithAccountsRequest[];
  transportation?: TransportationRequest[];
  announcements?: AnnouncementRequest[];
  hosts?: HostRequest[];
}

// ─── Aggregated ───

export interface EventInfoResponse {
  event: EventResponse;
  heroImages: HeroImageResponse[];
  transportation: TransportationResponse[];
  announcements: AnnouncementResponse[];
  schedules: ScheduleResponse[];
  accountGroups: AccountGroupWithAccounts[];
  detail: WeddingDetailResponse | GatheringDetailResponse | null;
}
