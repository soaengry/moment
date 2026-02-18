import { type FC, useState, useEffect, useRef } from "react";
import { useForm } from "react-hook-form";
import DaumPostcodeEmbed from "react-daum-postcode";
import type { WeddingRequest } from "../../types";

interface Props {
  initialData: WeddingRequest | null;
  onSubmit: (data: WeddingRequest) => void;
}

interface FormValues {
  title: string;
  weddingDate: string;
  weddingTime: string;
  venueName: string;
  venueAddress: string;
  venueDetail: string;
  venuePhone: string;
}

const KAKAO_MAP_KEY = import.meta.env.VITE_KAKAO_JS_KEY ?? "";

const BasicInfoStep: FC<Props> = ({ initialData, onSubmit }) => {
  const dateFromISO = initialData?.weddingDate
    ? new Date(initialData.weddingDate)
    : null;

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<FormValues>({
    defaultValues: {
      title: initialData?.title ?? "",
      weddingDate: dateFromISO
        ? dateFromISO.toISOString().slice(0, 10)
        : "",
      weddingTime: dateFromISO
        ? dateFromISO.toTimeString().slice(0, 5)
        : "",
      venueName: initialData?.venueName ?? "",
      venueAddress: initialData?.venueAddress ?? "",
      venueDetail: initialData?.venueDetail ?? "",
      venuePhone: initialData?.venuePhone ?? "",
    },
  });

  const [showPostcode, setShowPostcode] = useState(false);
  const [mapLoaded, setMapLoaded] = useState(false);
  const mapContainerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<ReturnType<typeof window.kakao.maps.Map> | null>(null);
  const markerRef = useRef<ReturnType<typeof window.kakao.maps.Marker> | null>(null);

  const venueAddress = watch("venueAddress");

  // Load Kakao Maps SDK
  useEffect(() => {
    if (!KAKAO_MAP_KEY) return;
    if (window.kakao?.maps) {
      window.kakao.maps.load(() => setMapLoaded(true));
      return;
    }
    const script = document.createElement("script");
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${KAKAO_MAP_KEY}&libraries=services&autoload=false`;
    script.onload = () => {
      window.kakao.maps.load(() => setMapLoaded(true));
    };
    document.head.appendChild(script);
  }, []);

  // Initialize or update map when address changes
  useEffect(() => {
    if (!mapLoaded || !venueAddress || !mapContainerRef.current) return;

    const geocoder = new window.kakao.maps.services.Geocoder();
    geocoder.addressSearch(venueAddress, (result, status) => {
      if (status !== window.kakao.maps.services.Status.OK || result.length === 0) return;

      const coords = new window.kakao.maps.LatLng(
        parseFloat(result[0].y),
        parseFloat(result[0].x),
      );

      if (!mapRef.current && mapContainerRef.current) {
        mapRef.current = new window.kakao.maps.Map(mapContainerRef.current, {
          center: coords,
          level: 3,
        });
        markerRef.current = new window.kakao.maps.Marker({
          map: mapRef.current,
          position: coords,
        });
      } else if (mapRef.current && markerRef.current) {
        mapRef.current.setCenter(coords);
        markerRef.current.setPosition(coords);
      }
    });
  }, [mapLoaded, venueAddress]);

  const handlePostcodeComplete = (data: {
    address: string;
    roadAddress: string;
    jibunAddress: string;
    zonecode: string;
  }) => {
    const address = data.roadAddress || data.address;
    setValue("venueAddress", address, { shouldValidate: true });
    setShowPostcode(false);
  };

  const onFormSubmit = (values: FormValues) => {
    const weddingDate = new Date(
      `${values.weddingDate}T${values.weddingTime || "00:00"}`,
    ).toISOString();

    const request: WeddingRequest = {
      title: values.title,
      weddingDate,
      venueName: values.venueName,
      venueAddress: values.venueAddress,
      venueDetail: values.venueDetail || undefined,
      venuePhone: values.venuePhone || undefined,
    };
    onSubmit(request);
  };

  const inputClass =
    "w-full px-4 py-2.5 rounded-lg border border-gray-200 text-sm focus:outline-none focus:border-primary";
  const labelClass = "block text-sm font-medium text-gray-700 mb-1";
  const errorClass = "text-xs text-rose mt-1";

  return (
    <form onSubmit={handleSubmit(onFormSubmit)} className="space-y-4">
      <div className="bg-white rounded-2xl shadow-lg p-6 border border-green-100 space-y-4">
        <div>
          <label className={labelClass}>초대장 제목 *</label>
          <input
            {...register("title", { required: "제목을 입력해주세요" })}
            placeholder="○○ ♥ ○○ 결혼합니다"
            className={inputClass}
          />
          {errors.title && <p className={errorClass}>{errors.title.message}</p>}
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className={labelClass}>예식 날짜 *</label>
            <input
              type="date"
              {...register("weddingDate", { required: "날짜를 선택해주세요" })}
              className={inputClass}
            />
            {errors.weddingDate && (
              <p className={errorClass}>{errors.weddingDate.message}</p>
            )}
          </div>
          <div>
            <label className={labelClass}>예식 시간 *</label>
            <input
              type="time"
              {...register("weddingTime", {
                required: "시간을 선택해주세요",
              })}
              className={inputClass}
            />
            {errors.weddingTime && (
              <p className={errorClass}>{errors.weddingTime.message}</p>
            )}
          </div>
        </div>

        <div>
          <label className={labelClass}>예식장 이름 *</label>
          <input
            {...register("venueName", { required: "예식장 이름을 입력해주세요" })}
            placeholder="○○호텔 그랜드홀"
            className={inputClass}
          />
          {errors.venueName && (
            <p className={errorClass}>{errors.venueName.message}</p>
          )}
        </div>

        <div>
          <label className={labelClass}>예식장 주소 *</label>
          <div className="flex gap-2">
            <input
              {...register("venueAddress", {
                required: "주소를 입력해주세요",
              })}
              readOnly
              placeholder="주소 검색 버튼을 클릭해주세요"
              className={`${inputClass} bg-gray-50 cursor-pointer`}
              onClick={() => setShowPostcode(true)}
            />
            <button
              type="button"
              onClick={() => setShowPostcode(!showPostcode)}
              className="px-4 py-2.5 rounded-lg bg-primary text-white text-sm font-medium whitespace-nowrap hover:bg-primaryHover transition-colors"
            >
              주소 검색
            </button>
          </div>
          {errors.venueAddress && (
            <p className={errorClass}>{errors.venueAddress.message}</p>
          )}
        </div>

        {/* Daum Postcode Popup */}
        {showPostcode && (
          <div className="rounded-xl overflow-hidden border border-gray-200">
            <DaumPostcodeEmbed
              onComplete={handlePostcodeComplete}
              style={{ height: 400 }}
            />
          </div>
        )}

        {/* Kakao Map Preview */}
        {venueAddress && (
          <div>
            <label className={labelClass}>지도 미리보기</label>
            <div
              ref={mapContainerRef}
              className="w-full h-48 rounded-xl overflow-hidden border border-gray-200 bg-gray-100"
            />
          </div>
        )}

        <div>
          <label className={labelClass}>상세 위치</label>
          <input
            {...register("venueDetail")}
            placeholder="3층 그랜드볼룸"
            className={inputClass}
          />
        </div>

        <div>
          <label className={labelClass}>예식장 전화번호</label>
          <input
            {...register("venuePhone")}
            placeholder="02-1234-5678"
            className={inputClass}
          />
        </div>
      </div>

      <button
        type="submit"
        className="w-full py-3 rounded-xl bg-primary text-white font-semibold hover:bg-primaryHover transition-colors"
      >
        다음
      </button>
    </form>
  );
};

export default BasicInfoStep;
