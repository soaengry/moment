import { type FC, useEffect, useRef } from "react";
import { motion, useInView } from "framer-motion";
import { slideUp } from "../../../global/constants/animations";
import { toast } from "react-toastify";
import type { WeddingResponse } from "../types";
import { ENV } from "../../../global/config/env";

interface Props {
  wedding: WeddingResponse;
}

const loadKakaoMapSdk = (): Promise<void> => {
  return new Promise((resolve, reject) => {
    if (window.kakao?.maps) {
      window.kakao.maps.load(() => resolve());
      return;
    }
    const script = document.createElement("script");
    script.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=${ENV.KAKAO_MAP_KEY}&autoload=false`;
    script.onload = () => { window.kakao.maps.load(() => resolve()); };
    script.onerror = () => reject(new Error("카카오맵 로드 실패"));
    document.head.appendChild(script);
  });
};

const LocationSection: FC<Props> = ({ wedding }) => {
  const sectionRef = useRef(null);
  const isInView = useInView(sectionRef, { once: true, amount: 0.3 });
  const mapRef = useRef<HTMLDivElement>(null);
  const hasMap = wedding.venueLat != null && wedding.venueLng != null;

  useEffect(() => {
    if (!hasMap || !mapRef.current || !ENV.KAKAO_MAP_KEY) return;
    let isMounted = true;
    loadKakaoMapSdk()
      .then(() => {
        if (!isMounted || !mapRef.current) return;
        const { kakao } = window;
        const position = new kakao.maps.LatLng(wedding.venueLat!, wedding.venueLng!);
        const map = new kakao.maps.Map(mapRef.current, { center: position, level: 3 });
        new kakao.maps.Marker({ map, position });
      })
      .catch(() => {});
    return () => { isMounted = false; };
  }, [hasMap, wedding.venueLat, wedding.venueLng]);

  if (!hasMap && !wedding.mapImageUrl) return null;

  const handleCopyAddress = async () => {
    try {
      await navigator.clipboard.writeText(wedding.venueAddress);
      toast.success("주소가 복사되었습니다");
    } catch { toast.error("주소 복사에 실패했습니다"); }
  };

  const handleOpenKakaoMap = () => {
    const url = hasMap
      ? `https://map.kakao.com/link/map/${wedding.venueName},${wedding.venueLat},${wedding.venueLng}`
      : `https://map.kakao.com/link/search/${encodeURIComponent(wedding.venueAddress)}`;
    window.open(url, "_blank");
  };

  const handleOpenNavi = () => {
    const url = hasMap
      ? `https://map.kakao.com/link/to/${wedding.venueName},${wedding.venueLat},${wedding.venueLng}`
      : `https://map.kakao.com/link/search/${encodeURIComponent(wedding.venueAddress)}`;
    window.open(url, "_blank");
  };

  return (
    <motion.section
      ref={sectionRef}
      variants={slideUp}
      initial="hidden"
      animate={isInView ? "visible" : "hidden"}
      className="py-10 px-6"
    >
      <p className="text-[10px] tracking-[0.4em] text-primary/40 mb-8 uppercase font-medium text-center">
        Location
      </p>

      {hasMap && ENV.KAKAO_MAP_KEY && (
        <div ref={mapRef} className="w-full h-60 rounded-2xl mb-4 bg-gray-100 shadow-inner" />
      )}

      {wedding.mapImageUrl && (
        <img src={wedding.mapImageUrl} alt="약도" className="w-full rounded-2xl mb-4" />
      )}

      <div className="flex gap-2 justify-center">
        <button
          onClick={handleOpenKakaoMap}
          className="flex-1 max-w-[120px] py-2.5 rounded-xl bg-primary text-white text-xs font-medium hover:bg-primaryHover transition-colors"
        >
          카카오맵
        </button>
        <button
          onClick={handleOpenNavi}
          className="flex-1 max-w-[120px] py-2.5 rounded-xl bg-[#3B5998] text-white text-xs font-medium hover:opacity-90 transition-colors"
        >
          길찾기
        </button>
        <button
          onClick={handleCopyAddress}
          className="flex-1 max-w-[120px] py-2.5 rounded-xl border border-gray-200 text-gray-600 text-xs font-medium hover:bg-gray-50 transition-colors"
        >
          주소 복사
        </button>
      </div>
    </motion.section>
  );
};

export default LocationSection;
