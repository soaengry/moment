import { type FC } from "react";
import { motion } from "framer-motion";
import { buttonTap } from "../../../global/constants/animations";
import { useNavigate } from "react-router-dom";
import {
  IoHeartCircleOutline,
  IoHeartCircle,
  IoPizza,
  IoPizzaOutline,
  IoBook,
  IoBookOutline,
  IoChatbubbles,
  IoChatbubblesOutline,
  IoCheckmarkCircleOutline,
  IoCheckmarkCircle,
} from "react-icons/io5";
import { useScrollVisibility } from "../../../global/hooks/useScrollVisibility";
import type { EventType } from "../types";

export type WeddingTab = "info" | "feed" | "guestbook" | "chat" | "mypage" | "rsvp";

interface Props {
  eventId: number;
  slug?: string;
  activeTab: WeddingTab;
  eventType: EventType;
}

const EventBottomNav: FC<Props> = ({
  eventId,
  slug,
  activeTab,
  eventType,
}) => {
  const navigate = useNavigate();
  const isVisible = useScrollVisibility();

  const basePath = slug
    ? `/event/${slug}`
    : `/event/${eventId}`;

  const allItems = [
    {
      key: "info" as const,
      label: "정보",
      icon: IoHeartCircleOutline,
      activeIcon: IoHeartCircle,
      action: () => navigate(basePath),
    },
    {
      key: "feed" as const,
      label: "피드",
      icon: IoPizzaOutline,
      activeIcon: IoPizza,
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
      action: () => navigate(`${basePath}/chat`),
    },
    {
      key: "rsvp" as const,
      label: "참석여부",
      icon: IoCheckmarkCircleOutline,
      activeIcon: IoCheckmarkCircle,
      action: () => navigate(`${basePath}/rsvp`),
    },
  ];

  const items = eventType === "GATHERING"
    ? allItems.filter((item) => item.key !== "guestbook" && item.key !== "rsvp")
    : allItems;

  return (
    <motion.nav
      initial={{ y: 0 }}
      animate={{ y: isVisible ? 0 : "100%" }}
      transition={{ type: "spring", stiffness: 300, damping: 30 }}
      className="fixed bottom-0 left-0 w-full bg-white border-t border-gray-200 z-50"
    >
      <div className="max-w-lg mx-auto flex items-center justify-around py-2">
        {items.map((item) => {
          const isActive = item.key === activeTab;
          const Icon = isActive ? item.activeIcon : item.icon;

          return (
            <motion.button
              key={item.key}
              onClick={item.action}
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
      <div className="pb-[env(safe-area-inset-bottom)]" />
    </motion.nav>
  );
};

export default EventBottomNav;
