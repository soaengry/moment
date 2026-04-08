export const CHAT_API = {
  MESSAGES: (eventId: number) => `/api/events/${eventId}/chat/messages`,
  UPLOAD_IMAGE: (eventId: number) => `/api/events/${eventId}/chat/images`,
} as const;
