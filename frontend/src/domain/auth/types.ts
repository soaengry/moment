// ─── Request ───

export interface SignupRequest {
  email: string;
  password: string;
  nickname: string;
}

export interface LoginRequest {
  email: string;
  password: string;
  deviceId?: string;
  deviceName?: string;
}

export interface RefreshRequest {
  refreshToken: string;
}

export interface UpdateProfileRequest {
  nickname?: string;
  profileImageUrl?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface RestoreAccountRequest {
  email: string;
  password: string;
}

// ─── Response ───

export interface SignupResponse {
  userId: number;
  email: string;
  message: string;
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface UserResponse {
  id: number;
  email: string;
  nickname: string;
  profileImageUrl: string | null;
  role: string;
  authProvider: string;
  isEmailVerified: boolean;
  createdAt: string;
}

export interface MessageResponse {
  message: string;
}

export interface CheckResponse {
  exists: boolean;
}

// ─── Auth Store ───

export interface AuthState {
  user: UserResponse | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  setAuth: (token: TokenResponse, user: UserResponse) => void;
  setAccessToken: (token: string) => void;
  setUser: (user: UserResponse) => void;
  logout: () => void;
  setLoading: (loading: boolean) => void;
}

// ─── Error ───

export interface FormFieldError {
  field: string;
  message: string;
}

export interface ApiErrorResponse {
  status: number;
  message: string;
  errors?: FormFieldError[];
}
