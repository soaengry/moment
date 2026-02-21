export const WEDDING_API = {
  BASE: "/api/weddings",
  DETAIL: (id: number) => `/api/weddings/${id}`,
  INFO: (invitationId: string) => `/api/weddings/${invitationId}/info`,
  CHECK_INVITATION: "/api/weddings/check-invitation",
  COUPLES: (weddingId: number) => `/api/weddings/${weddingId}/couples`,
  SCHEDULES: (weddingId: number) => `/api/weddings/${weddingId}/schedules`,
  ACCOUNT_GROUPS: (weddingId: number) =>
    `/api/weddings/${weddingId}/account-groups`,
  ACCOUNTS: (groupId: number) =>
    `/api/weddings/account-groups/${groupId}/accounts`,
  TRANSPORTATION: (weddingId: number) =>
    `/api/weddings/${weddingId}/transportation`,
  ANNOUNCEMENTS: (weddingId: number) =>
    `/api/weddings/${weddingId}/announcements`,
  GALLERIES: (weddingId: number) => `/api/weddings/${weddingId}/galleries`,
} as const;

export const WEDDING_VALIDATION = {
  INVITATION_PATTERN: /^[a-z0-9-]+$/,
  INVITATION_MIN_LENGTH: 4,
  INVITATION_MAX_LENGTH: 20,
} as const;
