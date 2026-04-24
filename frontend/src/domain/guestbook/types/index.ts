export interface GuestbookEntry {
  id: number;
  weddingId: number;
  userId: number | null;
  authorName: string;
  content: string;
  isSecret: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface GuestbookRequest {
  authorName: string;
  content: string;
  password?: string;
  isSecret?: boolean;
}

export interface CursorPageResponse<T> {
  content: T[];
  nextCursor: number | null;
  hasNext: boolean;
}
