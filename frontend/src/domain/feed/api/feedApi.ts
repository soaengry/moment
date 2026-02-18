import axiosInstance from "../../../global/api/axiosInstance";
import type {
  PostResponse,
  PostRequest,
  CommentResponse,
  CommentRequest,
  PageResponse,
} from "../types";

export const feedApi = {
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
};
