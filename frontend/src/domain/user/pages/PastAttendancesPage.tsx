import { type FC, useState, useEffect, useRef, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { IoArrowBack, IoChevronForward } from "react-icons/io5";
import { attendanceApi } from "../../attendance/api/AttendanceApi";
import type { AttendanceResponse } from "../../attendance/types";
import { useScrollVisibility } from "../../../global/hooks/useScrollVisibility";
import { formatMonthDay } from "../../../global/utils/date";

const PAGE_SIZE = 10;

const PastAttendancesPage: FC = () => {
  const navigate = useNavigate();
  const headerVisible = useScrollVisibility();
  const [allSchedules, setAllSchedules] = useState<AttendanceResponse[]>([]);
  const [displayCount, setDisplayCount] = useState(PAGE_SIZE);
  const [isLoading, setIsLoading] = useState(true);
  const sentinelRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const fetchSchedules = async () => {
      try {
        const all = await attendanceApi.getMyAttendances();
        const now = new Date();
        const past = all
          .filter((s) => new Date(s.date) < now)
          .sort(
            (a, b) => new Date(b.date).getTime() - new Date(a.date).getTime(),
          );
        setAllSchedules(past);
      } catch {
        /* silent */
      } finally {
        setIsLoading(false);
      }
    };
    fetchSchedules();
  }, []);

  const loadMore = useCallback(() => {
    setDisplayCount((prev) => Math.min(prev + PAGE_SIZE, allSchedules.length));
  }, [allSchedules.length]);

  // IntersectionObserver로 무한 스크롤
  useEffect(() => {
    const sentinel = sentinelRef.current;
    if (!sentinel) return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && displayCount < allSchedules.length) {
          loadMore();
        }
      },
      { threshold: 0.1 },
    );

    observer.observe(sentinel);
    return () => observer.disconnect();
  }, [displayCount, allSchedules.length, loadMore]);

  const displayedSchedules = allSchedules.slice(0, displayCount);

  return (
    <div className="max-w-lg mx-auto min-h-screen bg-[#faf9f6]">
      <header
        className={`sticky top-0 z-20 bg-white/80 backdrop-blur-md border-b border-gray-50 transition-transform duration-300 ${headerVisible ? "translate-y-0" : "-translate-y-full"}`}
      >
        <div className="flex items-center gap-3 px-4 py-3">
          <button onClick={() => navigate(-1)} className="text-gray-600">
            <IoArrowBack size={22} />
          </button>
          <h1 className="text-base font-semibold text-gray-800">이전 일정</h1>
        </div>
      </header>

      <div className="px-4 py-4 space-y-3">
        {displayedSchedules.map((schedule) => (
          <button
            key={schedule.id}
            onClick={() => navigate(`/event/${schedule.slug}`)}
            className="w-full bg-white rounded-2xl shadow-sm border border-gray-100 flex items-center justify-between px-5 py-4 hover:bg-gray-50 transition-colors"
          >
            <div className="text-left">
              <p className="text-sm font-medium text-gray-700">
                {schedule.title}
              </p>
              <p className="text-[11px] text-gray-400 mt-0.5">
                {formatMonthDay(schedule.date)} · {schedule.locationName}
              </p>
            </div>
            <IoChevronForward
              size={16}
              className="text-gray-300 flex-shrink-0"
            />
          </button>
        ))}
      </div>

      {/* Sentinel for infinite scroll */}
      {displayCount < allSchedules.length && (
        <div ref={sentinelRef} className="h-10" />
      )}

      {allSchedules.length === 0 && !isLoading && (
        <div className="text-center py-20">
          <p className="text-sm text-gray-300">이전 일정이 없습니다</p>
        </div>
      )}

      <div className="h-20" />
    </div>
  );
};

export default PastAttendancesPage;
