export const GUESTBOOK_API = {
  ENTRIES: (weddingId: number) => `/api/weddings/${weddingId}/guestbook`,
  ENTRY: (weddingId: number, entryId: number) => `/api/weddings/${weddingId}/guestbook/${entryId}`,
  VERIFY_PASSWORD: (weddingId: number, entryId: number) => `/api/weddings/${weddingId}/guestbook/${entryId}/verify-password`,
} as const;
