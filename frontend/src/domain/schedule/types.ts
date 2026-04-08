// ─── Response ───

export interface AttendanceResponse {
  id: number;
  eventId: number;
  slug: string;
  title: string;
  date: string;
  locationName: string;
  locationAddress: string;
  groomName: string | null;
  brideName: string | null;
  groomProfileImageUrl: string | null;
  brideProfileImageUrl: string | null;
  createdAt: string;
}

// ─── Request ───

export interface AddAttendanceRequest {
  slug: string;
}
