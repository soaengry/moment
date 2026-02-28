import { type FC } from "react";
import type { AttendanceResponse } from "../types";
import { PASTEL_COLORS } from "../schedule.constants";

interface WeddingBlockProps {
  attendance: AttendanceResponse;
  colorIndex: number;
  onClick: (attendance: AttendanceResponse) => void;
}

const WeddingBlock: FC<WeddingBlockProps> = ({
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

export default WeddingBlock;
