import axiosInstance from "../../../global/api/axiosInstance";
import { FEED_API } from "../feed.constants";
import type {
  PostResponse,
  PostRequest,
  CommentResponse,
  CommentRequest,
  CursorPageResponse,
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
  getFeed: async (cursor?: number, size = 20): Promise<CursorPageResponse<PostResponse>> => {
    const params = new URLSearchParams({ size: String(size) });
    if (cursor != null) params.set("cursor", String(cursor));
    const { data } = await axiosInstance.get<CursorPageResponse<PostResponse>>(
      `${FEED_API.POSTS}?${params}`,
    );
    return data;
  },

  getPost: async (postId: number): Promise<PostResponse> => {
    const { data } = await axiosInstance.get<PostResponse>(FEED_API.POST(postId));
    return data;
  },

  createPost: async (request: PostRequest): Promise<PostResponse> => {
    const { data } = await axiosInstance.post<PostResponse>(FEED_API.POSTS, request);
    return data;
  },

  updatePost: async (postId: number, request: PostRequest): Promise<PostResponse> => {
    const { data } = await axiosInstance.put<PostResponse>(FEED_API.POST(postId), request);
    return data;
  },

  deletePost: async (postId: number): Promise<void> => {
    await axiosInstance.delete(FEED_API.POST(postId));
  },

  // Like
  toggleLike: async (postId: number): Promise<{ liked: boolean }> => {
    const { data } = await axiosInstance.post<{ liked: boolean }>(FEED_API.POST_LIKE(postId));
    return data;
  },

  // Bookmark
  toggleBookmark: async (postId: number): Promise<{ bookmarked: boolean }> => {
    const { data } = await axiosInstance.post<{ bookmarked: boolean }>(FEED_API.POST_BOOKMARK(postId));
    return data;
  },

  getBookmarks: async (cursor?: number, size = 20): Promise<CursorPageResponse<PostResponse>> => {
    const params = new URLSearchParams({ size: String(size) });
    if (cursor != null) params.set("cursor", String(cursor));
    const { data } = await axiosInstance.get<CursorPageResponse<PostResponse>>(
      `${FEED_API.BOOKMARKS}?${params}`,
    );
    return data;
  },

  // Comments
  getComments: async (postId: number, cursor?: number, size = 30): Promise<CursorPageResponse<CommentResponse>> => {
    const params = new URLSearchParams({ size: String(size) });
    if (cursor != null) params.set("cursor", String(cursor));
    const { data } = await axiosInstance.get<CursorPageResponse<CommentResponse>>(
      `${FEED_API.POST_COMMENTS(postId)}?${params}`,
    );
    return data;
  },

  createComment: async (postId: number, request: CommentRequest): Promise<CommentResponse> => {
    const { data } = await axiosInstance.post<CommentResponse>(
      FEED_API.POST_COMMENTS(postId),
      request,
    );
    return data;
  },

  updateComment: async (commentId: number, request: CommentRequest): Promise<CommentResponse> => {
    const { data } = await axiosInstance.put<CommentResponse>(FEED_API.COMMENT(commentId), request);
    return data;
  },

  deleteComment: async (commentId: number): Promise<void> => {
    await axiosInstance.delete(FEED_API.COMMENT(commentId));
  },

  // Event Feed
  getEventFeed: async (eventId: number, cursor?: number, size = 20): Promise<CursorPageResponse<PostResponse>> => {
    const params = new URLSearchParams({ size: String(size) });
    if (cursor != null) params.set("cursor", String(cursor));
    const { data } = await axiosInstance.get<CursorPageResponse<PostResponse>>(
      `${FEED_API.EVENT_POSTS(eventId)}?${params}`,
    );
    return data;
  },

  createEventPost: async (eventId: number, request: PostRequest): Promise<PostResponse> => {
    const { data } = await axiosInstance.post<PostResponse>(
      FEED_API.EVENT_POSTS(eventId),
      request,
    );
    return data;
  },

  // My Page
  getMyPosts: async (cursor?: number, size = 20, eventId?: number): Promise<CursorPageResponse<PostResponse>> => {
    const params = new URLSearchParams({ size: String(size) });
    if (cursor != null) params.set("cursor", String(cursor));
    if (eventId != null) params.set("eventId", String(eventId));
    const { data } = await axiosInstance.get<CursorPageResponse<PostResponse>>(
      `${FEED_API.MY_POSTS}?${params}`,
    );
    return data;
  },

  getMyBookmarks: async (cursor?: number, size = 20, eventId?: number): Promise<CursorPageResponse<PostResponse>> => {
    const params = new URLSearchParams({ size: String(size) });
    if (cursor != null) params.set("cursor", String(cursor));
    if (eventId != null) params.set("eventId", String(eventId));
    const { data } = await axiosInstance.get<CursorPageResponse<PostResponse>>(
      `${FEED_API.MY_BOOKMARKS}?${params}`,
    );
    return data;
  },

  getMyLikes: async (cursor?: number, size = 20, eventId?: number): Promise<CursorPageResponse<PostResponse>> => {
    const params = new URLSearchParams({ size: String(size) });
    if (cursor != null) params.set("cursor", String(cursor));
    if (eventId != null) params.set("eventId", String(eventId));
    const { data } = await axiosInstance.get<CursorPageResponse<PostResponse>>(
      `${FEED_API.MY_LIKES}?${params}`,
    );
    return data;
  },

  getMyComments: async (cursor?: number, size = 20, eventId?: number): Promise<CursorPageResponse<CommentResponse>> => {
    const params = new URLSearchParams({ size: String(size) });
    if (cursor != null) params.set("cursor", String(cursor));
    if (eventId != null) params.set("eventId", String(eventId));
    const { data } = await axiosInstance.get<CursorPageResponse<CommentResponse>>(
      `${FEED_API.MY_COMMENTS}?${params}`,
    );
    return data;
  },
};
