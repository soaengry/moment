/**
 * Backend API 표준 응답 형식
 */
export interface ApiStatus {
  code: number;
  message: string;
}

export interface ApiResponse<T> {
  status: ApiStatus;
  data: T;
}
