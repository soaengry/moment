export interface ChatMessage {
  id: string;
  weddingId: number;
  userId: number;
  nickname: string;
  profileImageUrl: string | null;
  content: string;
  imageUrl: string | null;
  type: "CHAT" | "IMAGE";
  createdAt: string;
}

export interface ChatMessageRequest {
  weddingId: number;
  content: string;
  imageUrl?: string;
  type?: string;
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
