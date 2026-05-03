import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  IoCalendarOutline,
  IoNewspaperOutline,
  IoPersonOutline,
  IoHeartOutline,
  IoChatbubbleOutline,
  IoChevronForward,
  IoSparkles,
  IoChatbubblesOutline,
  IoCalendar,
  IoLocationOutline,
} from "react-icons/io5";
import { useAuthStore } from "../../domain/auth/store/useAuthStore";
import type { UserResponse } from "../../domain/auth/types";
import { attendanceApi } from "../../domain/attendance/api/attendanceApi";
import { feedApi } from "../../domain/feed/api/feedApi";
import { eventApi } from "../../domain/event/api/eventApi";
import type { AttendanceResponse } from "../../domain/attendance/types";
import type { PostResponse } from "../../domain/feed/types";
import type { EventType } from "../../domain/event/types";

// ─── Types ────────────────────────────────────────────────────

interface PublicEventCardData {
  slug: string;
  title: string;
  type: EventType;
  date: string;
  locationName: string;
  heroImageUrl: string | null;
}

// ─── Fetch helpers ────────────────────────────────────────────

async function fetchPublicEventsWithImages(
  size = 10,
): Promise<PublicEventCardData[]> {
  const { content } = await eventApi.searchEvents("", 0, size);
  if (content.length === 0) return [];

  const results = await Promise.allSettled(
    content.map((e) => eventApi.getEventInfo(e.slug)),
  );

  const cards: PublicEventCardData[] = content.map((event, i) => {
    let heroImageUrl: string | null = null;
    const result = results[i];
    if (result.status === "fulfilled") {
      const images = result.value.heroImages;
      const hero =
        images.find((img) => img.orderIndex === 0) ?? images[0] ?? null;
      heroImageUrl = hero?.imageUrl ?? null;
    }
    return {
      slug: event.slug,
      title: event.title,
      type: event.type,
      date: event.date,
      locationName: event.locationName,
      heroImageUrl,
    };
  });

  // Random shuffle
  return [...cards].sort(() => Math.random() - 0.5);
}

// ─── Main ─────────────────────────────────────────────────────

const HomePage = () => {
  const { user, isAuthenticated, isLoading } = useAuthStore();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="w-6 h-6 border-2 border-primary border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (isAuthenticated) {
    return <Dashboard user={user!} />;
  }

  return <Landing />;
};

// ─── 랜딩 (비로그인) ─────────────────────────────────────────

const Landing = () => {
  const features = [
    {
      icon: <IoSparkles className="w-7 h-7 text-primary" />,
      title: "초대장",
      description: "나만의 초대장을 만들고 소중한 분들에게 공유하세요",
    },
    {
      icon: <IoChatbubblesOutline className="w-7 h-7 text-primary" />,
      title: "실시간 소통",
      description: "채팅과 피드로 메시지를 전달하세요",
    },
    {
      icon: <IoCalendar className="w-7 h-7 text-primary" />,
      title: "일정 관리",
      description: "참석 일정을 한눈에 확인하고 관리하세요",
    },
  ];

  return (
    <div className="max-w-lg mx-auto space-y-8">
      {/* 히어로 */}
      <div className="bg-gradient-to-br from-primary to-primaryHover rounded-2xl p-8 text-center text-white">
        <h1 className="text-3xl font-bold mb-2">Moment</h1>
        <p className="text-white/90 mb-6">소중한 순간을 함께 나누세요</p>
      </div>

      {/* 공개 일정 */}
      <PublicEventsSection />

      {/* 기능 소개 */}
      <div className="space-y-3">
        {features.map((f) => (
          <div
            key={f.title}
            className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5 flex items-start gap-4"
          >
            <div className="w-12 h-12 rounded-xl bg-primary/10 flex items-center justify-center shrink-0">
              {f.icon}
            </div>
            <div>
              <h3 className="font-semibold text-gray-800 mb-1">{f.title}</h3>
              <p className="text-sm text-gray-500">{f.description}</p>
            </div>
          </div>
        ))}
      </div>

      {/* 하단 CTA */}
      <div className="text-center pb-4">
        <Link
          to="/signup"
          className="inline-block px-8 py-3 bg-primary text-white font-semibold rounded-xl hover:bg-primaryHover transition-colors"
        >
          지금 시작하기
        </Link>
      </div>
    </div>
  );
};

// ─── 대시보드 (로그인) ────────────────────────────────────────

interface DashboardProps {
  user: UserResponse;
}

const Dashboard = ({ user }: DashboardProps) => {
  const [schedules, setSchedules] = useState<AttendanceResponse[]>([]);
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [schedulesLoading, setSchedulesLoading] = useState(true);
  const [postsLoading, setPostsLoading] = useState(true);

  useEffect(() => {
    attendanceApi
      .getMyAttendances()
      .then((data) => {
        const now = new Date();
        const upcoming = data
          .filter((s) => new Date(s.date) >= now)
          .sort(
            (a, b) => new Date(a.date).getTime() - new Date(b.date).getTime(),
          )
          .slice(0, 3);
        setSchedules(upcoming);
      })
      .catch(() => setSchedules([]))
      .finally(() => setSchedulesLoading(false));

    feedApi
      .getFeed(undefined, 3)
      .then((data) => setPosts(data.content))
      .catch(() => setPosts([]))
      .finally(() => setPostsLoading(false));
  }, []);

  return (
    <div className="max-w-lg mx-auto space-y-5">
      {/* 환영 인사 */}
      <div className="flex items-center gap-3">
        {user?.profileImageUrl ? (
          <img
            src={user.profileImageUrl}
            alt="프로필"
            className="w-12 h-12 rounded-full object-cover border border-gray-100"
          />
        ) : (
          <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center">
            <IoPersonOutline className="w-6 h-6 text-primary" />
          </div>
        )}
        <div>
          <p className="text-lg font-semibold text-gray-800">
            안녕하세요, {user?.nickname ?? "회원"}님
          </p>
          <p className="text-sm text-gray-400">오늘도 좋은 하루 되세요</p>
        </div>
      </div>

      {/* 빠른 메뉴 */}
      <div className="grid grid-cols-3 gap-3">
        {[
          {
            to: "/my-schedule",
            icon: <IoCalendarOutline className="w-6 h-6" />,
            label: "내 일정",
          },
          {
            to: "/feed",
            icon: <IoNewspaperOutline className="w-6 h-6" />,
            label: "피드",
          },
          {
            to: "/my-page",
            icon: <IoPersonOutline className="w-6 h-6" />,
            label: "마이페이지",
          },
        ].map((m) => (
          <Link
            key={m.to}
            to={m.to}
            className="bg-white rounded-2xl shadow-sm border border-gray-100 py-4 flex flex-col items-center gap-2 text-primary hover:bg-primary/5 transition-colors"
          >
            {m.icon}
            <span className="text-xs font-medium text-gray-600">{m.label}</span>
          </Link>
        ))}
      </div>

      {/* 공개 일정 */}
      <PublicEventsSection />

      {/* 다가오는 일정 */}
      <section className="bg-white rounded-2xl shadow-sm border border-gray-100 p-4">
        <h2 className="font-semibold text-gray-800 mb-3">다가오는 일정</h2>
        {schedulesLoading ? (
          <div className="flex justify-center py-6">
            <div className="w-5 h-5 border-2 border-primary border-t-transparent rounded-full animate-spin" />
          </div>
        ) : schedules.length === 0 ? (
          <p className="text-sm text-gray-400 text-center py-6">
            다가오는 일정이 없습니다
          </p>
        ) : (
          <div className="space-y-2">
            {schedules.map((s) => (
              <Link
                key={s.id}
                to={`/event/${s.slug}`}
                className="flex items-center justify-between p-3 rounded-xl hover:bg-gray-50 transition-colors"
              >
                <div className="min-w-0">
                  <p className="font-medium text-gray-800 truncate">
                    {s.title}
                  </p>
                  <p className="text-xs text-gray-400 mt-0.5">
                    {formatScheduleDate(s.date)}
                    {s.locationName && ` · ${s.locationName}`}
                  </p>
                </div>
                <IoChevronForward className="w-4 h-4 text-gray-300 shrink-0 ml-2" />
              </Link>
            ))}
          </div>
        )}
        <Link
          to="/my-schedule"
          className="block text-center text-sm text-primary font-medium mt-3 pt-3 border-t border-gray-50"
        >
          전체 일정 보기
        </Link>
      </section>

      {/* 최근 피드 */}
      <section className="bg-white rounded-2xl shadow-sm border border-gray-100 p-4">
        <h2 className="font-semibold text-gray-800 mb-3">최근 피드</h2>
        {postsLoading ? (
          <div className="flex justify-center py-6">
            <div className="w-5 h-5 border-2 border-primary border-t-transparent rounded-full animate-spin" />
          </div>
        ) : posts.length === 0 ? (
          <p className="text-sm text-gray-400 text-center py-6">
            아직 피드가 없습니다
          </p>
        ) : (
          <div className="space-y-2">
            {posts.map((p) => (
              <Link
                key={p.id}
                to="/feed"
                className="flex items-center justify-between p-3 rounded-xl hover:bg-gray-50 transition-colors"
              >
                <div className="min-w-0 flex-1">
                  <p className="text-sm font-medium text-gray-700">
                    {p.author.nickname}
                  </p>
                  <p className="text-xs text-gray-400 truncate mt-0.5">
                    {p.content}
                  </p>
                </div>
                <div className="flex items-center gap-2.5 text-xs text-gray-400 shrink-0 ml-3">
                  <span className="flex items-center gap-0.5">
                    <IoHeartOutline className="w-3.5 h-3.5" />
                    {p.likeCount}
                  </span>
                  <span className="flex items-center gap-0.5">
                    <IoChatbubbleOutline className="w-3.5 h-3.5" />
                    {p.commentCount}
                  </span>
                </div>
              </Link>
            ))}
          </div>
        )}
        <Link
          to="/feed"
          className="block text-center text-sm text-primary font-medium mt-3 pt-3 border-t border-gray-50"
        >
          피드 더보기
        </Link>
      </section>
    </div>
  );
};

// ─── 공개 일정 섹션 ───────────────────────────────────────────

const PublicEventsSection = () => {
  const [cards, setCards] = useState<PublicEventCardData[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchPublicEventsWithImages(10)
      .then(setCards)
      .catch(() => setCards([]))
      .finally(() => setLoading(false));
  }, []);

  if (!loading && cards.length === 0) return null;

  return (
    <section>
      <div className="flex items-center justify-between mb-3">
        <h2 className="font-semibold text-gray-800">공개 일정 둘러보기</h2>
        <Link to="/search" className="text-xs text-primary font-medium">
          전체 보기
        </Link>
      </div>

      <div
        className="flex gap-3 overflow-x-auto pb-1"
        style={{
          scrollSnapType: "x mandatory",
          scrollbarWidth: "none",
          msOverflowStyle: "none",
        } as React.CSSProperties}
      >
        {loading
          ? Array.from({ length: 4 }).map((_, i) => (
              <SkeletonCard key={i} />
            ))
          : cards.map((card) => (
              <PublicEventCard key={card.slug} card={card} />
            ))}
      </div>
    </section>
  );
};

// ─── 이벤트 카드 ─────────────────────────────────────────────

interface PublicEventCardProps {
  card: PublicEventCardData;
}

const PublicEventCard = ({ card }: PublicEventCardProps) => {
  const isWedding = card.type === "WEDDING";

  return (
    <Link
      to={`/event/${card.slug}`}
      className="relative rounded-2xl overflow-hidden shrink-0 block active:scale-95 transition-transform"
      style={{ width: 160, height: 240, scrollSnapAlign: "start" }}
    >
      {card.heroImageUrl ? (
        <img
          src={card.heroImageUrl}
          alt={card.title}
          className="w-full h-full object-cover"
        />
      ) : (
        <div
          className={`w-full h-full ${
            isWedding
              ? "bg-gradient-to-br from-rose-100 to-pink-200"
              : "bg-gradient-to-br from-indigo-100 to-blue-200"
          }`}
        />
      )}

      {/* 하단 그라디언트 오버레이 */}
      <div className="absolute inset-0 bg-gradient-to-t from-black/75 via-black/15 to-transparent" />

      {/* 타입 배지 */}
      <div className="absolute top-2.5 left-2.5">
        <span
          className={`text-xs font-semibold px-2 py-0.5 rounded-full ${
            isWedding
              ? "bg-rose-500/90 text-white"
              : "bg-indigo-500/90 text-white"
          }`}
        >
          {isWedding ? "웨딩" : "모임"}
        </span>
      </div>

      {/* 이벤트 정보 */}
      <div className="absolute bottom-0 left-0 right-0 p-3">
        <p className="text-white font-semibold text-sm leading-snug line-clamp-2 mb-1">
          {card.title}
        </p>
        <p className="text-white/75 text-xs">{formatScheduleDate(card.date)}</p>
        {card.locationName && (
          <p className="text-white/60 text-xs truncate mt-0.5 flex items-center gap-0.5">
            <IoLocationOutline className="w-3 h-3 shrink-0" />
            {card.locationName}
          </p>
        )}
      </div>
    </Link>
  );
};

// ─── 스켈레톤 카드 ────────────────────────────────────────────

const SkeletonCard = () => (
  <div
    className="rounded-2xl shrink-0 bg-gray-100 animate-pulse"
    style={{ width: 160, height: 240, scrollSnapAlign: "start" }}
  />
);

// ─── 유틸 ─────────────────────────────────────────────────────

const DAYS = ["일", "월", "화", "수", "목", "금", "토"];

function formatScheduleDate(dateStr: string): string {
  const d = new Date(dateStr);
  const month = d.getMonth() + 1;
  const day = d.getDate();
  const dayOfWeek = DAYS[d.getDay()];
  return `${month}월 ${day}일 (${dayOfWeek})`;
}

export default HomePage;
