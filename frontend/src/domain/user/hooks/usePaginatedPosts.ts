import { useState, useCallback, useEffect } from "react";
import type { CursorPageResponse } from "../../feed/types";
import { handleApiError } from "../../../global/utils/errorHandler";

interface UsePaginatedResult<T> {
  items: T[];
  hasMore: boolean;
  isLoading: boolean;
  loadMore: () => void;
  reset: () => void;
  setItems: React.Dispatch<React.SetStateAction<T[]>>;
}

export function usePaginatedPosts<T>(
  fetchFn: (cursor?: number) => Promise<CursorPageResponse<T>>,
  errorMessage = "게시글을 불러오지 못했습니다.",
): UsePaginatedResult<T> {
  const [items, setItems] = useState<T[]>([]);
  const [nextCursor, setNextCursor] = useState<number | undefined>(undefined);
  const [hasMore, setHasMore] = useState(true);
  const [isLoading, setIsLoading] = useState(false);

  const fetch = useCallback(
    async (cursor?: number, append = false) => {
      setIsLoading(true);
      try {
        const res = await fetchFn(cursor);
        if (append) {
          setItems((prev) => [...prev, ...res.content]);
        } else {
          setItems(res.content);
        }
        setNextCursor(res.nextCursor ?? undefined);
        setHasMore(res.hasNext);
      } catch (error) {
        handleApiError(error, errorMessage);
      } finally {
        setIsLoading(false);
      }
    },
    [fetchFn, errorMessage],
  );

  useEffect(() => {
    fetch(undefined);
  }, [fetch]);

  const loadMore = useCallback(() => {
    if (nextCursor != null) fetch(nextCursor, true);
  }, [nextCursor, fetch]);

  const reset = useCallback(() => {
    fetch(undefined, false);
  }, [fetch]);

  return { items, hasMore, isLoading, loadMore, reset, setItems };
}
