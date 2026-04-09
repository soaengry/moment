// ─── Enums ───

export type CoupleRole = "GROOM" | "BRIDE";
export type TemplateType = "WEDDING" | "REUNION" | "GATHERING";
export type TransportType = "SUBWAY" | "BUS" | "SHUTTLE";

// ─── Response ───

export interface WeddingResponse {
  id: number;
  title: string;
  invitationId: string;
  templateType: TemplateType;
  eventDate: string;
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
  invitationId: number;
  userId: number | null;
  role: CoupleRole;
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
  invitationId: number;
  time: string;
  title: string;
  description: string | null;
  orderIndex: number;
}

export interface AccountGroupResponse {
  id: number;
  invitationId: number;
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
  invitationId: number;
  imageUrl: string;
  thumbnailUrl: string | null;
  caption: string | null;
  orderIndex: number;
  createdAt: string;
}

export interface TransportationResponse {
  id: number;
  invitationId: number;
  type: TransportType;
  title: string;
  description: string | null;
  orderIndex: number;
}

export interface AccommodationResponse {
  id: number;
  invitationId: number;
  name: string;
  address: string | null;
  phone: string | null;
  distance: string | null;
  priceRange: string | null;
  orderIndex: number;
}

export interface AnnouncementResponse {
  id: number;
  invitationId: number;
  title: string;
  content: string;
  isPinned: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CheckResponse {
  exists: boolean;
}

export interface TemplateResponse {
  type: TemplateType;
  displayName: string;
  description: string | null;
  previewImageUrl: string | null;
}

// ─── Request ───

export interface WeddingRequest {
  title: string;
  invitationId: string;
  templateType?: TemplateType;
  eventDate: string;
  venueName: string;
  venueAddress: string;
  venueDetail?: string;
  venuePhone?: string;
  mapImageUrl?: string;
  dressCode?: string;
  notice?: string;
  parkingInfo?: string;
  mealInfo?: string;
  isPublic?: boolean;
}

export interface CoupleRequest {
  role: CoupleRole;
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
  invitation: WeddingResponse;
  couples: CoupleResponse[];
  schedules: ScheduleResponse[];
  accountGroups: AccountGroupWithAccounts[];
  gallery: GalleryResponse[];
  transportation: TransportationResponse[];
  accommodation: AccommodationResponse[];
  announcements: AnnouncementResponse[];
}
