import { type FC, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
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
import { tokenStorage, parseJwt } from "../../auth/auth.utils";

const WeddingInfoPage: FC = () => {
  const { invitationId } = useParams<{ invitationId: string }>();
  const [data, setData] = useState<WeddingInfoResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchWeddingInfo = async () => {
      if (!invitationId) {
        setError("잘못된 접근입니다");
        setIsLoading(false);
        return;
      }
      try {
        const info = await weddingApi.getWeddingInfo(invitationId);
        console.log("Wedding Info:", info);
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

  // 현재 로그인한 사용자 ID (JWT sub)
  const token = tokenStorage.getAccessToken();
  const currentUserId = token ? Number(parseJwt(token)?.sub) || null : null;
  // 호스트: 이 웨딩의 커플로 등록된 사용자들의 ID
  const hostUserIds = couples
    .map((c) => c.userId)
    .filter((id): id is number => id !== null);

  return (
    <div className="min-h-screen bg-[#faf9f6]">
      <div className="max-w-lg mx-auto">
        {/* 랜딩 슬라이더 */}
        <LandingSection
          gallery={gallery}
          title={wedding.title}
          weddingDate={wedding.weddingDate}
          groomName={groom?.name}
          brideName={bride?.name}
        />

        {/* 구분선 */}
        <div className="flex items-center justify-center gap-3 py-8">
          <div className="w-16 h-px bg-primary/10" />
          <div className="w-1 h-1 rounded-full bg-primary/20" />
          <div className="w-16 h-px bg-primary/10" />
        </div>

        {/* 신랑신부 소개 */}
        <CoupleSection couples={couples} />

        {/* 예식 일시 및 장소 */}
        <DateVenueSection wedding={wedding} />

        {/* 오시는 길 */}
        <LocationSection wedding={wedding} />

        {/* 식순 */}
        <ScheduleSection schedules={schedules} />

        {/* 정보 (드레스코드, 유의사항, 교통 등) */}
        <DressCodeSection wedding={wedding} transportation={transportation} />

        {/* 계좌번호 */}
        <AccountSection accountGroups={accountGroups} />

        {/* 구분선 */}
        <div className="flex items-center justify-center gap-3 py-4">
          <div className="w-16 h-px bg-primary/10" />
          <div className="w-1 h-1 rounded-full bg-primary/20" />
          <div className="w-16 h-px bg-primary/10" />
        </div>

        {/* 방명록 */}
        <GuestbookSection
          weddingId={Number(wedding.id)}
          currentUserId={currentUserId}
          hostUserIds={hostUserIds}
        />

        {/* 하단 푸터 */}
        <footer className="py-10 text-center">
          <p className="text-[10px] tracking-[0.3em] text-gray-300 uppercase">
            Powered by Moment
          </p>
        </footer>

        {/* 공지사항 FAB 공간 */}
        {announcements.length > 0 && <div className="h-20" />}
      </div>

      {/* 공지사항 FAB + 모달 */}
      <AnnouncementSection announcements={announcements} />

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
