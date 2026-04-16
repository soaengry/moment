import { type FC, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "../../auth/store/useAuthStore";
import { authApi } from "../../auth/api/authApi";
import { attendanceApi } from "../../attendance/api/attendanceApi";
import { useScrollVisibility } from "../../../global/hooks/useScrollVisibility";
import type { AttendanceResponse } from "../../attendance/types";
import Calendar from "../../attendance/components/Calendar";
import { ToastContainer } from "react-toastify";
import { formatMonthDay } from "../../../global/utils/date";
import {
  IoArrowBack,
  IoCreateOutline,
  IoDocumentTextOutline,
  IoBookmarkOutline,
  IoHeartOutline,
  IoChatbubbleOutline,
  IoLogOutOutline,
  IoChevronForward,
  IoCalendarOutline,
  IoTimeOutline,
  IoMailOutline,
} from "react-icons/io5";

const MyPage: FC = () => {
  const navigate = useNavigate();
  const user = useAuthStore((state) => state.user);
  const setUser = useAuthStore((state) => state.setUser);
  const logout = useAuthStore((state) => state.logout);
  const [upcomingSchedules, setUpcomingSchedules] = useState<
    AttendanceResponse[]
  >([]);
  const [verifyMessage, setVerifyMessage] = useState<string | null>(null);
  const [isSendingVerify, setIsSendingVerify] = useState(false);
  const headerVisible = useScrollVisibility();

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const data = await authApi.getMe();
        setUser(data);
      } catch {
        // 인터셉터에서 401 처리
      }
    };
    fetchUser();
  }, [setUser]);

  useEffect(() => {
    const fetchSchedules = async () => {
      try {
        const all = await attendanceApi.getMyAttendances();
        const now = new Date();
        const upcoming = all
          .filter((s) => new Date(s.date) >= now)
          .sort(
            (a, b) => new Date(a.date).getTime() - new Date(b.date).getTime(),
          )
          .slice(0, 3);
        setUpcomingSchedules(upcoming);
      } catch {
        /* silent */
      }
    };
    fetchSchedules();
  }, []);

  const handleSendVerification = async () => {
    if (!user) return;
    setIsSendingVerify(true);
    setVerifyMessage(null);
    try {
      await authApi.resendVerification(user.email);
      setVerifyMessage("인증 메일이 발송되었습니다. 이메일을 확인해주세요.");
    } catch {
      setVerifyMessage("인증 메일 발송에 실패했습니다.");
    } finally {
      setIsSendingVerify(false);
    }
  };

  const handleLogout = async () => {
    try {
      await authApi.logout();
    } catch {
      // 서버 로그아웃 실패해도 클라이언트 로그아웃 진행
    } finally {
      logout();
      navigate("/login");
    }
  };

  if (!user) return null;

  const menuItems = [
    {
      icon: IoTimeOutline,
      label: "이전 일정",
      path: "/my-page/past-schedules",
    },
    {
      icon: IoDocumentTextOutline,
      label: "내가 작성한 게시글",
      path: "/my-page/posts",
    },
    {
      icon: IoBookmarkOutline,
      label: "북마크",
      path: "/my-page/bookmarks",
    },
    {
      icon: IoHeartOutline,
      label: "좋아요한 게시글",
      path: "/my-page/likes",
    },
    {
      icon: IoChatbubbleOutline,
      label: "내 댓글",
      path: "/my-page/comments",
    },
  ];

  return (
    <div className="min-h-screen bg-[#faf9f6]">
      <div className="max-w-lg mx-auto">
        {/* 헤더 */}
        <header
          className={`sticky top-0 z-20 bg-white/80 backdrop-blur-md border-b border-gray-50 transition-transform duration-300 ${headerVisible ? "translate-y-0" : "-translate-y-full"}`}
        >
          <div className="flex items-center gap-3 px-4 py-3">
            <button onClick={() => navigate(-1)} className="p-1 text-gray-600">
              <IoArrowBack size={22} />
            </button>
            <h1 className="text-base font-semibold text-gray-800">
              마이페이지
            </h1>
          </div>
        </header>

        <div className="px-4 py-4 space-y-4">
          {/* 프로필 카드 */}
          <div className="bg-white rounded-2xl shadow-sm p-5 border border-gray-100 relative">
            <button
              onClick={() => navigate("/edit-profile")}
              className="absolute top-4 right-4 p-1.5 text-gray-400 hover:text-gray-600 transition-colors"
            >
              <IoCreateOutline size={20} />
            </button>
            <div className="flex items-center gap-4">
              <img
                src={user.profileImageUrl ?? "/default-avatar.png"}
                alt="프로필"
                className="w-16 h-16 rounded-full object-cover bg-gray-100"
              />
              <div>
                <p className="text-lg font-semibold text-gray-800">
                  {user.nickname}
                </p>
                <p className="text-sm text-gray-500">{user.email}</p>
              </div>
            </div>

            {/* 이메일 인증 */}
            <div className="flex items-center justify-between mt-4 pt-3 border-t border-gray-50">
              <div className="flex items-center gap-2 text-sm text-gray-500">
                <IoMailOutline size={16} />
                <span>이메일 인증</span>
              </div>
              {user.isEmailVerified ? (
                <span className="text-xs text-green-600 font-medium">
                  인증됨
                </span>
              ) : (
                <button
                  onClick={handleSendVerification}
                  disabled={isSendingVerify}
                  className="text-xs px-3 py-1 rounded-md font-medium text-white disabled:opacity-50 bg-primary hover:bg-primaryHover transition-colors"
                >
                  {isSendingVerify ? "발송 중..." : "인증 메일 발송"}
                </button>
              )}
            </div>
            {verifyMessage && (
              <div className="mt-2 p-2 rounded-lg text-xs bg-green-50 text-green-700">
                {verifyMessage}
              </div>
            )}
          </div>

          {/* 다가오는 일정 */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
            <div className="flex items-center gap-2 px-5 py-3 border-b border-gray-50">
              <IoCalendarOutline size={18} className="text-primary" />
              <span className="text-sm font-semibold text-gray-700">
                다가오는 일정
              </span>
            </div>
            {upcomingSchedules.length > 0 ? (
              upcomingSchedules.map((schedule) => (
                <button
                  key={schedule.id}
                  onClick={() => navigate(`/event/${schedule.slug}`)}
                  className="w-full flex items-center justify-between px-5 py-3.5 hover:bg-gray-50 transition-colors border-b border-gray-50 last:border-b-0"
                >
                  <div className="text-left">
                    <p className="text-sm font-medium text-gray-700">
                      {schedule.title}
                    </p>
                    <p className="text-[11px] text-gray-400 mt-0.5">
                      {formatMonthDay(schedule.date)} · {schedule.locationName}
                    </p>
                  </div>
                  <IoChevronForward size={16} className="text-gray-300" />
                </button>
              ))
            ) : (
              <div className="px-5 py-6 text-center">
                <p className="text-sm text-gray-300">
                  다가오는 일정이 없습니다
                </p>
              </div>
            )}
          </div>

          {/* 캘린더 */}
          <div className="bg-white rounded-2xl shadow-sm p-4 border border-gray-100">
            <Calendar />
          </div>

          {/* 메뉴 리스트 */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
            {menuItems.map((item, index) => (
              <button
                key={item.path}
                onClick={() => navigate(item.path)}
                className={`w-full flex items-center justify-between px-5 py-4 hover:bg-gray-50 transition-colors ${
                  index !== menuItems.length - 1
                    ? "border-b border-gray-50"
                    : ""
                }`}
              >
                <div className="flex items-center gap-3">
                  <item.icon size={20} className="text-gray-500" />
                  <span className="text-sm text-gray-700">{item.label}</span>
                </div>
                <IoChevronForward size={16} className="text-gray-300" />
              </button>
            ))}

            {/* 로그아웃 */}
            <button
              onClick={handleLogout}
              className="w-full flex items-center gap-3 px-5 py-4 hover:bg-gray-50 transition-colors border-t border-gray-100"
            >
              <IoLogOutOutline size={20} className="text-gray-400" />
              <span className="text-sm text-gray-500">로그아웃</span>
            </button>
          </div>
        </div>

        {/* Bottom space for nav */}
        <div className="h-20" />
      </div>

      <ToastContainer
        position="bottom-center"
        autoClose={3000}
        hideProgressBar
        toastClassName="text-sm"
      />
    </div>
  );
};

export default MyPage;
