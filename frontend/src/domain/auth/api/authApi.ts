import axiosInstance from "../../../global/api/axiosInstance";
import { AUTH_API, USER_API } from "../auth.constants";
import { deviceStorage, tokenStorage } from "../auth.utils";
import type {
  LoginRequest,
  SignupRequest,
  SignupResponse,
  TokenResponse,
  UserResponse,
  MessageResponse,
  CheckResponse,
  VerificationStatusResponse,
  UpdateProfileRequest,
  ChangePasswordRequest,
  RestoreAccountRequest,
} from "../types";

export const authApi = {
  // ─── Auth ───

  signup: async (request: SignupRequest): Promise<SignupResponse> => {
    const { data } = await axiosInstance.post<SignupResponse>(
      AUTH_API.SIGN_UP,
      request,
    );
    return data;
  },

  verifyEmail: async (token: string): Promise<MessageResponse> => {
    const { data } = await axiosInstance.get<MessageResponse>(
      AUTH_API.VERIFY_EMAIL,
      { params: { token } },
    );
    return data;
  },

  sendSignupVerification: async (email: string): Promise<MessageResponse> => {
    const { data } = await axiosInstance.post<MessageResponse>(
      AUTH_API.SEND_SIGNUP_VERIFICATION,
      { email },
    );
    return data;
  },

  getVerificationStatus: async (email: string): Promise<VerificationStatusResponse> => {
    const { data } = await axiosInstance.get<VerificationStatusResponse>(
      AUTH_API.VERIFICATION_STATUS,
      { params: { email } },
    );
    return data;
  },

  resendVerification: async (email: string): Promise<MessageResponse> => {
    const { data } = await axiosInstance.post<MessageResponse>(
      AUTH_API.RESEND_VERIFICATION,
      { email },
    );
    return data;
  },

  login: async (
    request: Omit<LoginRequest, "deviceId" | "deviceName">,
  ): Promise<TokenResponse> => {
    const { data } = await axiosInstance.post<TokenResponse>(AUTH_API.LOGIN, {
      ...request,
      deviceId: deviceStorage.getDeviceId(),
      deviceName: deviceStorage.getDeviceName(),
    });
    return data;
  },

  logout: async (): Promise<MessageResponse> => {
    const refreshToken = tokenStorage.getRefreshToken();
    const { data } = await axiosInstance.post<MessageResponse>(
      AUTH_API.LOGOUT,
      { refreshToken },
    );
    return data;
  },

  refresh: async (refreshToken: string): Promise<TokenResponse> => {
    const { data } = await axiosInstance.post<TokenResponse>(AUTH_API.REFRESH, {
      refreshToken,
    });
    return data;
  },

  checkEmail: async (email: string): Promise<CheckResponse> => {
    const { data } = await axiosInstance.post<CheckResponse>(
      AUTH_API.CHECK_EMAIL,
      { email },
    );
    return data;
  },

  checkEmailVerified: async (email: string): Promise<{ verified: boolean }> => {
    const { data } = await axiosInstance.get<{ verified: boolean }>(
      AUTH_API.CHECK_EMAIL_VERIFIED,
      { params: { email } },
    );
    return data;
  },

  checkNickname: async (nickname: string): Promise<CheckResponse> => {
    const { data } = await axiosInstance.post<CheckResponse>(
      AUTH_API.CHECK_NICKNAME,
      { nickname },
    );
    return data;
  },

  exchangeOAuth2Token: async (code: string): Promise<TokenResponse> => {
    const { data } = await axiosInstance.get<TokenResponse>(
      AUTH_API.OAUTH2_TOKEN,
      { params: { code } },
    );
    return data;
  },

  // ─── User ───

  getMe: async (): Promise<UserResponse> => {
    const { data } = await axiosInstance.get<UserResponse>(USER_API.ME);
    return data;
  },

  updateProfile: async (
    request: UpdateProfileRequest,
  ): Promise<UserResponse> => {
    const { data } = await axiosInstance.patch<UserResponse>(
      USER_API.ME,
      request,
    );
    return data;
  },

  changePassword: async (
    request: ChangePasswordRequest,
  ): Promise<MessageResponse> => {
    const { data } = await axiosInstance.patch<MessageResponse>(
      USER_API.PASSWORD,
      request,
    );
    return data;
  },

  deleteAccount: async (): Promise<MessageResponse> => {
    const { data } = await axiosInstance.delete<MessageResponse>(USER_API.ME);
    return data;
  },

  restoreAccount: async (
    request: RestoreAccountRequest,
  ): Promise<MessageResponse> => {
    const { data } = await axiosInstance.post<MessageResponse>(
      USER_API.RESTORE,
      request,
    );
    return data;
  },
};
