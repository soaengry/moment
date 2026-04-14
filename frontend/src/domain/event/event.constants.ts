export const EVENT_API = {
  BASE: "/api/events",
  DETAIL: (id: number) => `/api/events/${id}`,
  UPDATE_DETAILS: (id: number) => `/api/events/${id}/details`,
  INFO: (slug: string) => `/api/events/${slug}`,
  CHECK_SLUG: "/api/events/check-slug",
} as const;

export const EVENT_VALIDATION = {
  SLUG_PATTERN: /^[a-z0-9-]+$/,
  SLUG_MIN_LENGTH: 4,
  SLUG_MAX_LENGTH: 50,
} as const;
