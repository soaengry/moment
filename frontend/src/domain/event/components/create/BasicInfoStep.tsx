import { type FC, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import DaumPostcodeEmbed from "react-daum-postcode";
import type { EventRequest, EventType } from "../../types";
import { EVENT_VALIDATION } from "../../event.constants";
import { eventApi } from "../../api/eventApi";
import { TEMPLATE_LABELS } from "../../utils/templateLabels";
import { inputCls, labelCls, errorCls } from "../../../../global/styles/formStyles";

interface Props {
  initialData: EventRequest | null;
  templateType: EventType;
  onSubmit: (data: EventRequest) => void;
}

const eventSchema = z.object({
  title: z.string().min(1, "제목을 입력해주세요."),
  slug: z
    .string()
    .min(
      EVENT_VALIDATION.SLUG_MIN_LENGTH,
      `초대장 ID는 최소 ${EVENT_VALIDATION.SLUG_MIN_LENGTH}자 이상이어야 합니다.`,
    )
    .max(
      EVENT_VALIDATION.SLUG_MAX_LENGTH,
      `초대장 ID는 최대 ${EVENT_VALIDATION.SLUG_MAX_LENGTH}자 이하여야 합니다.`,
    )
    .regex(
      EVENT_VALIDATION.SLUG_PATTERN,
      "영문 소문자, 숫자, '-'만 사용할 수 있습니다.",
    ),
  date: z.string().min(1, "날짜를 입력해주세요."),
  eventTime: z.string().min(1, "시간을 입력해주세요."),
  locationName: z.string().min(1, "장소 이름을 입력해주세요."),
  locationAddress: z.string().min(1, "주소를 입력해주세요."),
  locationDetail: z.string().optional(),
});

type FormValues = z.infer<typeof eventSchema>;

const BasicInfoStep: FC<Props> = ({ initialData, templateType, onSubmit }) => {
  const labels = TEMPLATE_LABELS[templateType];
  const dateFromISO = initialData?.date
    ? new Date(initialData.date)
    : null;

  const {
    register,
    handleSubmit,
    setValue,
    getValues,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(eventSchema),
    defaultValues: {
      title: initialData?.title ?? "",
      slug: initialData?.slug ?? "",
      date: dateFromISO ? dateFromISO.toISOString().slice(0, 10) : "",
      eventTime: dateFromISO ? dateFromISO.toTimeString().slice(0, 5) : "",
      locationName: initialData?.locationName ?? "",
      locationAddress: initialData?.locationAddress ?? "",
      locationDetail: initialData?.locationDetail ?? "",
    },
  });

  const [showPostcode, setShowPostcode] = useState(false);
  const [slugDupError, setSlugDupError] = useState<string | null>(null);
  const [isPublic, setIsPublic] = useState(initialData?.isPublic ?? false);

  const handleSlugBlur = async () => {
    const slug = getValues("slug");
    if (!slug || slug.length < EVENT_VALIDATION.SLUG_MIN_LENGTH) return;

    try {
      const { exists } = await eventApi.checkSlug(slug);
      setSlugDupError(exists ? "이미 사용 중인 초대장 ID입니다." : null);
    } catch {
      // 중복 체크 실패 시 무시
    }
  };

  const handlePostcodeComplete = (data: {
    address: string;
    roadAddress: string;
    jibunAddress: string;
    zonecode: string;
  }) => {
    const address = data.roadAddress || data.address;
    setValue("locationAddress", address, { shouldValidate: true });
    setShowPostcode(false);
  };

  const onFormSubmit = (values: FormValues) => {
    if (slugDupError) return;

    // LocalDateTime 형식으로 전송 (타임존 변환 없이 KST 그대로)
    const date = `${values.date}T${values.eventTime || "00:00"}:00`;

    const request: EventRequest = {
      title: values.title,
      slug: values.slug,
      date,
      locationName: values.locationName,
      locationAddress: values.locationAddress,
      locationDetail: values.locationDetail || undefined,
      isPublic,
    };
    onSubmit(request);
  };

  return (
    <form onSubmit={handleSubmit(onFormSubmit)} className="space-y-4">
      <div className="bg-white rounded-2xl shadow-lg p-6 border border-green-100 space-y-4">
        <div>
          <label className={labelCls}>초대장 제목 *</label>
          <input
            {...register("title", { required: "제목을 입력해주세요" })}
            placeholder={templateType === "GATHERING" ? "○○ 모임" : "○○ ♥ ○○ 결혼합니다"}
            className={inputCls}
          />
          {errors.title && <p className={errorCls}>{errors.title.message}</p>}
        </div>
        <div>
          <label className={labelCls}>초대장 아이디 *</label>
          <input
            {...register("slug", { required: "아이디를 입력해주세요" })}
            onBlur={handleSlugBlur}
            placeholder="영문 소문자, 숫자, - 만 사용 가능"
            className={inputCls}
          />
          {errors.slug && (
            <p className={errorCls}>{errors.slug.message}</p>
          )}
          {slugDupError && (
            <p className={errorCls}>{slugDupError}</p>
          )}
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className={labelCls}>{labels.dateLabel} *</label>
            <input
              type="date"
              {...register("date", { required: "날짜를 선택해주세요" })}
              className={inputCls}
            />
            {errors.date && (
              <p className={errorCls}>{errors.date.message}</p>
            )}
          </div>
          <div>
            <label className={labelCls}>{labels.timeLabel} *</label>
            <input
              type="time"
              {...register("eventTime", {
                required: "시간을 선택해주세요",
              })}
              className={inputCls}
            />
            {errors.eventTime && (
              <p className={errorCls}>{errors.eventTime.message}</p>
            )}
          </div>
        </div>

        <div>
          <label className={labelCls}>{labels.venueLabel} *</label>
          <input
            {...register("locationName", {
              required: `${labels.venueLabel}을 입력해주세요`,
            })}
            placeholder={templateType === "GATHERING" ? "○○" : "○○호텔 그랜드홀"}
            className={inputCls}
          />
          {errors.locationName && (
            <p className={errorCls}>{errors.locationName.message}</p>
          )}
        </div>

        <div>
          <label className={labelCls}>주소 *</label>
          <div className="flex gap-2">
            <input
              {...register("locationAddress", {
                required: "주소를 입력해주세요",
              })}
              readOnly
              placeholder="주소 검색 버튼을 클릭해주세요"
              className={`${inputCls} bg-gray-50 cursor-pointer`}
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
          {errors.locationAddress && (
            <p className={errorCls}>{errors.locationAddress.message}</p>
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

        <div>
          <label className={labelCls}>상세 위치</label>
          <input
            {...register("locationDetail")}
            placeholder="3층 그랜드볼룸"
            className={inputCls}
          />
        </div>

        <div className="flex items-center justify-between py-1">
          <div>
            <p className="text-sm font-medium text-gray-700">일정 공개 여부</p>
            <p className="text-xs text-gray-400 mt-0.5">
              {isPublic ? "누구나 이 일정을 볼 수 있습니다" : "참석자만 이 일정을 볼 수 있습니다"}
            </p>
          </div>
          <button
            type="button"
            onClick={() => setIsPublic((v) => !v)}
            className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
              isPublic ? "bg-primary" : "bg-gray-200"
            }`}
          >
            <span
              className={`inline-block h-4 w-4 transform rounded-full bg-white shadow transition-transform ${
                isPublic ? "translate-x-6" : "translate-x-1"
              }`}
            />
          </button>
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
