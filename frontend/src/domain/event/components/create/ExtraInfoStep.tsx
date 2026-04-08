import { type FC, useState } from "react";
import type {
  TransportationRequest,
  TransportType,
  AnnouncementRequest,
  EventType,
} from "../../types";
import { inputCls, labelCls } from "../../../../global/styles/formStyles";

export interface ExtraInfoFormData {
  notice: string;
  parkingInfo: string;
  mealInfo: string;
  transportation: TransportationRequest[];
  announcements: AnnouncementRequest[];
}

interface Props {
  initialData: ExtraInfoFormData;
  onSubmit: (data: ExtraInfoFormData) => void;
  onBack: () => void;
  isSubmitting: boolean;
  submitLabel?: string;
  templateType?: EventType;
}

const TRANSPORT_TYPE_OPTIONS: { value: TransportType; label: string }[] = [
  { value: "SUBWAY", label: "지하철" },
  { value: "BUS", label: "버스" },
  { value: "SHUTTLE", label: "셔틀" },
];

const emptyTransport = (orderIndex: number): TransportationRequest => ({
  type: "SUBWAY",
  title: "",
  description: "",
  orderIndex,
});

const emptyAnnouncement = (): AnnouncementRequest => ({
  title: "",
  content: "",
  isPinned: false,
});

const ExtraInfoStep: FC<Props> = ({
  initialData,
  onSubmit,
  onBack,
  isSubmitting,
  submitLabel = "초대장 생성",
  templateType,
}) => {
  const isGathering = templateType === "GATHERING";
  const [notice, setNotice] = useState(initialData.notice);
  const [parkingInfo, setParkingInfo] = useState(initialData.parkingInfo);
  const [mealInfo, setMealInfo] = useState(initialData.mealInfo);
  const [transports, setTransports] = useState<TransportationRequest[]>(
    initialData.transportation,
  );
  const [announcements, setAnnouncements] = useState<AnnouncementRequest[]>(
    initialData.announcements,
  );

  // Transport helpers
  const addTransport = () =>
    setTransports((prev) => [...prev, emptyTransport(prev.length)]);
  const removeTransport = (i: number) =>
    setTransports((prev) =>
      prev.filter((_, idx) => idx !== i).map((t, idx) => ({ ...t, orderIndex: idx })),
    );
  const updateTransport = (
    i: number,
    field: keyof TransportationRequest,
    value: string | number,
  ) =>
    setTransports((prev) =>
      prev.map((t, idx) => (idx === i ? { ...t, [field]: value } : t)),
    );

  // Announcement helpers
  const addAnnouncement = () =>
    setAnnouncements((prev) => [...prev, emptyAnnouncement()]);
  const removeAnnouncement = (i: number) =>
    setAnnouncements((prev) => prev.filter((_, idx) => idx !== i));
  const updateAnnouncement = (
    i: number,
    field: keyof AnnouncementRequest,
    value: string | boolean,
  ) =>
    setAnnouncements((prev) =>
      prev.map((a, idx) => (idx === i ? { ...a, [field]: value } : a)),
    );

  const handleSubmit = () => {
    const validTransports = transports
      .filter((t) => t.title.trim())
      .map((t, i) => ({ ...t, orderIndex: i }));
    const validAnnouncements = announcements.filter(
      (a) => a.title.trim() && a.content.trim(),
    );

    onSubmit({
      notice,
      parkingInfo,
      mealInfo,
      transportation: validTransports,
      announcements: validAnnouncements,
    });
  };

  return (
    <div className="space-y-4">
      {/* 안내 정보 - WEDDING 전용 */}
      {!isGathering && (
        <div className="bg-white rounded-2xl shadow-lg p-6 border border-green-100 space-y-4">
          <h3 className="text-sm font-semibold text-primary">안내 정보</h3>

          <div>
            <label className={labelCls}>유의사항</label>
            <textarea
              value={notice}
              onChange={(e) => setNotice(e.target.value)}
              placeholder="하객분들께 전달할 유의사항"
              rows={2}
              className={`${inputCls} resize-none`}
            />
          </div>
          <div>
            <label className={labelCls}>주차 안내</label>
            <textarea
              value={parkingInfo}
              onChange={(e) => setParkingInfo(e.target.value)}
              placeholder="지하 2층 무료주차 가능 (2시간)"
              rows={2}
              className={`${inputCls} resize-none`}
            />
          </div>
          <div>
            <label className={labelCls}>식사 안내</label>
            <input
              value={mealInfo}
              onChange={(e) => setMealInfo(e.target.value)}
              placeholder="2층 뷔페홀"
              className={inputCls}
            />
          </div>
        </div>
      )}

      {/* 교통 안내 */}
      <div className="bg-white rounded-2xl shadow-lg p-6 border border-green-100 space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-semibold text-primary">교통 안내</h3>
          <span className="text-xs text-gray-400">선택사항</span>
        </div>

        {transports.map((t, i) => (
          <div
            key={i}
            className="p-4 rounded-xl bg-bgPrimary space-y-3 relative"
          >
            <button
              type="button"
              onClick={() => removeTransport(i)}
              className="absolute top-3 right-3 w-6 h-6 flex items-center justify-center rounded-full text-gray-400 hover:bg-gray-200 text-xs"
            >
              ✕
            </button>

            <div className="grid grid-cols-3 gap-3">
              <div>
                <label className={labelCls}>유형</label>
                <select
                  value={t.type}
                  onChange={(e) =>
                    updateTransport(i, "type", e.target.value)
                  }
                  className={inputCls}
                >
                  {TRANSPORT_TYPE_OPTIONS.map((opt) => (
                    <option key={opt.value} value={opt.value}>
                      {opt.label}
                    </option>
                  ))}
                </select>
              </div>
              <div className="col-span-2">
                <label className={labelCls}>제목</label>
                <input
                  value={t.title}
                  onChange={(e) => updateTransport(i, "title", e.target.value)}
                  placeholder="2호선 삼성역 5번 출구"
                  className={inputCls}
                />
              </div>
            </div>
            <div>
              <label className={labelCls}>설명</label>
              <input
                value={t.description ?? ""}
                onChange={(e) =>
                  updateTransport(i, "description", e.target.value)
                }
                placeholder="도보 10분"
                className={inputCls}
              />
            </div>
          </div>
        ))}

        <button
          type="button"
          onClick={addTransport}
          className="w-full py-2.5 rounded-lg border-2 border-dashed border-green-200 text-primary text-sm font-medium hover:bg-bgPrimary transition-colors"
        >
          + 교통 수단 추가
        </button>
      </div>

      {/* 공지사항 */}
      <div className="bg-white rounded-2xl shadow-lg p-6 border border-green-100 space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-semibold text-primary">공지사항</h3>
          <span className="text-xs text-gray-400">선택사항</span>
        </div>

        {announcements.map((a, i) => (
          <div
            key={i}
            className="p-4 rounded-xl bg-bgPrimary space-y-3 relative"
          >
            <button
              type="button"
              onClick={() => removeAnnouncement(i)}
              className="absolute top-3 right-3 w-6 h-6 flex items-center justify-center rounded-full text-gray-400 hover:bg-gray-200 text-xs"
            >
              ✕
            </button>
            <div>
              <label className={labelCls}>제목</label>
              <input
                value={a.title}
                onChange={(e) =>
                  updateAnnouncement(i, "title", e.target.value)
                }
                placeholder="공지 제목"
                className={inputCls}
              />
            </div>
            <div>
              <label className={labelCls}>내용</label>
              <textarea
                value={a.content}
                onChange={(e) =>
                  updateAnnouncement(i, "content", e.target.value)
                }
                placeholder="공지 내용"
                rows={3}
                className={`${inputCls} resize-none`}
              />
            </div>
            <label className="flex items-center gap-2 text-xs text-gray-500">
              <input
                type="checkbox"
                checked={a.isPinned}
                onChange={(e) =>
                  updateAnnouncement(i, "isPinned", e.target.checked)
                }
                className="rounded"
              />
              상단 고정
            </label>
          </div>
        ))}

        <button
          type="button"
          onClick={addAnnouncement}
          className="w-full py-2.5 rounded-lg border-2 border-dashed border-green-200 text-primary text-sm font-medium hover:bg-bgPrimary transition-colors"
        >
          + 공지 추가
        </button>
      </div>

      {/* 버튼 */}
      <div className="flex gap-3">
        <button
          type="button"
          onClick={onBack}
          disabled={isSubmitting}
          className="flex-1 py-3 rounded-xl border border-gray-200 text-gray-600 font-semibold hover:bg-gray-50 transition-colors disabled:opacity-50"
        >
          이전
        </button>
        <button
          type="button"
          onClick={handleSubmit}
          disabled={isSubmitting}
          className="flex-1 py-3 rounded-xl bg-primary text-white font-semibold hover:bg-primaryHover transition-colors disabled:opacity-50"
        >
          {isSubmitting ? "저장 중..." : submitLabel}
        </button>
      </div>
    </div>
  );
};

export default ExtraInfoStep;
