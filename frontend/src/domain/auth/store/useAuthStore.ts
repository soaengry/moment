import { create } from "zustand";
import type { AuthState, AuthResponse } from "../types";
import { tokenStorage } from "../auth.utils";

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  accessToken: null,
  refreshToken: null,
  isAuthenticated: false,
  isLoading: true,

  setAuth: (response: AuthResponse) => {
    tokenStorage.setTokens(response.accessToken, response.refreshToken);
    set({
      user: response.user,
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
      isAuthenticated: true,
      isLoading: false,
    });
  },

  setAccessToken: (token: string) => {
    const refreshToken = tokenStorage.getRefreshToken();
    if (refreshToken) {
      tokenStorage.setTokens(token, refreshToken);
    }
    set({ accessToken: token });
  },

  logout: () => {
    tokenStorage.clearTokens();
    set({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      isLoading: false,
    });
  },

  setLoading: (loading: boolean) => {
    set({ isLoading: loading });
  },
}));
