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

const TYPE_GRADIENT: Record<string, string> = {
  WEDDING: "from-rose-100 to-pink-200",
  GATHERING: "from-indigo-100 to-blue-200",
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

interface EventCardData extends EventResponse {
  heroImageUrl: string | null;
}

async function attachHeroImages(events: EventResponse[]): Promise<EventCardData[]> {
  const results = await Promise.allSettled(
    events.map((e) => eventApi.getEventInfo(e.slug)),
  );
  return events.map((event, i) => {
    let heroImageUrl: string | null = null;
    const result = results[i];
    if (result.status === "fulfilled") {
      const images = result.value.heroImages;
      const hero = images.find((img) => img.orderIndex === 0) ?? images[0] ?? null;
      heroImageUrl = hero ? (hero.thumbnailUrl ?? hero.imageUrl) : null;
    }
    return { ...event, heroImageUrl };
  });
}

interface EventCardProps {
  event: EventCardData;
  query: string;
  onClick: () => void;
}

const EventCard: FC<EventCardProps> = ({ event, query, onClick }) => (
  <motion.button
    type="button"
    onClick={onClick}
    whileTap={{ scale: 0.98 }}
    className="w-full text-left bg-white rounded-2xl border border-gray-100 p-4 shadow-sm hover:shadow-md hover:border-primary/20 transition-all duration-200 flex gap-3"
  >
    {/* 히어로 이미지 */}
    <div className="shrink-0 w-16 h-16 rounded-xl overflow-hidden">
      {event.heroImageUrl ? (
        <img
          src={event.heroImageUrl}
          alt={event.title}
          className="w-full h-full object-cover"
        />
      ) : (
        <div className={`w-full h-full bg-gradient-to-br ${TYPE_GRADIENT[event.type] ?? "from-gray-100 to-gray-200"}`} />
      )}
    </div>

    {/* 이벤트 정보 */}
    <div className="flex-1 min-w-0">
      <div className="flex items-start justify-between gap-2 mb-1">
        <h3 className="text-sm font-semibold text-gray-900 leading-snug flex-1 line-clamp-2">
          {event.title}
        </h3>
        <span className={`text-[10px] font-medium px-2 py-0.5 rounded-full flex-shrink-0 ${TYPE_COLOR[event.type] ?? "bg-gray-100 text-gray-500"}`}>
          {TYPE_LABEL[event.type] ?? event.type}
        </span>
      </div>

      <p className="text-xs text-primary/80 font-mono mb-2">
        /{highlight(event.slug, query)}
      </p>

      <div className="space-y-1">
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
    </div>
  </motion.button>
);

const SearchPage: FC = () => {
  const navigate = useNavigate();
  const [query, setQuery] = useState("");
  const [submittedQuery, setSubmittedQuery] = useState("");
  const [isSearchMode, setIsSearchMode] = useState(false);

  // 기본 목록 상태 (검색 전)
  const [browseItems, setBrowseItems] = useState<EventCardData[]>([]);
  const [browsePage, setBrowsePage] = useState(0);
  const [browseHasMore, setBrowseHasMore] = useState(false);
  const [browseLoading, setBrowseLoading] = useState(true);
  const [browseFetchingMore, setBrowseFetchingMore] = useState(false);
  const browseLoadingRef = useRef(false);

  // 검색 결과 상태
  const [searchItems, setSearchItems] = useState<EventCardData[]>([]);
  const [searchPage, setSearchPage] = useState(0);
  const [searchHasMore, setSearchHasMore] = useState(false);
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchFetchingMore, setSearchFetchingMore] = useState(false);
  const searchLoadingRef = useRef(false);

  const browseSentinelRef = useRef<HTMLDivElement>(null);
  const searchSentinelRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const fetchBrowse = useCallback(async (pageNum: number, replace: boolean) => {
    if (!replace && browseLoadingRef.current) return;
    if (!replace) browseLoadingRef.current = true;
    pageNum === 0 ? setBrowseLoading(true) : setBrowseFetchingMore(true);
    try {
      const data = await eventApi.searchEvents("", pageNum);
      const cards = await attachHeroImages(data.content);
      setBrowseItems(prev => replace ? cards : [...prev, ...cards]);
      setBrowseHasMore((data.page.number + 1) < data.page.totalPages);
      setBrowsePage(pageNum);
    } catch {
      // ignore
    } finally {
      browseLoadingRef.current = false;
      setBrowseLoading(false);
      setBrowseFetchingMore(false);
    }
  }, []);

  const fetchSearch = useCallback(async (q: string, pageNum: number, replace: boolean) => {
    if (!replace && searchLoadingRef.current) return;
    if (!replace) searchLoadingRef.current = true;
    pageNum === 0 ? setSearchLoading(true) : setSearchFetchingMore(true);
    try {
      const data = await eventApi.searchEvents(q, pageNum);
      const cards = await attachHeroImages(data.content);
      setSearchItems(prev => replace ? cards : [...prev, ...cards]);
      setSearchHasMore((data.page.number + 1) < data.page.totalPages);
      setSearchPage(pageNum);
    } catch {
      // ignore
    } finally {
      searchLoadingRef.current = false;
      setSearchLoading(false);
      setSearchFetchingMore(false);
    }
  }, []);

  // 초기 목록 로드
  useEffect(() => {
    fetchBrowse(0, true);
  }, [fetchBrowse]);

  const handleSearch = () => {
    const trimmed = query.trim();
    if (!trimmed) {
      setIsSearchMode(false);
      return;
    }
    setSubmittedQuery(trimmed);
    setIsSearchMode(true);
    setSearchPage(0);
    setSearchHasMore(false);
    fetchSearch(trimmed, 0, true);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") handleSearch();
  };

  // 기본 목록 무한스크롤
  useEffect(() => {
    if (isSearchMode || !browseSentinelRef.current || !browseHasMore || browseFetchingMore) return;
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) fetchBrowse(browsePage + 1, false);
      },
      { threshold: 0.1 },
    );
    observer.observe(browseSentinelRef.current);
    return () => observer.disconnect();
  }, [isSearchMode, browseHasMore, browseFetchingMore, browsePage, fetchBrowse]);

  // 검색 결과 무한스크롤
  useEffect(() => {
    if (!isSearchMode || !searchSentinelRef.current || !searchHasMore || searchFetchingMore) return;
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) fetchSearch(submittedQuery, searchPage + 1, false);
      },
      { threshold: 0.1 },
    );
    observer.observe(searchSentinelRef.current);
    return () => observer.disconnect();
  }, [isSearchMode, searchHasMore, searchFetchingMore, submittedQuery, searchPage, fetchSearch]);

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
            disabled={searchLoading}
            className="px-5 py-3.5 rounded-xl bg-primary text-white text-sm font-semibold hover:bg-primary/90 disabled:opacity-50 transition-colors whitespace-nowrap flex items-center gap-1.5"
          >
            {searchLoading ? (
              <span className="inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <IoSearchOutline className="text-base" />
            )}
            검색
          </button>
        </div>

        {/* 콘텐츠 영역 */}
        <AnimatePresence mode="wait">
          {isSearchMode ? (
            <motion.div
              key="search"
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0 }}
            >
              {searchLoading ? (
                <div className="flex justify-center py-16">
                  <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" />
                </div>
              ) : searchItems.length === 0 ? (
                <div className="text-center py-16 space-y-2">
                  <div className="text-4xl">🔍</div>
                  <p className="text-sm font-medium text-gray-600">
                    <span className="text-primary">"{submittedQuery}"</span>와 일치하는 공개 일정이 없습니다
                  </p>
                  <p className="text-xs text-gray-400">다른 키워드로 검색해보세요</p>
                </div>
              ) : (
                <>
                  <div className="flex items-center justify-between mb-3">
                    <p className="text-xs text-gray-500">
                      <span className="font-medium text-primary">"{submittedQuery}"</span> 검색 결과
                    </p>
                    <div className="flex items-center gap-1 text-xs text-gray-400">
                      {searchItems[0].type === "WEDDING"
                        ? <IoHeartOutline className="text-sm" />
                        : <IoPeopleOutline className="text-sm" />}
                      {searchItems.length}건
                    </div>
                  </div>
                  <div className="space-y-3">
                    {searchItems.map((event) => (
                      <EventCard
                        key={event.id}
                        event={event}
                        query={submittedQuery}
                        onClick={() => navigate(`/event/${event.slug}`)}
                      />
                    ))}
                  </div>
                  <div ref={searchSentinelRef} className="h-4" />
                  {searchFetchingMore && (
                    <div className="flex justify-center py-4">
                      <div className="w-6 h-6 border-2 border-primary border-t-transparent rounded-full animate-spin" />
                    </div>
                  )}
                  {!searchHasMore && (
                    <p className="text-center text-xs text-gray-300 py-4">모든 결과를 불러왔습니다</p>
                  )}
                </>
              )}
            </motion.div>
          ) : (
            <motion.div
              key="browse"
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0 }}
            >
              {browseLoading ? (
                <div className="flex justify-center py-16">
                  <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" />
                </div>
              ) : (
                <>
                  {browseItems.length > 0 && (
                    <p className="text-xs text-gray-400 mb-3">공개 일정</p>
                  )}
                  <div className="space-y-3">
                    {browseItems.map((event) => (
                      <EventCard
                        key={event.id}
                        event={event}
                        query=""
                        onClick={() => navigate(`/event/${event.slug}`)}
                      />
                    ))}
                  </div>
                  <div ref={browseSentinelRef} className="h-4" />
                  {browseFetchingMore && (
                    <div className="flex justify-center py-4">
                      <div className="w-6 h-6 border-2 border-primary border-t-transparent rounded-full animate-spin" />
                    </div>
                  )}
                  {!browseHasMore && browseItems.length > 0 && (
                    <p className="text-center text-xs text-gray-300 py-4">모든 일정을 불러왔습니다</p>
                  )}
                </>
              )}
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
};

export default SearchPage;
