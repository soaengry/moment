import { useEffect, useState } from "react";
import { eventApi } from "../api/eventApi";
import type { EventInfoResponse } from "../types";

export function useEventDetail(slug: string | undefined) {
  const [data, setData] = useState<EventInfoResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!slug) {
      setError("잘못된 접근입니다");
      setIsLoading(false);
      return;
    }
    setIsLoading(true);
    setError(null);
    eventApi
      .getEventInfo(slug)
      .then(setData)
      .catch(() => setError("초대장을 찾을 수 없습니다"))
      .finally(() => setIsLoading(false));
  }, [slug]);

  return { data, isLoading, error };
}
