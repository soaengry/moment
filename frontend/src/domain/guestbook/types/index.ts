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

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  last: boolean;
  first: boolean;
}
