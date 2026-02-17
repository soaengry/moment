import { type FC } from "react";
import { useForm } from "react-hook-form";
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
  venueLat: string;
  venueLng: string;
  venuePhone: string;
  mapImageUrl: string;
}

const BasicInfoStep: FC<Props> = ({ initialData, onSubmit }) => {
  const dateFromISO = initialData?.weddingDate
    ? new Date(initialData.weddingDate)
    : null;

  const {
    register,
    handleSubmit,
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
      venueLat: initialData?.venueLat?.toString() ?? "",
      venueLng: initialData?.venueLng?.toString() ?? "",
      venuePhone: initialData?.venuePhone ?? "",
      mapImageUrl: initialData?.mapImageUrl ?? "",
    },
  });

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
      venueLat: values.venueLat ? Number(values.venueLat) : undefined,
      venueLng: values.venueLng ? Number(values.venueLng) : undefined,
      venuePhone: values.venuePhone || undefined,
      mapImageUrl: values.mapImageUrl || undefined,
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
          <input
            {...register("venueAddress", {
              required: "주소를 입력해주세요",
            })}
            placeholder="서울시 강남구 ○○로 123"
            className={inputClass}
          />
          {errors.venueAddress && (
            <p className={errorClass}>{errors.venueAddress.message}</p>
          )}
        </div>

        <div>
          <label className={labelClass}>상세 위치</label>
          <input
            {...register("venueDetail")}
            placeholder="3층 그랜드볼룸"
            className={inputClass}
          />
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className={labelClass}>위도</label>
            <input
              {...register("venueLat")}
              placeholder="37.5665"
              className={inputClass}
            />
          </div>
          <div>
            <label className={labelClass}>경도</label>
            <input
              {...register("venueLng")}
              placeholder="126.9780"
              className={inputClass}
            />
          </div>
        </div>

        <div>
          <label className={labelClass}>예식장 전화번호</label>
          <input
            {...register("venuePhone")}
            placeholder="02-1234-5678"
            className={inputClass}
          />
        </div>

        <div>
          <label className={labelClass}>약도 이미지 URL</label>
          <input
            {...register("mapImageUrl")}
            placeholder="https://..."
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
