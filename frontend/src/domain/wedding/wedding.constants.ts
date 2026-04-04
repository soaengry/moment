export const WEDDING_API = {
  BASE: "/api/invitations",
  DETAIL: (id: number) => `/api/invitations/${id}`,
  INFO: (invitationId: string) => `/api/invitations/${invitationId}/info`,
  CHECK_INVITATION: "/api/invitations/check-invitation",
  TEMPLATES: "/api/invitations/templates",
  COUPLES: (weddingId: number) => `/api/invitations/${weddingId}/couples`,
  SCHEDULES: (weddingId: number) => `/api/invitations/${weddingId}/schedules`,
  ACCOUNT_GROUPS: (weddingId: number) =>
    `/api/invitations/${weddingId}/account-groups`,
  ACCOUNTS: (groupId: number) =>
    `/api/invitations/account-groups/${groupId}/accounts`,
  TRANSPORTATION: (weddingId: number) =>
    `/api/invitations/${weddingId}/transportation`,
  ANNOUNCEMENTS: (weddingId: number) =>
    `/api/invitations/${weddingId}/announcements`,
  GALLERIES: (weddingId: number) => `/api/invitations/${weddingId}/galleries`,
} as const;

export const WEDDING_VALIDATION = {
  INVITATION_PATTERN: /^[a-z0-9-]+$/,
  INVITATION_MIN_LENGTH: 4,
  INVITATION_MAX_LENGTH: 20,
} as const;
