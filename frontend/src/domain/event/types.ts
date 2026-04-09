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

export interface WeddingResponse {
  id: number;
  eventId: number;
  notice: string | null;
  parkingInfo: string | null;
  mealInfo: string | null;
  greeting: string | null;
}

export interface HostResponse {
  id: number;
  eventId: number;
  userId: number | null;
  role: HostRole;
  name: string;
  email: string;
  fatherName: string | null;
  motherName: string | null;
  isFatherAlive: boolean;
  isMotherAlive: boolean;
  contact: string | null;
  profileImageUrl: string | null;
  introduction: string | null;
}

export interface ScheduleResponse {
  id: number;
  weddingId: number;
  time: string;
  title: string;
  description: string | null;
  orderIndex: number;
}

export interface AccountGroupResponse {
  id: number;
  weddingId: number;
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

export interface RsvpResponse {
  id: number;
  weddingId: number;
  sessionId: string;
  userId: number | null;
  attendance: boolean;
  name: string;
  side: string | null;
  phone: string | null;
  attendeeCount: number;
  willEat: boolean;
  mealCount: number;
  willRide: boolean;
  rideCount: number;
  note: string | null;
  consent: boolean;
  createdAt: string;
}

export interface CheckSlugResponse {
  exists: boolean;
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
}

export interface WeddingRequest {
  eventId: number;
  notice?: string;
  parkingInfo?: string;
  mealInfo?: string;
  greeting?: string;
}

export interface HostRequest {
  role: HostRole;
  name: string;
  email: string;
  fatherName?: string;
  motherName?: string;
  isFatherAlive?: boolean;
  isMotherAlive?: boolean;
  contact?: string;
  profileImageUrl?: string;
  introduction?: string;
}

export interface ScheduleRequest {
  time: string;
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

export interface RsvpRequest {
  sessionId: string;
  attendance: boolean;
  name: string;
  side?: string;
  phone?: string;
  attendeeCount: number;
  willEat: boolean;
  mealCount: number;
  willRide: boolean;
  rideCount: number;
  note?: string;
  consent: boolean;
}

// ─── Aggregated ───

export interface EventInfoResponse {
  event: EventResponse;
  heroImages: HeroImageResponse[];
  transportation: TransportationResponse[];
  announcements: AnnouncementResponse[];
  wedding: WeddingResponse | null;
  hosts: HostResponse[];
  schedules: ScheduleResponse[];
  accountGroups: AccountGroupWithAccounts[];
}
