export const ATTENDANCE_API = {
  BASE: "/api/attendances",
  DETAIL: (id: number) => `/api/attendances/${id}`,
} as const;

export const DAYS_OF_WEEK = ["일", "월", "화", "수", "목", "금", "토"] as const;

export const PASTEL_COLORS = [
  "bg-pink-100 text-pink-700",
  "bg-blue-100 text-blue-700",
  "bg-green-100 text-green-700",
  "bg-purple-100 text-purple-700",
  "bg-yellow-100 text-yellow-700",
  "bg-orange-100 text-orange-700",
  "bg-teal-100 text-teal-700",
] as const;
