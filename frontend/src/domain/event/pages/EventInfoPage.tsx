import { type FC, useMemo } from "react";
import { useParams, useLocation } from "react-router-dom";
import { ToastContainer } from "react-toastify";
import { useEventDetail } from "../hooks/useEventDetail";
import { isWeddingDetail } from "../types";
import type { GatheringDetailResponse } from "../types";
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
import RsvpSection from "../../rsvp/components/RsvpSection";
import WeddingFeedTab from "../../feed/components/WeddingFeedTab";
import EventHeader from "../components/EventHeader";
import EventBottomNav from "../components/EventBottomNav";
import { tokenStorage, parseJwt } from "../../auth/auth.utils";
import { useAuthStore } from "../../auth/store/useAuthStore";

type WeddingTab = "info" | "feed" | "guestbook" | "rsvp";

const TAB_LABELS: Record<WeddingTab, string> = {
  info: "정보",
  feed: "피드",
  guestbook: "방명록",
  rsvp: "참석여부",
};

const EventInfoPage: FC = () => {
  const { slug } = useParams<{ slug: string }>();
  const location = useLocation();
  const { data, isLoading, error } = useEventDetail(slug);

  const activeTab = useMemo((): WeddingTab => {
    if (location.pathname.endsWith("/feed")) return "feed";
    if (location.pathname.endsWith("/guestbook")) return "guestbook";
    if (location.pathname.endsWith("/rsvp")) return "rsvp";
    return "info";
  }, [location.pathname]);

  const user = useAuthStore((s) => s.user);

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

  const { event, heroImages, transportation, announcements, detail, schedules, accountGroups } = data;

  const weddingDetail = isWeddingDetail(detail) ? detail : null;
  const hosts = weddingDetail
    ? weddingDetail.hosts
    : (detail ? (detail as GatheringDetailResponse).hosts : []);

  const groom = hosts.find((c) => c.role === "GROOM");
  const bride = hosts.find((c) => c.role === "BRIDE");

  const token = tokenStorage.getAccessToken();
  const currentUserId = token ? Number(parseJwt(token)?.sub) || null : null;
  const hostUserIds = hosts
    .map((c) => c.userId)
    .filter((id): id is number => id !== null);

  const isHost = currentUserId !== null &&
    (hostUserIds.includes(currentUserId) || event.userId === currentUserId);
  const isAdmin = user?.role === "ADMIN";
  const showSettings = isHost || isAdmin;

  const eventId = event.id;

  return (
    <div className="min-h-screen bg-[#faf9f6]">
      <div className="max-w-lg mx-auto">
        {/* Header */}
        <EventHeader
          title={
            activeTab === "info"
              ? event.title || "초대장"
              : TAB_LABELS[activeTab]
          }
          slug={slug}
          showSettings={showSettings && activeTab === "info"}
        />

        {/* Tab content */}
        {activeTab === "info" && (
          <>
            <LandingSection
              heroImages={heroImages}
              title={event.title}
              date={event.date}
              groomName={groom?.name}
              brideName={bride?.name}
            />

            <div className="flex items-center justify-center gap-3 py-8">
              <div className="w-16 h-px bg-primary/10" />
              <div className="w-1 h-1 rounded-full bg-primary/20" />
              <div className="w-16 h-px bg-primary/10" />
            </div>

            <CoupleSection couples={hosts} eventType={event.type} />
            <DateVenueSection wedding={event} eventType={event.type} />
            <LocationSection wedding={event} />
            <ScheduleSection schedules={schedules} eventType={event.type} />
            <DressCodeSection
              detail={weddingDetail}
              transportation={transportation}
            />
            <AccountSection accountGroups={accountGroups} eventType={event.type} />

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

        {activeTab === "feed" && <WeddingFeedTab eventId={eventId} />}

        {activeTab === "guestbook" && weddingDetail && (
          <div className="px-4 py-6">
            <GuestbookSection
              weddingId={weddingDetail.weddingId}
              currentUserId={currentUserId}
              hostUserIds={hostUserIds}
            />
          </div>
        )}

        {activeTab === "rsvp" && weddingDetail && (
          <div className="px-4 py-6">
            <RsvpSection weddingId={weddingDetail.weddingId} />
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
      <EventBottomNav
        eventId={eventId}
        slug={slug}
        activeTab={activeTab}
        eventType={event.type}
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

export default EventInfoPage;
