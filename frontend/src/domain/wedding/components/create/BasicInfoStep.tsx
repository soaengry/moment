import { type FC, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import DaumPostcodeEmbed from "react-daum-postcode";
import type { WeddingRequest } from "../../types";
import { WEDDING_VALIDATION } from "../../wedding.constants";
import { weddingApi } from "../../api/weddingApi";

interface Props {
  initialData: WeddingRequest | null;
  onSubmit: (data: WeddingRequest) => void;
}

const weddingSchema = z.object({
  title: z.string().min(1, "제목을 입력해주세요."),
  invitationId: z
    .string()
    .min(
      WEDDING_VALIDATION.INVITATION_MIN_LENGTH,
      `초대장 ID는 최소 ${WEDDING_VALIDATION.INVITATION_MIN_LENGTH}자 이상이어야 합니다.`,
    )
    .max(
      WEDDING_VALIDATION.INVITATION_MAX_LENGTH,
      `초대장 ID는 최대 ${WEDDING_VALIDATION.INVITATION_MAX_LENGTH}자 이하여야 합니다.`,
    )
    .regex(
      WEDDING_VALIDATION.INVITATION_PATTERN,
      "영문 소문자, 숫자, '-'만 사용할 수 있습니다.",
    ),
  eventDate: z.string().min(1, "날짜를 입력해주세요."),
  weddingTime: z.string().min(1, "시간을 입력해주세요."),
  venueName: z.string().min(1, "예식장 이름을 입력해주세요."),
  venueAddress: z.string().min(1, "주소를 입력해주세요."),
  venueDetail: z.string().optional(),
  venuePhone: z.string().optional(),
});

type FormValues = z.infer<typeof weddingSchema>;

const BasicInfoStep: FC<Props> = ({ initialData, onSubmit }) => {
  const dateFromISO = initialData?.eventDate
    ? new Date(initialData.eventDate)
    : null;

  const {
    register,
    handleSubmit,
    setValue,
    getValues,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(weddingSchema),
    defaultValues: {
      title: initialData?.title ?? "",
      invitationId: initialData?.invitationId ?? "",
      eventDate: dateFromISO ? dateFromISO.toISOString().slice(0, 10) : "",
      weddingTime: dateFromISO ? dateFromISO.toTimeString().slice(0, 5) : "",
      venueName: initialData?.venueName ?? "",
      venueAddress: initialData?.venueAddress ?? "",
      venueDetail: initialData?.venueDetail ?? "",
      venuePhone: initialData?.venuePhone ?? "",
    },
  });

  const [showPostcode, setShowPostcode] = useState(false);
  const [invitationIdDupError, setInvitationIdDupError] = useState<
    string | null
  >(null);

  const handleInvitationIdBlur = async () => {
    const invitationId = getValues("invitationId");
    if (
      !invitationId ||
      invitationId.length < WEDDING_VALIDATION.INVITATION_MIN_LENGTH
    )
      return;

    try {
      const { exists } = await weddingApi.checkInvitationId(invitationId);
      setInvitationIdDupError(
        exists ? "이미 사용 중인 초대장 ID입니다." : null,
      );
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
    setValue("venueAddress", address, { shouldValidate: true });
    setShowPostcode(false);
  };

  const onFormSubmit = (values: FormValues) => {
    if (invitationIdDupError) return; // 중복이면 제출 막기

    // LocalDateTime 형식으로 전송 (타임존 변환 없이 KST 그대로)
    const eventDate = `${values.eventDate}T${values.weddingTime || "00:00"}:00`;

    const request: WeddingRequest = {
      title: values.title,
      invitationId: values.invitationId,
      eventDate,
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
        <div>
          <label className={labelClass}>초대장 아이디 *</label>
          <input
            {...register("invitationId", { required: "아이디를 입력해주세요" })}
            onBlur={handleInvitationIdBlur}
            placeholder="영문 소문자, 숫자, - 만 사용 가능"
            className={inputClass}
          />
          {errors.invitationId && (
            <p className={errorClass}>{errors.invitationId.message}</p>
          )}
          {invitationIdDupError && (
            <p className={errorClass}>{invitationIdDupError}</p>
          )}
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className={labelClass}>예식 날짜 *</label>
            <input
              type="date"
              {...register("eventDate", { required: "날짜를 선택해주세요" })}
              className={inputClass}
            />
            {errors.eventDate && (
              <p className={errorClass}>{errors.eventDate.message}</p>
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
            {...register("venueName", {
              required: "예식장 이름을 입력해주세요",
            })}
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
