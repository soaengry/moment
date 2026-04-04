import { type FC, useState, useEffect, useCallback } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { kenBurns } from "../../../global/constants/animations";
import type { GalleryResponse } from "../types";

interface Props {
  gallery: GalleryResponse[];
  title: string;
  eventDate: string;
  groomName?: string;
  brideName?: string;
}

const LandingSection: FC<Props> = ({
  gallery,
  title,
  eventDate,
  groomName,
  brideName,
}) => {
  const images = [...gallery].sort((a, b) => a.orderIndex - b.orderIndex);
  const [current, setCurrent] = useState(0);

  const next = useCallback(() => {
    if (images.length <= 1) return;
    setCurrent((prev) => (prev + 1) % images.length);
  }, [images.length]);

  useEffect(() => {
    if (images.length <= 1) return;
    const timer = setInterval(next, 3000);
    return () => clearInterval(timer);
  }, [images.length, next]);

  const formatDate = (dateStr: string) => {
    const d = new Date(dateStr);
    const DAYS = ["일", "월", "화", "수", "목", "금", "토"];
    const year = d.getFullYear();
    const month = d.getMonth() + 1;
    const day = d.getDate();
    const dow = DAYS[d.getDay()];
    return `${year}. ${month}. ${day}. ${dow}요일`;
  };

  return (
    <section
      className="relative w-full overflow-hidden bg-black"
      style={{ minHeight: "85vh" }}
    >
      {/* 슬라이드 이미지 */}
      {images.length > 0 ? (
        <div className="relative w-full h-full" style={{ minHeight: "85vh" }}>
          <AnimatePresence mode="wait">
            <motion.div
              key={current}
              variants={kenBurns}
              initial="hidden"
              animate="visible"
              exit="exit"
              className="absolute inset-0"
            >
              <img
                src={images[current].imageUrl}
                alt={images[current].caption ?? `슬라이드 ${current + 1}`}
                className="w-full h-full object-cover"
                style={{ minHeight: "85vh" }}
              />
            </motion.div>
          </AnimatePresence>
          {/* 하단 그라데이션 오버레이 */}
          <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-transparent to-black/20 pointer-events-none" />
        </div>
      ) : (
        <div
          className="w-full bg-gradient-to-b from-[#f5ede4] to-[#e8ddd0]"
          style={{ minHeight: "85vh" }}
        />
      )}

      {/* 하단 텍스트 오버레이 */}
      <div className="absolute bottom-0 left-0 right-0 pb-12 px-6 text-center text-white">
        {groomName && brideName && (
          <p className="text-lg tracking-[0.3em] font-light mb-3 drop-shadow-lg">
            {groomName} <span className="text-white/60 mx-2">&</span>{" "}
            {brideName}
          </p>
        )}
        <h1 className="text-2xl font-semibold mb-4 drop-shadow-lg leading-relaxed">
          {title}
        </h1>
        <p className="text-sm tracking-[0.15em] text-white/80 drop-shadow">
          {formatDate(eventDate)}
        </p>

        {/* 슬라이드 인디케이터 */}
        {images.length > 1 && (
          <div className="flex justify-center gap-2 mt-6">
            {images.map((_, i) => (
              <button
                key={i}
                onClick={() => setCurrent(i)}
                className={`w-2 h-2 rounded-full transition-all duration-300 ${
                  i === current ? "bg-white w-6" : "bg-white/40"
                }`}
              />
            ))}
          </div>
        )}
      </div>
    </section>
  );
};

export default LandingSection;
