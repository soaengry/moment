import { type FC, useState, useRef, useCallback, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import {
  IoSearchOutline,
  IoCalendarOutline,
  IoLocationOutline,
  IoHeartOutline,
  IoPeopleOutline,
} from "react-icons/io5";
import { eventApi } from "../../domain/event/api/eventApi";
import type { EventResponse } from "../../domain/event/types";

const TYPE_LABEL: Record<string, string> = {
  WEDDING: "결혼식",
  GATHERING: "모임",
};

const TYPE_COLOR: Record<string, string> = {
  WEDDING: "bg-rose-50 text-rose-500",
  GATHERING: "bg-blue-50 text-blue-500",
};

function formatDate(dateStr: string): string {
  const d = new Date(dateStr);
  return `${d.getFullYear()}. ${d.getMonth() + 1}. ${d.getDate()}`;
}

function highlight(text: string, query: string): React.ReactNode {
  if (!query.trim()) return text;
  const idx = text.toLowerCase().indexOf(query.toLowerCase());
  if (idx === -1) return text;
  return (
    <>
      {text.slice(0, idx)}
      <mark className="bg-primary/15 text-primary font-semibold rounded-sm">
        {text.slice(idx, idx + query.length)}
      </mark>
      {text.slice(idx + query.length)}
    </>
  );
}

interface EventCardProps {
  event: EventResponse;
  query: string;
  onClick: () => void;
}

const EventCard: FC<EventCardProps> = ({ event, query, onClick }) => (
  <motion.button
    type="button"
    onClick={onClick}
    whileTap={{ scale: 0.98 }}
    className="w-full text-left bg-white rounded-2xl border border-gray-100 p-4 shadow-sm hover:shadow-md hover:border-primary/20 transition-all duration-200"
  >
    <div className="flex items-start justify-between gap-2 mb-2">
      <h3 className="text-sm font-semibold text-gray-900 leading-snug flex-1 line-clamp-2">
        {event.title}
      </h3>
      <span className={`text-[10px] font-medium px-2 py-0.5 rounded-full flex-shrink-0 ${TYPE_COLOR[event.type] ?? "bg-gray-100 text-gray-500"}`}>
        {TYPE_LABEL[event.type] ?? event.type}
      </span>
    </div>

    <p className="text-xs text-primary/80 font-mono mb-3">
      /{highlight(event.slug, query)}
    </p>

    <div className="space-y-1.5">
      {event.date && (
        <div className="flex items-center gap-1.5 text-xs text-gray-500">
          <IoCalendarOutline className="flex-shrink-0 text-gray-400" />
          {formatDate(event.date)}
        </div>
      )}
      {event.locationName && (
        <div className="flex items-center gap-1.5 text-xs text-gray-500">
          <IoLocationOutline className="flex-shrink-0 text-gray-400" />
          <span className="truncate">{event.locationName}</span>
        </div>
      )}
    </div>
  </motion.button>
);

const SearchPage: FC = () => {
  const navigate = useNavigate();
  const [query, setQuery] = useState("");
  const [submittedQuery, setSubmittedQuery] = useState("");
  const [results, setResults] = useState<EventResponse[]>([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isFetchingMore, setIsFetchingMore] = useState(false);
  const [searched, setSearched] = useState(false);
  const sentinelRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const loadingMoreRef = useRef(false);

  const fetchResults = useCallback(async (q: string, pageNum: number, replace: boolean) => {
    if (!q.trim()) return;
    if (!replace && loadingMoreRef.current) return;
    if (!replace) loadingMoreRef.current = true;
    pageNum === 0 ? setIsLoading(true) : setIsFetchingMore(true);
    try {
      const data = await eventApi.searchEvents(q.trim(), pageNum);
      setResults(prev => replace ? data.content : [...prev, ...data.content]);
      setHasMore((data.page.number + 1) < data.page.totalPages);
      setPage(pageNum);
    } catch {
      // ignore
    } finally {
      loadingMoreRef.current = false;
      setIsLoading(false);
      setIsFetchingMore(false);
    }
  }, []);

  const handleSearch = () => {
    const trimmed = query.trim();
    if (!trimmed) { inputRef.current?.focus(); return; }
    setSubmittedQuery(trimmed);
    setSearched(true);
    setHasMore(false);
    setPage(0);
    fetchResults(trimmed, 0, true);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") handleSearch();
  };

  // 무한스크롤: sentinel 관찰
  useEffect(() => {
    if (!sentinelRef.current || !hasMore || isFetchingMore) return;
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) {
          fetchResults(submittedQuery, page + 1, false);
        }
      },
      { threshold: 0.1 },
    );
    observer.observe(sentinelRef.current);
    return () => observer.disconnect();
  }, [hasMore, isFetchingMore, submittedQuery, page, fetchResults]);

  return (
    <div className="min-h-screen bg-[#faf9f6]">
      <div className="max-w-lg mx-auto px-4 pt-14 pb-32">
        {/* 헤더 */}
        <div className="mb-6 text-center">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-primary/10 mb-3">
            <IoSearchOutline className="text-3xl text-primary" />
          </div>
          <h1 className="text-xl font-bold text-gray-800">초대장 검색</h1>
          <p className="text-sm text-gray-400 mt-0.5">초대장 ID로 공개 일정을 찾아보세요</p>
        </div>

        {/* 검색 입력 */}
        <div className="flex gap-2 mb-6">
          <input
            ref={inputRef}
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="초대장 ID 입력 (예: readers, wedding-2025)"
            autoCapitalize="none"
            autoCorrect="off"
            spellCheck={false}
            className="flex-1 px-4 py-3.5 rounded-xl border border-gray-200 bg-white text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:border-primary transition-colors"
          />
          <button
            type="button"
            onClick={handleSearch}
            disabled={isLoading}
            className="px-5 py-3.5 rounded-xl bg-primary text-white text-sm font-semibold hover:bg-primary/90 disabled:opacity-50 transition-colors whitespace-nowrap flex items-center gap-1.5"
          >
            {isLoading ? (
              <span className="inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <IoSearchOutline className="text-base" />
            )}
            검색
          </button>
        </div>

        {/* 결과 영역 */}
        <AnimatePresence mode="wait">
          {isLoading && (
            <motion.div
              key="loading"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="flex justify-center py-16"
            >
              <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" />
            </motion.div>
          )}

          {!isLoading && searched && results.length === 0 && (
            <motion.div
              key="empty"
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0 }}
              className="text-center py-16 space-y-2"
            >
              <div className="text-4xl">🔍</div>
              <p className="text-sm font-medium text-gray-600">
                <span className="text-primary">"{submittedQuery}"</span>와 일치하는 공개 일정이 없습니다
              </p>
              <p className="text-xs text-gray-400">다른 키워드로 검색해보세요</p>
            </motion.div>
          )}

          {!isLoading && results.length > 0 && (
            <motion.div
              key="results"
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0 }}
            >
              <div className="flex items-center justify-between mb-3">
                <p className="text-xs text-gray-500">
                  <span className="font-medium text-primary">"{submittedQuery}"</span> 검색 결과
                </p>
                <div className="flex items-center gap-1 text-xs text-gray-400">
                  {results[0].type === "WEDDING"
                    ? <IoHeartOutline className="text-sm" />
                    : <IoPeopleOutline className="text-sm" />}
                  {results.length}건
                </div>
              </div>

              <div className="space-y-3">
                {results.map((event) => (
                  <EventCard
                    key={event.id}
                    event={event}
                    query={submittedQuery}
                    onClick={() => navigate(`/event/${event.slug}`)}
                  />
                ))}
              </div>

              {/* 무한스크롤 sentinel */}
              <div ref={sentinelRef} className="h-4" />

              {isFetchingMore && (
                <div className="flex justify-center py-4">
                  <div className="w-6 h-6 border-2 border-primary border-t-transparent rounded-full animate-spin" />
                </div>
              )}

              {!hasMore && results.length > 0 && (
                <p className="text-center text-xs text-gray-300 py-4">모든 결과를 불러왔습니다</p>
              )}
            </motion.div>
          )}
        </AnimatePresence>

        {/* 미검색 상태 안내 */}
        {!searched && (
          <div className="mt-6 rounded-2xl bg-white border border-gray-100 p-5 space-y-3">
            <p className="text-xs font-semibold text-gray-500 uppercase tracking-wide">검색 방법</p>
            <p className="text-sm text-gray-500 leading-relaxed">
              초대장 ID의 일부만 입력해도 검색됩니다.
            </p>
            <div className="space-y-1.5">
              <div className="rounded-lg bg-gray-50 px-3 py-2">
                <p className="text-xs text-gray-400 font-mono">
                  <span className="text-primary font-semibold">reader</span>
                  {" "}<span className="text-gray-300">→</span>{" "}
                  reader, readers, we-are-readers-087
                </p>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default SearchPage;
