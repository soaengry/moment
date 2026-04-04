// ─── Response ───

export interface AttendanceResponse {
  id: number;
  weddingId: number;
  invitationId: string;
  title: string;
  eventDate: string;
  venueName: string;
  venueAddress: string;
  groomName: string | null;
  brideName: string | null;
  groomProfileImageUrl: string | null;
  brideProfileImageUrl: string | null;
  createdAt: string;
}

// ─── Request ───

export interface AddAttendanceRequest {
  invitationId: string;
}
