import { type FC, useRef } from "react";
import { motion, useInView } from "framer-motion";
import { slideUp } from "../../../global/constants/animations";
import type { WeddingResponse, TransportationResponse } from "../types";

interface Props {
  wedding: WeddingResponse;
  transportation?: TransportationResponse[];
}

const DressCodeSection: FC<Props> = ({ wedding, transportation = [] }) => {
  const ref = useRef(null);
  const isInView = useInView(ref, { once: true, amount: 0.3 });

  const { dressCode, notice, parkingInfo, mealInfo } = wedding;
  const hasContent = dressCode || notice || parkingInfo || mealInfo || transportation.length > 0;

  if (!hasContent) return null;

  const items: { icon: string; title: string; content: string }[] = [];
  if (dressCode) items.push({ icon: "👔", title: "드레스 코드", content: dressCode });
  if (notice) items.push({ icon: "📌", title: "유의사항", content: notice });
  if (parkingInfo) items.push({ icon: "🅿️", title: "주차 안내", content: parkingInfo });
  if (mealInfo) items.push({ icon: "🍽️", title: "식사 안내", content: mealInfo });

  const sorted = [...transportation].sort((a, b) => a.orderIndex - b.orderIndex);

  return (
    <motion.section
      ref={ref}
      variants={slideUp}
      initial="hidden"
      animate={isInView ? "visible" : "hidden"}
      className="py-10 px-6"
    >
      <p className="text-[10px] tracking-[0.4em] text-primary/40 mb-8 uppercase font-medium text-center">
        Information
      </p>

      <div className="space-y-3 max-w-sm mx-auto">
        {items.map((item) => (
          <div key={item.title} className="flex gap-3 p-4 rounded-xl bg-white border border-gray-100">
            <span className="text-lg mt-0.5">{item.icon}</span>
            <div>
              <p className="text-sm font-semibold text-gray-700 mb-1">{item.title}</p>
              <p className="text-xs text-gray-500 whitespace-pre-line leading-relaxed">{item.content}</p>
            </div>
          </div>
        ))}

        {sorted.map((t) => (
          <div key={t.id} className="flex gap-3 p-4 rounded-xl bg-white border border-gray-100">
            <span className="text-lg mt-0.5">
              {t.type === "SUBWAY" ? "🚇" : t.type === "BUS" ? "🚌" : "🚐"}
            </span>
            <div>
              <p className="text-sm font-semibold text-gray-700 mb-1">{t.title}</p>
              {t.description && (
                <p className="text-xs text-gray-500 whitespace-pre-line leading-relaxed">{t.description}</p>
              )}
            </div>
          </div>
        ))}
      </div>
    </motion.section>
  );
};

export default DressCodeSection;
