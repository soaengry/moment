import { type FC, useState } from "react";
import type { ScheduleRequest } from "../../types";

interface Props {
  initialData: ScheduleRequest[];
  onSubmit: (schedules: ScheduleRequest[]) => void;
  onBack: () => void;
}

const emptySchedule = (orderIndex: number): ScheduleRequest => ({
  time: "",
  title: "",
  description: "",
  orderIndex,
});

const ScheduleStep: FC<Props> = ({ initialData, onSubmit, onBack }) => {
  const [items, setItems] = useState<ScheduleRequest[]>(
    initialData.length > 0 ? initialData : [emptySchedule(0)],
  );

  const handleChange = (
    index: number,
    field: keyof ScheduleRequest,
    value: string | number,
  ) => {
    setItems((prev) =>
      prev.map((item, i) => (i === index ? { ...item, [field]: value } : item)),
    );
  };

  const addItem = () => {
    setItems((prev) => [...prev, emptySchedule(prev.length)]);
  };

  const removeItem = (index: number) => {
    setItems((prev) =>
      prev
        .filter((_, i) => i !== index)
        .map((item, i) => ({ ...item, orderIndex: i })),
    );
  };

  const handleSubmit = () => {
    // 빈 항목 제거 후 전달 (time과 title이 있는 항목만)
    const valid = items
      .filter((s) => s.time && s.title.trim())
      .map((s, i) => ({ ...s, orderIndex: i }));
    onSubmit(valid);
  };

  const inputClass =
    "w-full px-4 py-2.5 rounded-lg border border-gray-200 text-sm focus:outline-none focus:border-primary";
  const labelClass = "block text-sm font-medium text-gray-700 mb-1";

  return (
    <div className="space-y-4">
      <div className="bg-white rounded-2xl shadow-lg p-6 border border-green-100 space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-semibold text-primary">식순</h3>
          <span className="text-xs text-gray-400">선택사항</span>
        </div>

        {items.map((item, index) => (
          <div
            key={index}
            className="p-4 rounded-xl bg-bgPrimary space-y-3 relative"
          >
            {items.length > 1 && (
              <button
                type="button"
                onClick={() => removeItem(index)}
                className="absolute top-3 right-3 w-6 h-6 flex items-center justify-center rounded-full text-gray-400 hover:bg-gray-200 text-xs"
              >
                ✕
              </button>
            )}

            <div className="grid grid-cols-3 gap-3">
              <div>
                <label className={labelClass}>시간</label>
                <input
                  type="time"
                  value={item.time}
                  onChange={(e) => handleChange(index, "time", e.target.value)}
                  className={inputClass}
                />
              </div>
              <div className="col-span-2">
                <label className={labelClass}>일정 제목</label>
                <input
                  value={item.title}
                  onChange={(e) => handleChange(index, "title", e.target.value)}
                  placeholder="하객 입장"
                  className={inputClass}
                />
              </div>
            </div>

            <div>
              <label className={labelClass}>설명</label>
              <input
                value={item.description ?? ""}
                onChange={(e) =>
                  handleChange(index, "description", e.target.value)
                }
                placeholder="선택사항"
                className={inputClass}
              />
            </div>
          </div>
        ))}

        <button
          type="button"
          onClick={addItem}
          className="w-full py-2.5 rounded-lg border-2 border-dashed border-green-200 text-primary text-sm font-medium hover:bg-bgPrimary transition-colors"
        >
          + 식순 추가
        </button>
      </div>

      <div className="flex gap-3">
        <button
          type="button"
          onClick={onBack}
          className="flex-1 py-3 rounded-xl border border-gray-200 text-gray-600 font-semibold hover:bg-gray-50 transition-colors"
        >
          이전
        </button>
        <button
          type="button"
          onClick={handleSubmit}
          className="flex-1 py-3 rounded-xl bg-primary text-white font-semibold hover:bg-primaryHover transition-colors"
        >
          다음
        </button>
      </div>
    </div>
  );
};

export default ScheduleStep;
