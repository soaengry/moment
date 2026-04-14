import { type FC, useMemo } from "react";
import { DAYS_OF_WEEK } from "../attendance.constants";
import type { AttendanceResponse } from "../types";
import EventBlock from "./EventBlock";

interface CalendarGridProps {
  year: number;
  month: number;
  attendances: AttendanceResponse[];
  onBlockClick: (attendance: AttendanceResponse) => void;
}

const CalendarGrid: FC<CalendarGridProps> = ({
  year,
  month,
  attendances,
  onBlockClick,
}) => {
  const { cells, attendancesByDate } = useMemo(() => {
    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const totalCells = Math.ceil((firstDay + daysInMonth) / 7) * 7;

    const cells: (number | null)[] = [];
    for (let i = 0; i < totalCells; i++) {
      const day = i - firstDay + 1;
      cells.push(day >= 1 && day <= daysInMonth ? day : null);
    }

    const byDate = new Map<number, AttendanceResponse[]>();
    for (const a of attendances) {
      const date = new Date(a.date);
      if (date.getFullYear() === year && date.getMonth() === month) {
        const day = date.getDate();
        if (!byDate.has(day)) byDate.set(day, []);
        byDate.get(day)!.push(a);
      }
    }

    return { cells, attendancesByDate: byDate };
  }, [year, month, attendances]);

  const today = new Date();
  const isToday = (day: number) =>
    today.getFullYear() === year &&
    today.getMonth() === month &&
    today.getDate() === day;

  return (
    <div>
      <div className="grid grid-cols-7 mb-1">
        {DAYS_OF_WEEK.map((day, i) => (
          <div
            key={day}
            className={`text-center text-xs font-medium py-2 ${
              i === 0
                ? "text-red-400"
                : i === 6
                  ? "text-blue-400"
                  : "text-gray-500"
            }`}
          >
            {day}
          </div>
        ))}
      </div>
      <div className="grid grid-cols-7 border-t border-l border-gray-200">
        {cells.map((day, i) => {
          const dayAttendances = day ? (attendancesByDate.get(day) ?? []) : [];
          return (
            <div
              key={i}
              className={`min-h-[80px] border-r border-b border-gray-200 p-1 ${
                day === null ? "bg-gray-50" : "bg-white"
              }`}
            >
              {day !== null && (
                <>
                  <span
                    className={`text-xs font-medium inline-flex items-center justify-center w-6 h-6 rounded-full ${
                      isToday(day)
                        ? "bg-primary text-white"
                        : i % 7 === 0
                          ? "text-red-400"
                          : i % 7 === 6
                            ? "text-blue-400"
                            : "text-gray-700"
                    }`}
                  >
                    {day}
                  </span>
                  <div className="mt-0.5 space-y-0.5">
                    {dayAttendances.map((a, idx) => (
                      <EventBlock
                        key={a.id}
                        attendance={a}
                        colorIndex={idx}
                        onClick={onBlockClick}
                      />
                    ))}
                  </div>
                </>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default CalendarGrid;
