import { type FC, useEffect, useState } from "react";
import { useParams, useLocation } from "react-router-dom";
import { ToastContainer } from "react-toastify";
import { weddingApi } from "../api/weddingApi";
import type { WeddingInfoResponse } from "../types";
import {
  LandingSection,
  CoupleSection,
  DateVenueSection,
  LocationSection,
  ScheduleSection,
  DressCodeSection,
  AccountSection,
  AnnouncementSection,
} from "../components";
import GuestbookSection from "../../guestbook/components/GuestbookSection";
import WeddingFeedTab from "../../feed/components/WeddingFeedTab";
import WeddingHeader from "../components/WeddingHeader";
import WeddingBottomNav from "../components/WeddingBottomNav";
import { tokenStorage, parseJwt } from "../../auth/auth.utils";
import { useAuthStore } from "../../auth/store/useAuthStore";

type WeddingTab = "info" | "feed" | "guestbook";

const TAB_LABELS: Record<WeddingTab, string> = {
  info: "정보",
  feed: "피드",
  guestbook: "방명록",
};

const WeddingInfoPage: FC = () => {
  const { invitationId } = useParams<{ invitationId: string }>();
  const location = useLocation();
  const [data, setData] = useState<WeddingInfoResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const getTabFromPath = (): WeddingTab => {
    if (location.pathname.endsWith("/feed")) return "feed";
    if (location.pathname.endsWith("/guestbook")) return "guestbook";
    return "info";
  };
  const activeTab = getTabFromPath();

  const user = useAuthStore((s) => s.user);

  useEffect(() => {
    const fetchWeddingInfo = async () => {
      if (!invitationId) {
        setError("잘못된 접근입니다");
        setIsLoading(false);
        return;
      }
      try {
        const info = await weddingApi.getWeddingInfo(invitationId);
        setData(info);
      } catch {
        setError("초대장을 찾을 수 없습니다");
      } finally {
        setIsLoading(false);
      }
    };
    fetchWeddingInfo();
  }, [invitationId]);

  if (isLoading) {
    return (
      <div className="min-h-screen bg-[#faf9f6] flex items-center justify-center">
        <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="min-h-screen bg-[#faf9f6] flex items-center justify-center">
        <div className="text-center">
          <p className="text-gray-500 text-lg mb-2">
            {error ?? "초대장을 찾을 수 없습니다"}
          </p>
          <p className="text-gray-400 text-sm">주소를 다시 확인해주세요</p>
        </div>
      </div>
    );
  }

  const {
    wedding,
    couples,
    schedules,
    accountGroups,
    gallery,
    transportation,
    announcements,
  } = data;
  const groom = couples.find((c) => c.role === "GROOM");
  const bride = couples.find((c) => c.role === "BRIDE");

  const token = tokenStorage.getAccessToken();
  const currentUserId = token ? Number(parseJwt(token)?.sub) || null : null;
  const hostUserIds = couples
    .map((c) => c.userId)
    .filter((id): id is number => id !== null);

  const isHost = currentUserId !== null && hostUserIds.includes(currentUserId);
  const isAdmin = user?.role === "ADMIN";
  const showSettings = isHost || isAdmin;

  const weddingId = Number(wedding.id);

  return (
    <div className="min-h-screen bg-[#faf9f6]">
      <div className="max-w-lg mx-auto">
        {/* Header */}
        <WeddingHeader
          title={
            activeTab === "info"
              ? wedding.title || "초대장"
              : TAB_LABELS[activeTab]
          }
          weddingId={weddingId}
          invitationId={invitationId}
          showSettings={showSettings && activeTab === "info"}
        />

        {/* Tab content */}
        {activeTab === "info" && (
          <>
            <LandingSection
              gallery={gallery}
              title={wedding.title}
              weddingDate={wedding.weddingDate}
              groomName={groom?.name}
              brideName={bride?.name}
            />

            <div className="flex items-center justify-center gap-3 py-8">
              <div className="w-16 h-px bg-primary/10" />
              <div className="w-1 h-1 rounded-full bg-primary/20" />
              <div className="w-16 h-px bg-primary/10" />
            </div>

            <CoupleSection couples={couples} />
            <DateVenueSection wedding={wedding} />
            <LocationSection wedding={wedding} />
            <ScheduleSection schedules={schedules} />
            <DressCodeSection
              wedding={wedding}
              transportation={transportation}
            />
            <AccountSection accountGroups={accountGroups} />

            <div className="flex items-center justify-center gap-3 py-4">
              <div className="w-16 h-px bg-primary/10" />
              <div className="w-1 h-1 rounded-full bg-primary/20" />
              <div className="w-16 h-px bg-primary/10" />
            </div>

            <footer className="py-10 text-center">
              <p className="text-[10px] tracking-[0.3em] text-gray-300 uppercase">
                Powered by Moment
              </p>
            </footer>

            {announcements.length > 0 && <div className="h-20" />}
          </>
        )}

        {activeTab === "feed" && <WeddingFeedTab weddingId={weddingId} />}

        {activeTab === "guestbook" && (
          <div className="px-4 py-6">
            <GuestbookSection
              weddingId={weddingId}
              currentUserId={currentUserId}
              hostUserIds={hostUserIds}
            />
          </div>
        )}

        {/* Bottom space for nav */}
        <div className="h-20" />
      </div>

      {/* Announcement FAB — info 탭에서만 */}
      {activeTab === "info" && (
        <AnnouncementSection announcements={announcements} />
      )}

      {/* Bottom Nav */}
      <WeddingBottomNav
        weddingId={weddingId}
        invitationId={invitationId}
        activeTab={activeTab}
      />

      <ToastContainer
        position="bottom-center"
        autoClose={2000}
        hideProgressBar
        closeOnClick
        pauseOnHover={false}
        toastClassName="text-sm"
      />
    </div>
  );
};

export default WeddingInfoPage;
