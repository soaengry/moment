import { type FC } from "react";
import type { WeddingResponse } from "../types";

interface Props {
  wedding: WeddingResponse;
}

const DAYS = ["일", "월", "화", "수", "목", "금", "토"];

const formatWeddingDate = (dateStr: string) => {
  const date = new Date(dateStr);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const dayOfWeek = DAYS[date.getDay()];
  const hours = date.getHours();
  const minutes = String(date.getMinutes()).padStart(2, "0");
  const period = hours < 12 ? "오전" : "오후";
  const displayHour = hours > 12 ? hours - 12 : hours === 0 ? 12 : hours;

  return {
    date: `${year}. ${month}. ${day} (${dayOfWeek})`,
    time: `${period} ${displayHour}:${minutes}`,
  };
};

const DateVenueSection: FC<Props> = ({ wedding }) => {
  const { date, time } = formatWeddingDate(wedding.weddingDate);

  return (
    <section className="bg-white rounded-2xl shadow-lg p-6 border border-green-100 text-center">
      <h3 className="text-sm text-gray-400 tracking-widest mb-6">
        WEDDING DAY
      </h3>

      <p className="text-xl font-semibold text-gray-800 mb-1">{date}</p>
      <p className="text-lg text-primary font-medium mb-6">{time}</p>

      <div className="w-12 h-px bg-gray-200 mx-auto mb-6" />

      <p className="text-lg font-semibold text-gray-800 mb-1">
        {wedding.venueName}
      </p>
      <p className="text-sm text-gray-500 mb-1">{wedding.venueAddress}</p>
      {wedding.venueDetail && (
        <p className="text-sm text-gray-400">{wedding.venueDetail}</p>
      )}
      {wedding.venuePhone && (
        <a
          href={`tel:${wedding.venuePhone}`}
          className="inline-block mt-2 text-sm text-primary hover:underline"
        >
          {wedding.venuePhone}
        </a>
      )}
    </section>
  );
};

export default DateVenueSection;
