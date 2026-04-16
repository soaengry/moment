import { useState, useCallback, useEffect } from "react";
import type { PostResponse, PageResponse } from "../../feed/types";
import { handleApiError } from "../../../global/utils/errorHandler";

interface UsePaginatedPostsResult {
  posts: PostResponse[];
  page: number;
  hasMore: boolean;
  isLoading: boolean;
  fetchPosts: (pageNum: number, append?: boolean) => Promise<void>;
  setPosts: React.Dispatch<React.SetStateAction<PostResponse[]>>;
}

export function usePaginatedPosts(
  fetchFn: (page: number) => Promise<PageResponse<PostResponse>>,
  errorMessage = "게시글을 불러오지 못했습니다.",
): UsePaginatedPostsResult {
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [isLoading, setIsLoading] = useState(false);

  const fetchPosts = useCallback(
    async (pageNum: number, append = false) => {
      setIsLoading(true);
      try {
        const res = await fetchFn(pageNum);
        if (append) {
          setPosts((prev) => [...prev, ...res.content]);
        } else {
          setPosts(res.content);
        }
        setHasMore(!res.last);
        setPage(pageNum);
      } catch (error) {
        handleApiError(error, errorMessage);
      } finally {
        setIsLoading(false);
      }
    },
    [fetchFn, errorMessage],
  );

  useEffect(() => {
    fetchPosts(0);
  }, [fetchPosts]);

  return { posts, page, hasMore, isLoading, fetchPosts, setPosts };
}
