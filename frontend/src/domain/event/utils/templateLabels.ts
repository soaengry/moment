import type { EventType } from "../types";

export const TEMPLATE_LABELS: Record<
  EventType,
  {
    sectionTitle: string;
    role1: string;
    role2: string | null;
    dateTitle: string;
    scheduleTitle: string;
    scheduleAddLabel: string;
    accountDesc: string;
    venueLabel: string;
    dateLabel: string;
    timeLabel: string;
  }
> = {
  WEDDING: {
    sectionTitle: "Invitation",
    role1: "Groom",
    role2: "Bride",
    dateTitle: "Wedding Day",
    scheduleTitle: "Ceremony",
    scheduleAddLabel: "식순 추가",
    accountDesc: "축하의 마음을 전해주세요",
    venueLabel: "예식장 이름",
    dateLabel: "예식 날짜",
    timeLabel: "예식 시간",
  },
  GATHERING: {
    sectionTitle: "모임",
    role1: "주최자",
    role2: null,
    dateTitle: "모임 일시",
    scheduleTitle: "일정",
    scheduleAddLabel: "일정 추가",
    accountDesc: "계좌 안내",
    venueLabel: "장소 이름",
    dateLabel: "모임 날짜",
    timeLabel: "모임 시간",
  },
};
