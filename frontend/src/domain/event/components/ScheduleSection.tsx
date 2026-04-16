import { type FC, useRef } from "react";
import { motion, useInView } from "framer-motion";
import { slideUp } from "../../../global/constants/animations";
import type { ScheduleResponse, EventType } from "../types";
import { TEMPLATE_LABELS } from "../utils/templateLabels";

interface Props {
  schedules: ScheduleResponse[];
  eventType: EventType;
}

const ScheduleSection: FC<Props> = ({ schedules, eventType }) => {
  const tl = TEMPLATE_LABELS[eventType];
  const ref = useRef(null);
  const isInView = useInView(ref, { once: true, amount: 0.3 });

  if (schedules.length === 0) return null;

  const sorted = [...schedules].sort((a, b) => a.orderIndex - b.orderIndex);

  return (
    <motion.section
      ref={ref}
      variants={slideUp}
      initial="hidden"
      animate={isInView ? "visible" : "hidden"}
      className="py-10 px-6"
    >
      <p className="text-[10px] tracking-[0.4em] text-primary/40 mb-8 uppercase font-medium text-center">
        {tl.scheduleTitle}
      </p>

      <div className="relative max-w-sm mx-auto">
        {/* 타임라인 세로선 */}
        <div className="absolute left-[6px] top-3 bottom-3 w-px bg-primary/15" />

        <div className="space-y-6">
          {sorted.map((schedule, i) => (
            <div key={schedule.id} className="relative pl-8">
              {/* 타임라인 점 */}
              <div
                className={`absolute left-0 top-1 w-[13px] h-[13px] rounded-full border-2 ${
                  i === 0 ? "border-primary bg-primary" : "border-primary/40 bg-white"
                }`}
              />

              <div>
                <p className="text-sm font-medium text-gray-700">
                  {schedule.title}
                </p>
                {schedule.description && (
                  <p className="text-xs text-gray-400 mt-0.5">
                    {schedule.description}
                  </p>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>
    </motion.section>
  );
};

export default ScheduleSection;
