import { type FC } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { IoHomeOutline, IoHome } from "react-icons/io5";
import { IoPersonOutline, IoPerson } from "react-icons/io5";
import { IoAddCircleOutline, IoAddCircle } from "react-icons/io5";
import { IoNewspaperOutline, IoNewspaper } from "react-icons/io5";
import { useAuthStore } from "../../domain/auth/store/useAuthStore";
import { useScrollVisibility } from "../hooks/useScrollVisibility";

interface NavItem {
  path: string;
  label: string;
  icon: FC<{ className?: string }>;
  activeIcon: FC<{ className?: string }>;
  adminOnly?: boolean;
}

const NAV_ITEMS: NavItem[] = [
  {
    path: "/",
    label: "홈",
    icon: IoHomeOutline,
    activeIcon: IoHome,
  },
  {
    path: "/feed",
    label: "피드",
    icon: IoNewspaperOutline,
    activeIcon: IoNewspaper,
  },
  {
    path: "/wedding/create",
    label: "초대장 만들기",
    icon: IoAddCircleOutline,
    activeIcon: IoAddCircle,
    adminOnly: true,
  },
  {
    path: "/my-page",
    label: "마이페이지",
    icon: IoPersonOutline,
    activeIcon: IoPerson,
  },
];

const BottomNav: FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const user = useAuthStore((state) => state.user);
  const isVisible = useScrollVisibility();

  const isAdmin = user?.role === "ADMIN";
  const visibleItems = NAV_ITEMS.filter((item) => !item.adminOnly || isAdmin);

  // 특정 페이지에서는 BottomNav를 숨김 (로그인, 회원가입 등)
  const hiddenPaths = [
    "/login",
    "/signup",
    "/verify-email",
    "/restore-account",
    "/oauth2/callback",
  ];
  if (hiddenPaths.some((p) => location.pathname.startsWith(p))) {
    return null;
  }

  // wedding info 페이지에서도 숨김
  if (/^\/wedding\/[^/]+$/.test(location.pathname)) {
    return null;
  }

  // 웨딩 하위 페이지 (채팅, 피드, 방명록, 편집 포함) — WeddingBottomNav 사용
  if (/^\/wedding\/[^/]+\/(chat|feed|guestbook|edit)/.test(location.pathname)) {
    return null;
  }

  // 마이페이지 하위 페이지 (자체 헤더 사용)
  if (/^\/my-page\/.+$/.test(location.pathname)) {
    return null;
  }

  return (
    <nav
      className={`fixed bottom-0 left-0 w-full bg-white border-t border-gray-200 z-50 transition-transform duration-300 ${
        isVisible ? "translate-y-0" : "translate-y-full"
      }`}
    >
      <div className="max-w-lg mx-auto flex items-center justify-around py-2">
        {visibleItems.map((item) => {
          const isActive = location.pathname === item.path;
          const Icon = isActive ? item.activeIcon : item.icon;

          return (
            <button
              key={item.path}
              onClick={() => navigate(item.path)}
              className="flex flex-col items-center gap-0.5 px-3 py-1 min-w-[56px]"
            >
              <Icon
                className={`text-2xl ${isActive ? "text-primary" : "text-gray-400"}`}
              />
              <span
                className={`text-[10px] ${isActive ? "text-primary font-semibold" : "text-gray-400"}`}
              >
                {item.label}
              </span>
            </button>
          );
        })}
      </div>
      {/* Safe area for iOS */}
      <div className="pb-[env(safe-area-inset-bottom)]" />
    </nav>
  );
};

export default BottomNav;
