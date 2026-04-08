export const EVENT_API = {
  // Event
  BASE: "/api/events",
  DETAIL: (id: number) => `/api/events/${id}`,
  INFO: (slug: string) => `/api/events/${slug}/info`,
  CHECK_SLUG: "/api/events/check-slug",
  HERO_IMAGES: (eventId: number) => `/api/events/${eventId}/hero-images`,
  HERO_IMAGE: (imageId: number) => `/api/events/hero-images/${imageId}`,
  TRANSPORTATION: (eventId: number) => `/api/events/${eventId}/transportation`,
  TRANSPORTATION_DETAIL: (transportId: number) =>
    `/api/events/transportation/${transportId}`,
  ANNOUNCEMENTS: (eventId: number) => `/api/events/${eventId}/announcements`,
  ANNOUNCEMENT_DETAIL: (announcementId: number) =>
    `/api/events/announcements/${announcementId}`,

  // Wedding
  WEDDING_BASE: "/api/weddings",
  WEDDING_DETAIL: (weddingId: number) => `/api/weddings/${weddingId}`,
  HOSTS: (eventId: number) => `/api/events/${eventId}/hosts`,
  HOST_DETAIL: (hostId: number) => `/api/events/hosts/${hostId}`,
  SCHEDULES: (weddingId: number) => `/api/weddings/${weddingId}/schedules`,
  SCHEDULE_DETAIL: (scheduleId: number) =>
    `/api/weddings/schedules/${scheduleId}`,
  ACCOUNT_GROUPS: (weddingId: number) =>
    `/api/weddings/${weddingId}/account-groups`,
  ACCOUNT_GROUP_DETAIL: (groupId: number) =>
    `/api/weddings/account-groups/${groupId}`,
  ACCOUNTS: (groupId: number) =>
    `/api/weddings/account-groups/${groupId}/accounts`,
  ACCOUNT_DETAIL: (accountId: number) => `/api/weddings/accounts/${accountId}`,
} as const;

export const EVENT_VALIDATION = {
  SLUG_PATTERN: /^[a-z0-9-]+$/,
  SLUG_MIN_LENGTH: 4,
  SLUG_MAX_LENGTH: 20,
} as const;
