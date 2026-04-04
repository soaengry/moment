import { type FC, useRef } from "react";
import { motion, useInView } from "framer-motion";
import { slideUp } from "../../../global/constants/animations";
import type { WeddingResponse } from "../types";

interface Props {
  wedding: WeddingResponse;
}

const DAYS = ["일", "월", "화", "수", "목", "금", "토"];

const DateVenueSection: FC<Props> = ({ wedding }) => {
  const ref = useRef(null);
  const isInView = useInView(ref, { once: true, amount: 0.3 });

  const date = new Date(wedding.eventDate);
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const dayOfWeek = DAYS[date.getDay()];
  const hours = date.getHours();
  const minutes = String(date.getMinutes()).padStart(2, "0");
  const period = hours < 12 ? "오전" : "오후";
  const displayHour = hours > 12 ? hours - 12 : hours === 0 ? 12 : hours;

  // 달력 생성
  const firstDay = new Date(year, date.getMonth(), 1).getDay();
  const daysInMonth = new Date(year, date.getMonth() + 1, 0).getDate();
  const calendarDays: (number | null)[] = [];
  for (let i = 0; i < firstDay; i++) calendarDays.push(null);
  for (let i = 1; i <= daysInMonth; i++) calendarDays.push(i);

  return (
    <motion.section
      ref={ref}
      variants={slideUp}
      initial="hidden"
      animate={isInView ? "visible" : "hidden"}
      className="py-10 px-6 text-center"
    >
      <p className="text-[10px] tracking-[0.4em] text-primary/40 mb-6 uppercase font-medium">
        Wedding Day
      </p>

      <p className="text-lg text-gray-700 font-medium">
        {year}년 {month}월 {day}일 {dayOfWeek}요일
      </p>
      <p className="text-primary font-semibold text-base mt-1">
        {period} {displayHour}시 {minutes !== "00" ? `${minutes}분` : ""}
      </p>

      {/* 미니 캘린더 */}
      <div className="mt-6 mx-auto max-w-[280px]">
        <div className="grid grid-cols-7 gap-0">
          {["일", "월", "화", "수", "목", "금", "토"].map((d, i) => (
            <div
              key={d}
              className={`text-[10px] font-medium py-2 ${i === 0 ? "text-rose" : i === 6 ? "text-blue-400" : "text-gray-400"}`}
            >
              {d}
            </div>
          ))}
          {calendarDays.map((d, i) => {
            const isWeddingDay = d === day;
            const dayIndex = i % 7;
            return (
              <div
                key={i}
                className={`text-xs py-1.5 ${
                  isWeddingDay
                    ? "bg-primary text-white rounded-full w-7 h-7 flex items-center justify-center mx-auto font-bold"
                    : d
                      ? `${dayIndex === 0 ? "text-rose" : dayIndex === 6 ? "text-blue-400" : "text-gray-600"}`
                      : ""
                }`}
              >
                {d ?? ""}
              </div>
            );
          })}
        </div>
      </div>

      {/* 구분선 */}
      <div className="flex items-center justify-center gap-3 my-8">
        <div className="w-12 h-px bg-gray-200" />
        <div className="w-1.5 h-1.5 rounded-full bg-primary/30" />
        <div className="w-12 h-px bg-gray-200" />
      </div>

      {/* 예식장 정보 */}
      <p className="text-base font-semibold text-gray-800">
        {wedding.venueName}
      </p>
      <p className="text-sm text-gray-500 mt-1">{wedding.venueAddress}</p>
      {wedding.venueDetail && (
        <p className="text-xs text-gray-400 mt-0.5">{wedding.venueDetail}</p>
      )}
      {wedding.venuePhone && (
        <a
          href={`tel:${wedding.venuePhone}`}
          className="inline-block mt-2 text-xs text-primary hover:underline"
        >
          {wedding.venuePhone}
        </a>
      )}
    </motion.section>
  );
};

export default DateVenueSection;
