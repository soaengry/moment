export const RSVP_API = {
  BASE: "/api/rsvp",
  ME: "/api/rsvp/me",
  STATS: "/api/rsvp/stats",
  LIST: "/api/rsvp/list",
  EXPORT: "/api/rsvp/export",
} as const;

export const RSVP_VALIDATION = {
  PAGE_SIZE: 20,
  NOTE_MAX_LENGTH: 50,
  MAX_ATTENDEE_COUNT: 99,
  MAX_MEAL_COUNT: 99,
  MAX_SHUTTLE_COUNT: 99,
} as const;
