/**
 * Backend API 표준 응답 형식
 */
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string | null;
  timestamp: string;
}
