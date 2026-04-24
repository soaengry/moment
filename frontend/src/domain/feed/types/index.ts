export interface AuthorInfo {
  id: number;
  nickname: string;
  profileImageUrl: string | null;
}

export interface PostResponse {
  id: number;
  author: AuthorInfo;
  content: string;
  imageUrls: string[];
  likeCount: number;
  commentCount: number;
  isLiked: boolean;
  isBookmarked: boolean;
  eventId: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface CommentResponse {
  id: number;
  postId: number;
  author: AuthorInfo;
  content: string;
  createdAt: string;
  updatedAt: string;
}

export interface PostRequest {
  content: string;
  imageUrls?: string[];
}

export interface CommentRequest {
  content: string;
}

export interface CursorPageResponse<T> {
  content: T[];
  nextCursor: number | null;
  hasNext: boolean;
}
