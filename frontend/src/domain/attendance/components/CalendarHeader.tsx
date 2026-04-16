import { type FC } from "react";
import { IoChevronBack, IoChevronForward, IoAdd } from "react-icons/io5";

interface CalendarHeaderProps {
  year: number;
  month: number;
  onPrevMonth: () => void;
  onNextMonth: () => void;
  onAddClick: () => void;
}

const CalendarHeader: FC<CalendarHeaderProps> = ({
  year,
  month,
  onPrevMonth,
  onNextMonth,
  onAddClick,
}) => {
  return (
    <div className="flex items-center justify-between mb-4">
      <div className="flex items-center gap-2">
        <button
          onClick={onPrevMonth}
          className="p-2 rounded-lg hover:bg-gray-100 transition-colors"
        >
          <IoChevronBack className="w-5 h-5 text-gray-600" />
        </button>
        <h3 className="text-lg font-semibold text-gray-800 min-w-[120px] text-center">
          {year}년 {month + 1}월
        </h3>
        <button
          onClick={onNextMonth}
          className="p-2 rounded-lg hover:bg-gray-100 transition-colors"
        >
          <IoChevronForward className="w-5 h-5 text-gray-600" />
        </button>
      </div>
      <button
        onClick={onAddClick}
        className="flex items-center gap-1 px-3 py-2 rounded-lg text-sm font-medium text-white bg-primary hover:bg-primaryHover transition-colors"
      >
        <IoAdd className="w-4 h-4" />
        일정 등록
      </button>
    </div>
  );
};

export default CalendarHeader;
