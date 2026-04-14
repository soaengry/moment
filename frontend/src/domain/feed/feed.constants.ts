export const FEED_API = {
  // Posts
  POSTS: "/api/feed",
  POST: (postId: number) => `/api/feed/${postId}`,
  POST_LIKE: (postId: number) => `/api/feed/${postId}/like`,
  POST_BOOKMARK: (postId: number) => `/api/feed/${postId}/bookmark`,
  POST_COMMENTS: (postId: number) => `/api/feed/${postId}/comments`,
  COMMENT: (commentId: number) => `/api/feed/comments/${commentId}`,
  BOOKMARKS: "/api/feed/bookmarks",

  // Event Feed
  EVENT_POSTS: (eventId: number) => `/api/events/${eventId}/feed/posts`,

  // My Page
  MY_POSTS: "/api/feed/my/posts",
  MY_BOOKMARKS: "/api/feed/my/bookmarks",
  MY_LIKES: "/api/feed/my/likes",
  MY_COMMENTS: "/api/feed/my/comments",
} as const;
