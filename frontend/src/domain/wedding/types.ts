// ─── Enums ───

export type CoupleRole = "GROOM" | "BRIDE";
export type AccountSide = "GROOM" | "BRIDE" | "BOTH";
export type TransportType = "SUBWAY" | "BUS" | "SHUTTLE";

// ─── Response ───

export interface WeddingResponse {
  id: number;
  title: string;
  weddingDate: string;
  venueName: string;
  venueAddress: string;
  venueDetail: string | null;
  venueLat: number | null;
  venueLng: number | null;
  venuePhone: string | null;
  mapImageUrl: string | null;
  dressCode: string | null;
  notice: string | null;
  parkingInfo: string | null;
  mealInfo: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CoupleResponse {
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
  side: AccountSide;
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

export interface GalleryResponse {
  id: number;
  weddingId: number;
  imageUrl: string;
  thumbnailUrl: string | null;
  caption: string | null;
  orderIndex: number;
  createdAt: string;
}

export interface TransportationResponse {
  id: number;
  weddingId: number;
  type: TransportType;
  title: string;
  description: string | null;
  orderIndex: number;
}

export interface AccommodationResponse {
  id: number;
  weddingId: number;
  name: string;
  address: string | null;
  phone: string | null;
  distance: string | null;
  priceRange: string | null;
  orderIndex: number;
}

export interface AnnouncementResponse {
  id: number;
  weddingId: number;
  title: string;
  content: string;
  isPinned: boolean;
  createdAt: string;
  updatedAt: string;
}

// ─── Request ───

export interface WeddingRequest {
  title: string;
  weddingDate: string;
  venueName: string;
  venueAddress: string;
  venueDetail?: string;
  venuePhone?: string;
  mapImageUrl?: string;
  dressCode?: string;
  notice?: string;
  parkingInfo?: string;
  mealInfo?: string;
}

export interface CoupleRequest {
  role: CoupleRole;
  name: string;
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
  side: AccountSide;
  groupName: string;
  orderIndex: number;
}

export interface AccountRequest {
  bankName: string;
  bankCode?: string;
  accountNumber: string;
  accountHolder: string;
  kakaoPayUrl?: string;
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

// ─── Aggregated ───

export interface WeddingInfoResponse {
  wedding: WeddingResponse;
  couples: CoupleResponse[];
  schedules: ScheduleResponse[];
  accountGroups: AccountGroupWithAccounts[];
  gallery: GalleryResponse[];
  transportation: TransportationResponse[];
  accommodation: AccommodationResponse[];
  announcements: AnnouncementResponse[];
}
