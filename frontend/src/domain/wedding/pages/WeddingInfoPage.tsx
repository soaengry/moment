import { type FC, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { ToastContainer } from "react-toastify";
import { weddingApi } from "../api/weddingApi";
import type { WeddingInfoResponse } from "../types";
import {
  CoupleSection,
  DateVenueSection,
  LocationSection,
  ScheduleSection,
  DressCodeSection,
  AccountSection,
  AnnouncementSection,
} from "../components";

const WeddingInfoPage: FC = () => {
  const { weddingId } = useParams<{ weddingId: string }>();
  const [data, setData] = useState<WeddingInfoResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchWeddingInfo = async () => {
      if (!weddingId) {
        setError("잘못된 접근입니다");
        setIsLoading(false);
        return;
      }

      try {
        const info = await weddingApi.getWeddingInfo(Number(weddingId));
        setData(info);
      } catch {
        setError("초대장을 찾을 수 없습니다");
      } finally {
        setIsLoading(false);
      }
    };

    fetchWeddingInfo();
  }, [weddingId]);

  if (isLoading) {
    return (
      <div className="min-h-screen bg-bgPrimary flex items-center justify-center">
        <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="min-h-screen bg-bgPrimary flex items-center justify-center">
        <div className="text-center">
          <p className="text-gray-500 text-lg mb-2">
            {error ?? "초대장을 찾을 수 없습니다"}
          </p>
          <p className="text-gray-400 text-sm">
            주소를 다시 확인해주세요
          </p>
        </div>
      </div>
    );
  }

  const { wedding, couples, schedules, accountGroups, transportation, announcements } = data;

  return (
    <div className="min-h-screen bg-bgPrimary">
      <div className="max-w-lg mx-auto px-4 py-8 space-y-6">
        {/* 타이틀 */}
        <div className="text-center pt-4 pb-2">
          <h1 className="text-2xl font-bold text-gray-800">
            {wedding.title}
          </h1>
        </div>

        {/* 신랑신부 소개 */}
        <CoupleSection couples={couples} />

        {/* 예식 일시 및 장소 */}
        <DateVenueSection wedding={wedding} />

        {/* 오시는 길 */}
        <LocationSection wedding={wedding} />

        {/* 식순 */}
        <ScheduleSection schedules={schedules} />

        {/* 드레스 코드 및 유의사항 */}
        <DressCodeSection wedding={wedding} />

        {/* 교통 안내 */}
        {transportation.length > 0 && (
          <section className="bg-white rounded-2xl shadow-lg p-6 border border-green-100">
            <h3 className="text-center text-sm text-gray-400 tracking-widest mb-6">
              TRANSPORTATION
            </h3>
            <div className="space-y-3">
              {[...transportation]
                .sort((a, b) => a.orderIndex - b.orderIndex)
                .map((t) => (
                  <div key={t.id} className="p-4 rounded-xl bg-bgPrimary">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="text-xs font-semibold text-primary px-2 py-0.5 rounded bg-primary/10">
                        {t.type}
                      </span>
                      <span className="text-sm font-semibold text-gray-700">
                        {t.title}
                      </span>
                    </div>
                    {t.description && (
                      <p className="text-sm text-gray-500 whitespace-pre-line">
                        {t.description}
                      </p>
                    )}
                  </div>
                ))}
            </div>
          </section>
        )}

        {/* 계좌번호 */}
        <AccountSection accountGroups={accountGroups} />

        {/* 하단 여백 (FAB 버튼 공간) */}
        {announcements.length > 0 && <div className="h-16" />}
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
