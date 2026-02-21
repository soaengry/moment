export interface ChatRoom {
  id: number;
  weddingId: number;
  name: string;
  createdAt: string;
}

export interface ChatMessage {
  id: number;
  roomId: number;
  userId: number;
  nickname: string;
  profileImageUrl: string | null;
  content: string;
  type: "CHAT" | "JOIN" | "LEAVE";
  createdAt: string;
}

export interface ChatMessageRequest {
  roomId: number;
  content: string;
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
