import axiosInstance from "../../../global/api/axiosInstance";
import type {
  PostResponse,
  PostRequest,
  CommentResponse,
  CommentRequest,
  PageResponse,
} from "../types";

export const feedApi = {
  // File Upload
  uploadImage: async (file: File): Promise<string> => {
    const formData = new FormData();
    formData.append("file", file);
    const { data } = await axiosInstance.post<{ url: string }>(
      "/api/files/upload",
      formData,
      { headers: { "Content-Type": "multipart/form-data" } },
    );
    return data.url;
  },

  // Posts
  getFeed: async (page = 0, size = 20): Promise<PageResponse<PostResponse>> => {
    const { data } = await axiosInstance.get<PageResponse<PostResponse>>(
      `/api/feed/posts?page=${page}&size=${size}`
    );
    return data;
  },

  getPost: async (postId: number): Promise<PostResponse> => {
    const { data } = await axiosInstance.get<PostResponse>(`/api/feed/posts/${postId}`);
    return data;
  },

  createPost: async (request: PostRequest): Promise<PostResponse> => {
    const { data } = await axiosInstance.post<PostResponse>("/api/feed/posts", request);
    return data;
  },

  updatePost: async (postId: number, request: PostRequest): Promise<PostResponse> => {
    const { data } = await axiosInstance.put<PostResponse>(`/api/feed/posts/${postId}`, request);
    return data;
  },

  deletePost: async (postId: number): Promise<void> => {
    await axiosInstance.delete(`/api/feed/posts/${postId}`);
  },

  // Like
  toggleLike: async (postId: number): Promise<{ liked: boolean }> => {
    const { data } = await axiosInstance.post<{ liked: boolean }>(`/api/feed/posts/${postId}/like`);
    return data;
  },

  // Bookmark
  toggleBookmark: async (postId: number): Promise<{ bookmarked: boolean }> => {
    const { data } = await axiosInstance.post<{ bookmarked: boolean }>(`/api/feed/posts/${postId}/bookmark`);
    return data;
  },

  getBookmarks: async (page = 0, size = 20): Promise<PageResponse<PostResponse>> => {
    const { data } = await axiosInstance.get<PageResponse<PostResponse>>(
      `/api/feed/bookmarks?page=${page}&size=${size}`
    );
    return data;
  },

  // Comments
  getComments: async (postId: number, page = 0, size = 30): Promise<PageResponse<CommentResponse>> => {
    const { data } = await axiosInstance.get<PageResponse<CommentResponse>>(
      `/api/feed/posts/${postId}/comments?page=${page}&size=${size}`
    );
    return data;
  },

  createComment: async (postId: number, request: CommentRequest): Promise<CommentResponse> => {
    const { data } = await axiosInstance.post<CommentResponse>(
      `/api/feed/posts/${postId}/comments`,
      request
    );
    return data;
  },

  updateComment: async (commentId: number, request: CommentRequest): Promise<CommentResponse> => {
    const { data } = await axiosInstance.put<CommentResponse>(`/api/feed/comments/${commentId}`, request);
    return data;
  },

  deleteComment: async (commentId: number): Promise<void> => {
    await axiosInstance.delete(`/api/feed/comments/${commentId}`);
  },

  // Wedding Feed
  getWeddingFeed: async (weddingId: number, page = 0, size = 20): Promise<PageResponse<PostResponse>> => {
    const { data } = await axiosInstance.get<PageResponse<PostResponse>>(
      `/api/weddings/${weddingId}/feed/posts?page=${page}&size=${size}`
    );
    return data;
  },

  createWeddingPost: async (weddingId: number, request: PostRequest): Promise<PostResponse> => {
    const { data } = await axiosInstance.post<PostResponse>(
      `/api/weddings/${weddingId}/feed/posts`,
      request
    );
    return data;
  },

  updateWeddingPost: async (weddingId: number, postId: number, request: PostRequest): Promise<PostResponse> => {
    const { data } = await axiosInstance.put<PostResponse>(
      `/api/weddings/${weddingId}/feed/posts/${postId}`,
      request
    );
    return data;
  },

  deleteWeddingPost: async (weddingId: number, postId: number): Promise<void> => {
    await axiosInstance.delete(`/api/weddings/${weddingId}/feed/posts/${postId}`);
  },

  toggleWeddingLike: async (weddingId: number, postId: number): Promise<{ liked: boolean }> => {
    const { data } = await axiosInstance.post<{ liked: boolean }>(
      `/api/weddings/${weddingId}/feed/posts/${postId}/like`
    );
    return data;
  },

  toggleWeddingBookmark: async (weddingId: number, postId: number): Promise<{ bookmarked: boolean }> => {
    const { data } = await axiosInstance.post<{ bookmarked: boolean }>(
      `/api/weddings/${weddingId}/feed/posts/${postId}/bookmark`
    );
    return data;
  },

  getWeddingComments: async (weddingId: number, postId: number, page = 0, size = 30): Promise<PageResponse<CommentResponse>> => {
    const { data } = await axiosInstance.get<PageResponse<CommentResponse>>(
      `/api/weddings/${weddingId}/feed/posts/${postId}/comments?page=${page}&size=${size}`
    );
    return data;
  },

  createWeddingComment: async (weddingId: number, postId: number, request: CommentRequest): Promise<CommentResponse> => {
    const { data } = await axiosInstance.post<CommentResponse>(
      `/api/weddings/${weddingId}/feed/posts/${postId}/comments`,
      request
    );
    return data;
  },

  // My Page
  getMyPosts: async (page = 0, size = 20, weddingId?: number): Promise<PageResponse<PostResponse>> => {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (weddingId != null) params.set("weddingId", String(weddingId));
    const { data } = await axiosInstance.get<PageResponse<PostResponse>>(
      `/api/feed/my/posts?${params}`
    );
    return data;
  },

  getMyBookmarks: async (page = 0, size = 20, weddingId?: number): Promise<PageResponse<PostResponse>> => {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (weddingId != null) params.set("weddingId", String(weddingId));
    const { data } = await axiosInstance.get<PageResponse<PostResponse>>(
      `/api/feed/my/bookmarks?${params}`
    );
    return data;
  },

  getMyLikes: async (page = 0, size = 20, weddingId?: number): Promise<PageResponse<PostResponse>> => {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (weddingId != null) params.set("weddingId", String(weddingId));
    const { data } = await axiosInstance.get<PageResponse<PostResponse>>(
      `/api/feed/my/likes?${params}`
    );
    return data;
  },

  getMyComments: async (page = 0, size = 20, weddingId?: number): Promise<PageResponse<CommentResponse>> => {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (weddingId != null) params.set("weddingId", String(weddingId));
    const { data } = await axiosInstance.get<PageResponse<CommentResponse>>(
      `/api/feed/my/comments?${params}`
    );
    return data;
  },
};
