export const AUTH_API = {
  LOGIN: "/api/auth/login",
  SIGN_UP: "/api/auth/signup",
  VERIFY_EMAIL: "/api/auth/verify-email",
  RESEND_VERIFICATION: "/api/auth/resend-verification",
  REFRESH: "/api/auth/refresh",
  LOGOUT: "/api/auth/logout",
  CHECK_EMAIL: "/api/auth/check-email",
  CHECK_EMAIL_VERIFIED: "/api/auth/check-email-verified",
  CHECK_NICKNAME: "/api/auth/check-nickname",
  SEND_SIGNUP_VERIFICATION: "/api/auth/send-signup-verification",
  VERIFICATION_STATUS: "/api/auth/verification-status",
  OAUTH2_TOKEN: "/api/auth/oauth2/token",
  PASSWORD_RESET_REQUEST: "/api/auth/password-reset/request",
  PASSWORD_RESET_CONFIRM: "/api/auth/password-reset/confirm",
} as const;

export const USER_API = {
  ME: "/api/users/me",
  PASSWORD: "/api/users/me/password",
  RESTORE: "/api/users/restore",
} as const;

export const TOKEN_KEY = {
  ACCESS: "moment_access_token",
  REFRESH: "moment_refresh_token",
  DEVICE_ID: "moment_device_id",
} as const;

export const AUTH_VALIDATION = {
  EMAIL_REGEX: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  PASSWORD_MIN_LENGTH: 8,
  PASSWORD_MAX_LENGTH: 20,
  NICKNAME_MIN_LENGTH: 2,
  NICKNAME_MAX_LENGTH: 50,
  PASSWORD_PATTERN:
    /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[~!@#$%^&*()_+<>?,./-=]).{8,}$/,
} as const;
