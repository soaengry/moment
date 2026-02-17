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
