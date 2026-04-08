export const FEED_API = {
  // Global Feed
  POSTS: "/api/feed/posts",
  POST: (postId: number) => `/api/feed/posts/${postId}`,
  POST_LIKE: (postId: number) => `/api/feed/posts/${postId}/like`,
  POST_BOOKMARK: (postId: number) => `/api/feed/posts/${postId}/bookmark`,
  POST_COMMENTS: (postId: number) => `/api/feed/posts/${postId}/comments`,
  COMMENT: (commentId: number) => `/api/feed/comments/${commentId}`,
  BOOKMARKS: "/api/feed/bookmarks",

  // Event Feed
  EVENT_POSTS: (eventId: number) => `/api/events/${eventId}/feed/posts`,
  EVENT_POST: (eventId: number, postId: number) => `/api/events/${eventId}/feed/posts/${postId}`,
  EVENT_POST_LIKE: (eventId: number, postId: number) => `/api/events/${eventId}/feed/posts/${postId}/like`,
  EVENT_POST_BOOKMARK: (eventId: number, postId: number) => `/api/events/${eventId}/feed/posts/${postId}/bookmark`,
  EVENT_POST_COMMENTS: (eventId: number, postId: number) => `/api/events/${eventId}/feed/posts/${postId}/comments`,

  // My Page
  MY_POSTS: "/api/feed/my/posts",
  MY_BOOKMARKS: "/api/feed/my/bookmarks",
  MY_LIKES: "/api/feed/my/likes",
  MY_COMMENTS: "/api/feed/my/comments",
} as const;
