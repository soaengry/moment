export const ENV = {
  API_BASE_URL: import.meta.env.VITE_API_BASE_URL ?? "",
  OAUTH2: import.meta.env.VITE_OAUTH2_BASE_URL ?? "/oauth2/authorization",
  KAKAO_MAP_KEY: import.meta.env.VITE_KAKAO_MAP_KEY ?? "",
} as const;
