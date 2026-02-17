import { type FC, useEffect, useRef } from "react";
import { toast } from "react-toastify";
import type { WeddingResponse } from "../types";
import { ENV } from "../../../global/config/env";

interface Props {
  wedding: WeddingResponse;
}

declare global {
  interface Window {
    kakao: {
      maps: {
        load: (callback: () => void) => void;
        Map: new (
          container: HTMLElement,
          options: { center: unknown; level: number },
        ) => unknown;
        LatLng: new (lat: number, lng: number) => unknown;
        Marker: new (options: { map: unknown; position: unknown }) => unknown;
      };
    };
  }
}

const loadKakaoMapSdk = (): Promise<void> => {
  return new Promise((resolve, reject) => {
    if (window.kakao?.maps) {
      window.kakao.maps.load(() => resolve());
      return;
    }

    const script = document.createElement("script");
    script.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=${ENV.KAKAO_MAP_KEY}&autoload=false`;
    script.onload = () => {
      window.kakao.maps.load(() => resolve());
    };
    script.onerror = () => reject(new Error("카카오맵 로드 실패"));
    document.head.appendChild(script);
  });
};

const LocationSection: FC<Props> = ({ wedding }) => {
  const mapRef = useRef<HTMLDivElement>(null);
  const hasMap = wedding.venueLat != null && wedding.venueLng != null;
  const hasMapImage = wedding.mapImageUrl != null;

  useEffect(() => {
    if (!hasMap || !mapRef.current || !ENV.KAKAO_MAP_KEY) return;

    let isMounted = true;

    loadKakaoMapSdk()
      .then(() => {
        if (!isMounted || !mapRef.current) return;
        const { kakao } = window;
        const position = new kakao.maps.LatLng(
          wedding.venueLat!,
          wedding.venueLng!,
        );
        const map = new kakao.maps.Map(mapRef.current, {
          center: position,
          level: 3,
        });
        new kakao.maps.Marker({ map, position });
      })
      .catch(() => {
        // SDK 로드 실패 시 조용히 무시
      });

    return () => {
      isMounted = false;
    };
  }, [hasMap, wedding.venueLat, wedding.venueLng]);

  if (!hasMap && !hasMapImage) return null;

  const handleCopyAddress = async () => {
    try {
      await navigator.clipboard.writeText(wedding.venueAddress);
      toast.success("주소가 복사되었습니다");
    } catch {
      toast.error("주소 복사에 실패했습니다");
    }
  };

  const handleOpenKakaoMap = () => {
    const url = hasMap
      ? `https://map.kakao.com/link/map/${wedding.venueName},${wedding.venueLat},${wedding.venueLng}`
      : `https://map.kakao.com/link/search/${encodeURIComponent(wedding.venueAddress)}`;
    window.open(url, "_blank");
  };

  const handleOpenNavigation = () => {
    const url = hasMap
      ? `https://map.kakao.com/link/to/${wedding.venueName},${wedding.venueLat},${wedding.venueLng}`
      : `https://map.kakao.com/link/search/${encodeURIComponent(wedding.venueAddress)}`;
    window.open(url, "_blank");
  };

  return (
    <section className="bg-white rounded-2xl shadow-lg p-6 border border-green-100">
      <h3 className="text-center text-sm text-gray-400 tracking-widest mb-6">
        LOCATION
      </h3>

      {hasMap && ENV.KAKAO_MAP_KEY && (
        <div
          ref={mapRef}
          className="w-full h-56 rounded-xl mb-4 bg-gray-100"
        />
      )}

      <div className="flex gap-2 justify-center mb-4">
        <button
          onClick={handleOpenKakaoMap}
          className="flex-1 max-w-[140px] py-2.5 rounded-lg bg-primary text-white text-sm font-medium hover:bg-primaryHover transition-colors"
        >
          카카오맵
        </button>
        <button
          onClick={handleOpenNavigation}
          className="flex-1 max-w-[140px] py-2.5 rounded-lg bg-gold text-white text-sm font-medium hover:opacity-90 transition-colors"
        >
          길찾기
        </button>
        <button
          onClick={handleCopyAddress}
          className="flex-1 max-w-[140px] py-2.5 rounded-lg border border-gray-200 text-gray-600 text-sm font-medium hover:bg-gray-50 transition-colors"
        >
          주소 복사
        </button>
      </div>

      {hasMapImage && (
        <img
          src={wedding.mapImageUrl!}
          alt="약도"
          className="w-full rounded-xl"
        />
      )}
    </section>
  );
};

export default LocationSection;
