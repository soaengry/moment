import { type FC } from "react";
import { useNavigate } from "react-router-dom";
import {
  IoInformationCircle,
  IoInformationCircleOutline,
  IoNewspaper,
  IoNewspaperOutline,
  IoBook,
  IoBookOutline,
  IoChatbubbles,
  IoChatbubblesOutline,
  IoPerson,
  IoPersonOutline,
} from "react-icons/io5";
import { useScrollVisibility } from "../../../global/hooks/useScrollVisibility";

export type WeddingTab = "info" | "feed" | "guestbook" | "chat" | "mypage";

interface Props {
  weddingId: number;
  invitationId?: string;
  activeTab: WeddingTab;
}

const WeddingBottomNav: FC<Props> = ({ weddingId, invitationId, activeTab }) => {
  const navigate = useNavigate();
  const isVisible = useScrollVisibility();

  const basePath = invitationId ? `/wedding/${invitationId}` : `/wedding/${weddingId}`;

  const items = [
    {
      key: "info" as const,
      label: "정보",
      icon: IoInformationCircleOutline,
      activeIcon: IoInformationCircle,
      action: () => navigate(basePath),
    },
    {
      key: "feed" as const,
      label: "피드",
      icon: IoNewspaperOutline,
      activeIcon: IoNewspaper,
      action: () => navigate(`${basePath}/feed`),
    },
    {
      key: "guestbook" as const,
      label: "방명록",
      icon: IoBookOutline,
      activeIcon: IoBook,
      action: () => navigate(`${basePath}/guestbook`),
    },
    {
      key: "chat" as const,
      label: "채팅",
      icon: IoChatbubblesOutline,
      activeIcon: IoChatbubbles,
      action: () => navigate(`/wedding/${weddingId}/chat`),
    },
    {
      key: "mypage" as const,
      label: "마이페이지",
      icon: IoPersonOutline,
      activeIcon: IoPerson,
      action: () => navigate("/my-page"),
    },
  ];

  return (
    <nav
      className={`fixed bottom-0 left-0 w-full bg-white border-t border-gray-200 z-50 transition-transform duration-300 ${
        isVisible ? "translate-y-0" : "translate-y-full"
      }`}
    >
      <div className="max-w-lg mx-auto flex items-center justify-around py-2">
        {items.map((item) => {
          const isActive = item.key === activeTab;
          const Icon = isActive ? item.activeIcon : item.icon;

          return (
            <button
              key={item.key}
              onClick={item.action}
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
      <div className="pb-[env(safe-area-inset-bottom)]" />
    </nav>
  );
};

export default WeddingBottomNav;
