import { create } from "zustand";
import type { AuthState, TokenResponse, UserResponse } from "../types";
import { tokenStorage } from "../auth.utils";

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  accessToken: null,
  refreshToken: null,
  isAuthenticated: false,
  isLoading: true,

  setAuth: (token: TokenResponse, user: UserResponse) => {
    tokenStorage.setTokens(token.accessToken, token.refreshToken);
    set({
      user,
      accessToken: token.accessToken,
      refreshToken: token.refreshToken,
      isAuthenticated: true,
      isLoading: false,
    });
  },

  setAccessToken: (accessToken: string) => {
    const refreshToken = tokenStorage.getRefreshToken();
    if (refreshToken) {
      tokenStorage.setTokens(accessToken, refreshToken);
    }
    set({ accessToken });
  },

  setUser: (user: UserResponse) => {
    set({ user });
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
