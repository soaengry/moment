export interface User {
  id: number;
  email: string;
  name: string;
  role: "GUEST" | "COUPLE" | "ADMIN";
  createdAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignUpRequest {
  email: string;
  password: string;
  name: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface TokenRefreshResponse {
  accessToken: string;
}

export interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  setAuth: (response: AuthResponse) => void;
  setAccessToken: (token: string) => void;
  logout: () => void;
  setLoading: (loading: boolean) => void;
}

export interface FormFieldError {
  field: string;
  message: string;
}

export interface ApiErrorResponse {
  status: number;
  message: string;
  errors?: FormFieldError[];
}
