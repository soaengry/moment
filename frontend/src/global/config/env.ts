export const ENV = {
  API_BASE_URL: import.meta.env.VITE_API_BASE_URL ?? "",
  OAUTH2: "/oauth2/authorization",
} as const;
