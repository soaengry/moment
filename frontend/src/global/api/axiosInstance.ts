import axios, { type AxiosError, type InternalAxiosRequestConfig } from "axios";
import { ENV } from "../config/env";
import { tokenStorage, isTokenExpired } from "../../domain/auth/auth.utils";
import { AUTH_API } from "../../domain/auth/auth.constants";
import type { ApiErrorResponse, TokenResponse } from "../../domain/auth/types";
import type { ApiResponse } from "../types/ApiResponse";

const axiosInstance = axios.create({
  baseURL: ENV.API_BASE_URL,
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

let isRefreshing = false;
let failedQueue: Array<{
  resolve: (token: string) => void;
  reject: (error: unknown) => void;
}> = [];

const processQueue = (error: unknown, token: string | null = null): void => {
  failedQueue.forEach((promise) => {
    if (token) {
      promise.resolve(token);
    } else {
      promise.reject(error);
    }
  });
  failedQueue = [];
};

axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = tokenStorage.getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  },
);

axiosInstance.interceptors.response.use(
  (response) => {
    // ApiResponse 형식에서 data 필드 자동 추출
    // data 키가 없는 경우(null 응답 시 @JsonInclude(NON_NULL)으로 제외)도 null로 처리
    if (response.data && typeof response.data === 'object' && 'status' in response.data) {
      const apiResponse = response.data as ApiResponse<unknown>;
      response.data = 'data' in apiResponse ? apiResponse.data : null;
    }
    return response;
  },
  async (error: AxiosError<ApiErrorResponse>) => {
    const originalRequest = error.config;

    if (!originalRequest || error.response?.status !== 401) {
      return Promise.reject(error);
    }

    const isAuthRequest =
      originalRequest.url === AUTH_API.REFRESH ||
      originalRequest.url === AUTH_API.LOGIN ||
      originalRequest.url === AUTH_API.OAUTH2_TOKEN;

    if (isAuthRequest) {
      tokenStorage.clearTokens();
      window.location.href = "/login";
      return Promise.reject(error);
    }

    if (isRefreshing) {
      return new Promise<string>((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      }).then((token) => {
        originalRequest.headers.Authorization = `Bearer ${token}`;
        return axiosInstance(originalRequest);
      });
    }

    isRefreshing = true;

    try {
      const refreshToken = tokenStorage.getRefreshToken();
      if (!refreshToken || isTokenExpired(refreshToken)) {
        throw new Error("Refresh token expired");
      }

      const { data } = await axiosInstance.post<TokenResponse>(
        AUTH_API.REFRESH,
        { refreshToken },
      );

      tokenStorage.setTokens(data.accessToken, data.refreshToken);
      processQueue(null, data.accessToken);

      originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
      return axiosInstance(originalRequest);
    } catch (refreshError) {
      processQueue(refreshError, null);
      tokenStorage.clearTokens();
      window.location.href = "/login";
      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  },
);

export default axiosInstance;
