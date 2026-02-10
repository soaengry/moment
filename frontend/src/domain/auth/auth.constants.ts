export const AUTH_API = {
  LOGIN: "/api/auth/login",
  SIGN_UP: "/api/auth/signup",
  REFRESH: "/api/auth/refresh",
  LOGOUT: "/api/auth/logout",
  ME: "/api/auth/me",
} as const;

export const TOKEN_KEY = {
  ACCESS: "moment_access_token",
  REFRESH: "moment_refresh_token",
} as const;

export const AUTH_VALIDATION = {
  EMAIL_REGEX: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  PASSWORD_MIN_LENGTH: 8,
  PASSWORD_MAX_LENGTH: 20,
  NAME_MIN_LENGTH: 2,
  NAME_MAX_LENGTH: 20,
} as const;
