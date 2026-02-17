export const WEDDING_API = {
  BASE: "/api/weddings",
  DETAIL: (id: number) => `/api/weddings/${id}`,
  INFO: (id: number) => `/api/weddings/${id}/info`,
  COUPLES: (weddingId: number) => `/api/weddings/${weddingId}/couples`,
  SCHEDULES: (weddingId: number) => `/api/weddings/${weddingId}/schedules`,
  ACCOUNT_GROUPS: (weddingId: number) => `/api/weddings/${weddingId}/account-groups`,
  ACCOUNTS: (groupId: number) => `/api/weddings/account-groups/${groupId}/accounts`,
  TRANSPORTATION: (weddingId: number) => `/api/weddings/${weddingId}/transportation`,
  ANNOUNCEMENTS: (weddingId: number) => `/api/weddings/${weddingId}/announcements`,
} as const;
