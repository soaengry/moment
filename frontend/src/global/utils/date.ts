const DAYS_KO = ["일", "월", "화", "수", "목", "금", "토"] as const;

/** "2024년 3월 15일 금요일" — 랜딩/날짜섹션용 */
export function formatEventDateLong(dateStr: string): string {
  const d = new Date(dateStr);
  return `${d.getFullYear()}년 ${d.getMonth() + 1}월 ${d.getDate()}일 ${DAYS_KO[d.getDay()]}요일`;
}

/** "2024.03.15" — 방명록용 */
export function formatDotDate(dateStr: string): string {
  const d = new Date(dateStr);
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${d.getFullYear()}.${m}.${day}`;
}

/** "3월 15일" — 마이페이지 일정 카드용 */
export function formatMonthDay(dateStr: string): string {
  const d = new Date(dateStr);
  return `${d.getMonth() + 1}월 ${d.getDate()}일`;
}

/** "오전 9:30" — 채팅 메시지 시간용 */
export function formatChatTime(dateStr: string): string {
  const d = new Date(dateStr);
  const h = d.getHours();
  const m = String(d.getMinutes()).padStart(2, "0");
  const period = h < 12 ? "오전" : "오후";
  const displayH = h === 0 ? 12 : h > 12 ? h - 12 : h;
  return `${period} ${displayH}:${m}`;
}

/** "방금 전 / N분 전 / N시간 전 / M/D" — 피드 댓글·코멘트 시간용 */
export function formatRelativeTime(dateStr: string): string {
  const d = new Date(dateStr);
  const now = new Date();
  const diffMs = now.getTime() - d.getTime();
  const diffMin = Math.floor(diffMs / 60_000);
  if (diffMin < 1) return "방금 전";
  if (diffMin < 60) return `${diffMin}분 전`;
  const diffHour = Math.floor(diffMin / 60);
  if (diffHour < 24) return `${diffHour}시간 전`;
  return `${d.getMonth() + 1}/${d.getDate()}`;
}
