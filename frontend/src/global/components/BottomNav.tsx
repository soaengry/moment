import { type FC } from "react";
import { motion } from "framer-motion";
import { buttonTap } from "../constants/animations";
import { useNavigate, useLocation } from "react-router-dom";
import { IoHomeOutline, IoHome } from "react-icons/io5";
import { IoPersonOutline, IoPerson } from "react-icons/io5";
import { IoAddCircleOutline, IoAddCircle } from "react-icons/io5";
import { IoSearchOutline, IoSearch } from "react-icons/io5";
// import { IoNewspaperOutline, IoNewspaper } from "react-icons/io5";
import { useScrollVisibility } from "../hooks/useScrollVisibility";
import { useAuthStore } from "../../domain/auth/store/useAuthStore";

interface NavItem {
  path: string;
  label: string;
  icon: FC<{ className?: string }>;
  activeIcon: FC<{ className?: string }>;
  requiresAuth?: boolean;
}

const NAV_ITEMS: NavItem[] = [
  {
    path: "/",
    label: "홈",
    icon: IoHomeOutline,
    activeIcon: IoHome,
  },
  {
    path: "/search",
    label: "검색",
    icon: IoSearchOutline,
    activeIcon: IoSearch,
  },
  // {
  //   path: "/feed",
  //   label: "피드",
  //   icon: IoNewspaperOutline,
  //   activeIcon: IoNewspaper,
  // },
  {
    path: "/event/create",
    label: "초대장 만들기",
    icon: IoAddCircleOutline,
    activeIcon: IoAddCircle,
    requiresAuth: true,
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
  const visibleItems = NAV_ITEMS.filter((item) => !item.requiresAuth || !!user);

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

  // event 페이지에서도 숨김
  if (/^\/event\/[^/]+$/.test(location.pathname)) {
    return null;
  }

  // event 하위 페이지 (채팅, 피드, 방명록, 참석여부, 편집 포함) — EventBottomNav 사용
  if (/^\/event\/[^/]+\/(chat|feed|guestbook|rsvp|edit)/.test(location.pathname)) {
    return null;
  }

  // 마이페이지 하위 페이지 (자체 헤더 사용)
  if (/^\/my-page\/.+$/.test(location.pathname)) {
    return null;
  }

  // 개별 페이지 (자체 헤더 사용)
  const selfHeaderPaths = ["/my-schedule", "/edit-profile", "/delete-account"];
  if (selfHeaderPaths.includes(location.pathname)) {
    return null;
  }

  return (
    <motion.nav
      initial={{ y: 0 }}
      animate={{ y: isVisible ? 0 : "100%" }}
      transition={{ type: "spring", stiffness: 300, damping: 30 }}
      className="fixed bottom-0 left-0 w-full bg-white border-t border-gray-200 z-50"
    >
      <div className="max-w-lg mx-auto flex items-center justify-around py-2">
        {visibleItems.map((item) => {
          const isActive = location.pathname === item.path;
          const Icon = isActive ? item.activeIcon : item.icon;

          return (
            <motion.button
              key={item.path}
              onClick={() => navigate(item.path)}
              whileTap={buttonTap}
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
            </motion.button>
          );
        })}
      </div>
      {/* Safe area for iOS */}
      <div className="pb-[env(safe-area-inset-bottom)]" />
    </motion.nav>
  );
};

export default BottomNav;
