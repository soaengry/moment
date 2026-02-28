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
  weddingId: number | null;
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

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  last: boolean;
  first: boolean;
}
