import { type FC, useState, useRef, useEffect } from "react";
import { IoClose } from "react-icons/io5";

interface Props {
  imageUrls: string[];
  initialIndex: number;
  onClose: () => void;
}

const ImageViewer: FC<Props> = ({ imageUrls, initialIndex, onClose }) => {
  const [currentIndex, setCurrentIndex] = useState(initialIndex);
  const [translateX, setTranslateX] = useState(0);
  const [isTransitioning, setIsTransitioning] = useState(false);
  const [isDragging, setIsDragging] = useState(false);
  const startXRef = useRef(0);
  const isDraggingRef = useRef(false);

  // body scroll 잠금
  useEffect(() => {
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = "";
    };
  }, []);

  const handleTouchStart = (e: React.TouchEvent) => {
    if (isTransitioning) return;
    startXRef.current = e.touches[0].clientX;
    isDraggingRef.current = true;
    setIsDragging(true);
    setTranslateX(0);
  };

  const handleTouchMove = (e: React.TouchEvent) => {
    if (!isDraggingRef.current) return;
    const diff = e.touches[0].clientX - startXRef.current;

    // 첫 이미지에서 오른쪽 스와이프(이전) 제한
    if (currentIndex === 0 && diff > 0) {
      setTranslateX(diff * 0.3);
      return;
    }
    // 마지막 이미지에서 왼쪽 스와이프(다음) 제한
    if (currentIndex === imageUrls.length - 1 && diff < 0) {
      setTranslateX(diff * 0.3);
      return;
    }

    setTranslateX(diff);
  };

  const handleTouchEnd = () => {
    if (!isDraggingRef.current) return;
    isDraggingRef.current = false;
    setIsDragging(false);

    const threshold = 50;

    if (translateX < -threshold && currentIndex < imageUrls.length - 1) {
      setIsTransitioning(true);
      setCurrentIndex((prev) => prev + 1);
    } else if (translateX > threshold && currentIndex > 0) {
      setIsTransitioning(true);
      setCurrentIndex((prev) => prev - 1);
    }

    setTranslateX(0);
    setTimeout(() => setIsTransitioning(false), 300);
  };

  const handleBackdropClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget) onClose();
  };

  return (
    <div
      className="fixed inset-0 z-50 bg-black/95 flex flex-col"
      onClick={handleBackdropClick}
    >
      {/* 상단 바 */}
      <div className="flex items-center justify-between px-4 py-3 flex-shrink-0">
        <span className="text-white/80 text-sm font-medium">
          {currentIndex + 1} / {imageUrls.length}
        </span>
        <button onClick={onClose} className="p-1 text-white/80">
          <IoClose size={24} />
        </button>
      </div>

      {/* 이미지 영역 */}
      <div
        className="flex-1 overflow-hidden"
        onTouchStart={handleTouchStart}
        onTouchMove={handleTouchMove}
        onTouchEnd={handleTouchEnd}
      >
        <div
          className="flex h-full"
          style={{
            transform: `translateX(calc(-${currentIndex * 100}% + ${translateX}px))`,
            transition: isDragging ? "none" : "transform 300ms ease-out",
          }}
        >
          {imageUrls.map((url, i) => (
            <div
              key={i}
              className="w-full h-full flex items-center justify-center flex-shrink-0"
            >
              <img
                src={url}
                alt=""
                className="max-w-full max-h-full object-contain select-none"
                draggable={false}
              />
            </div>
          ))}
        </div>
      </div>

      {/* 하단 dot 인디케이터 */}
      {imageUrls.length > 1 && (
        <div className="flex justify-center gap-1.5 pb-6 pt-3 flex-shrink-0">
          {imageUrls.map((_, i) => (
            <div
              key={i}
              className={`w-1.5 h-1.5 rounded-full transition-colors ${
                i === currentIndex ? "bg-white" : "bg-white/40"
              }`}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default ImageViewer;
