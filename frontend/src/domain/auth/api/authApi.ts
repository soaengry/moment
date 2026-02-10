import axiosInstance from "../../../global/api/axiosInstance";
import { AUTH_API } from "../auth.constants";
import type { LoginRequest, SignUpRequest, AuthResponse, User } from "../types";

export const authApi = {
  login: async (request: LoginRequest): Promise<AuthResponse> => {
    const { data } = await axiosInstance.post<AuthResponse>(
      AUTH_API.LOGIN,
      request,
    );
    return data;
  },

  signUp: async (request: SignUpRequest): Promise<AuthResponse> => {
    const { data } = await axiosInstance.post<AuthResponse>(
      AUTH_API.SIGN_UP,
      request,
    );
    return data;
  },

  logout: async (): Promise<void> => {
    await axiosInstance.post(AUTH_API.LOGOUT);
  },

  getMe: async (): Promise<User> => {
    const { data } = await axiosInstance.get<User>(AUTH_API.ME);
    return data;
  },
};
