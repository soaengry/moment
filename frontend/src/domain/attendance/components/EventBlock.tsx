import { type FC } from "react";
import type { AttendanceResponse } from "../types";
import { PASTEL_COLORS } from "../attendance.constants";

interface EventBlockProps {
  attendance: AttendanceResponse;
  colorIndex: number;
  onClick: (attendance: AttendanceResponse) => void;
}

const EventBlock: FC<EventBlockProps> = ({
  attendance,
  colorIndex,
  onClick,
}) => {
  const colorClass = PASTEL_COLORS[colorIndex % PASTEL_COLORS.length];

  return (
    <button
      onClick={() => onClick(attendance)}
      className={`w-full px-1.5 py-0.5 rounded text-xs font-medium truncate text-left ${colorClass} hover:opacity-80 transition-opacity`}
    >
      {attendance.title}
    </button>
  );
};

export default EventBlock;
