import { type FC } from "react";
import type { ScheduleResponse } from "../types";

interface Props {
  schedules: ScheduleResponse[];
}

const formatTime = (time: string) => {
  // "HH:mm:ss" → "HH:mm"
  return time.slice(0, 5);
};

const ScheduleSection: FC<Props> = ({ schedules }) => {
  if (schedules.length === 0) return null;

  const sorted = [...schedules].sort((a, b) => a.orderIndex - b.orderIndex);

  return (
    <section className="bg-white rounded-2xl shadow-lg p-6 border border-green-100">
      <h3 className="text-center text-sm text-gray-400 tracking-widest mb-6">
        CEREMONY
      </h3>

      <div className="relative pl-6">
        {/* 타임라인 세로선 */}
        <div className="absolute left-[7px] top-2 bottom-2 w-px bg-green-200" />

        <div className="space-y-5">
          {sorted.map((schedule) => (
            <div key={schedule.id} className="relative">
              {/* 타임라인 점 */}
              <div className="absolute -left-6 top-1 w-[15px] h-[15px] rounded-full border-2 border-primary bg-white" />

              <div className="flex gap-4">
                <span className="text-sm font-semibold text-primary min-w-[45px]">
                  {formatTime(schedule.time)}
                </span>
                <div>
                  <p className="text-sm font-medium text-gray-800">
                    {schedule.title}
                  </p>
                  {schedule.description && (
                    <p className="text-xs text-gray-400 mt-0.5">
                      {schedule.description}
                    </p>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default ScheduleSection;
