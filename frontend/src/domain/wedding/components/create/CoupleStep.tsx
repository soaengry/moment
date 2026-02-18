import { type FC, useState } from "react";
import type { CoupleRequest, CoupleRole } from "../../types";

interface Props {
  initialData: CoupleRequest[];
  onSubmit: (couples: CoupleRequest[]) => void;
  onBack: () => void;
}

const emptyCoupleForm = (role: CoupleRole): CoupleRequest => ({
  role,
  name: "",
  fatherName: "",
  motherName: "",
  isFatherAlive: true,
  isMotherAlive: true,
  contact: "",
  introduction: "",
});

const CoupleStep: FC<Props> = ({ initialData, onSubmit, onBack }) => {
  const [groom, setGroom] = useState<CoupleRequest>(
    initialData.find((c) => c.role === "GROOM") ?? emptyCoupleForm("GROOM"),
  );
  const [bride, setBride] = useState<CoupleRequest>(
    initialData.find((c) => c.role === "BRIDE") ?? emptyCoupleForm("BRIDE"),
  );
  const [errors, setErrors] = useState<Record<string, string>>({});

  const handleSubmit = () => {
    const newErrors: Record<string, string> = {};
    if (!groom.name.trim()) newErrors.groomName = "신랑 이름을 입력해주세요";
    if (!bride.name.trim()) newErrors.brideName = "신부 이름을 입력해주세요";

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    const couples: CoupleRequest[] = [groom, bride];
    onSubmit(couples);
  };

  const inputClass =
    "w-full px-4 py-2.5 rounded-lg border border-gray-200 text-sm focus:outline-none focus:border-primary";
  const labelClass = "block text-sm font-medium text-gray-700 mb-1";
  const errorClass = "text-xs text-rose mt-1";

  const renderPersonForm = (
    label: string,
    data: CoupleRequest,
    setData: (d: CoupleRequest) => void,
    nameKey: string,
  ) => (
    <div className="bg-white rounded-2xl shadow-lg p-6 border border-green-100 space-y-4">
      <h3 className="text-sm font-semibold text-primary">{label}</h3>

      <div>
        <label className={labelClass}>이름 *</label>
        <input
          value={data.name}
          onChange={(e) => setData({ ...data, name: e.target.value })}
          placeholder="이름"
          className={inputClass}
        />
        {errors[nameKey] && <p className={errorClass}>{errors[nameKey]}</p>}
      </div>

      <div className="grid grid-cols-2 gap-3">
        <div>
          <label className={labelClass}>아버지 성함</label>
          <input
            value={data.fatherName ?? ""}
            onChange={(e) => setData({ ...data, fatherName: e.target.value })}
            className={inputClass}
          />
          <label className="flex items-center gap-2 mt-1.5 text-xs text-gray-500">
            <input
              type="checkbox"
              checked={!(data.isFatherAlive ?? true)}
              onChange={(e) =>
                setData({ ...data, isFatherAlive: !e.target.checked })
              }
              className="rounded"
            />
            故人
          </label>
        </div>
        <div>
          <label className={labelClass}>어머니 성함</label>
          <input
            value={data.motherName ?? ""}
            onChange={(e) => setData({ ...data, motherName: e.target.value })}
            className={inputClass}
          />
          <label className="flex items-center gap-2 mt-1.5 text-xs text-gray-500">
            <input
              type="checkbox"
              checked={!(data.isMotherAlive ?? true)}
              onChange={(e) =>
                setData({ ...data, isMotherAlive: !e.target.checked })
              }
              className="rounded"
            />
            故人
          </label>
        </div>
      </div>

      <div>
        <label className={labelClass}>연락처</label>
        <input
          value={data.contact ?? ""}
          onChange={(e) => setData({ ...data, contact: e.target.value })}
          placeholder="010-1234-5678"
          className={inputClass}
        />
      </div>

      <div>
        <label className={labelClass}>소개</label>
        <textarea
          value={data.introduction ?? ""}
          onChange={(e) => setData({ ...data, introduction: e.target.value })}
          placeholder="간단한 소개글"
          rows={2}
          className={`${inputClass} resize-none`}
        />
      </div>
    </div>
  );

  return (
    <div className="space-y-4">
      {renderPersonForm("신랑 정보", groom, setGroom, "groomName")}
      {renderPersonForm("신부 정보", bride, setBride, "brideName")}

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

export default CoupleStep;
