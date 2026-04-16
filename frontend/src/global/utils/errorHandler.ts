import { toast } from "react-toastify";

/** API 에러를 toast로 표시하는 표준 핸들러 */
export function handleApiError(error: unknown, message: string): void {
  console.error(message, error);
  toast.error(message);
}

const MAX_FILE_SIZE_MB = 5;
const MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;

/** 이미지 파일 크기 유효성 검사 (5MB 제한). 초과 시 toast 경고 후 false 반환. */
export function validateImageFile(file: File): boolean {
  if (file.size > MAX_FILE_SIZE_BYTES) {
    toast.error(`파일 크기는 ${MAX_FILE_SIZE_MB}MB 이하여야 합니다.`);
    return false;
  }
  return true;
}
