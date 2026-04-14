import { type FC, useState } from "react";
import type { HostRequest, HostRole } from "../../types";
import { eventApi } from "../../api/eventApi";
import { inputCls, labelCls, errorCls } from "../../../../global/styles/formStyles";
import ProfileUpload from "./ProfileUpload";

interface Props {
  label: string;
  role: HostRole;
  data: HostRequest;
  setData: (d: HostRequest) => void;
  nameKey: string;
  errors: Record<string, string>;
  showParents: boolean;
}

const PersonForm: FC<Props> = ({ label, data, setData, nameKey, errors, showParents }) => {
  const [preview, setPreview] = useState<string | null>(data.profileImageUrl ?? null);
  const [uploading, setUploading] = useState(false);

  const handleFileSelect = async (file: File) => {
    if (preview?.startsWith("blob:")) URL.revokeObjectURL(preview);
    const objectUrl = URL.createObjectURL(file);
    setPreview(objectUrl);
    setUploading(true);
    try {
      const url = await eventApi.uploadFile(file);
      setData({ ...data, profileImageUrl: url });
    } catch {
      setPreview(null);
      setData({ ...data, profileImageUrl: undefined });
    } finally {
      setUploading(false);
    }
  };

  const handleRemoveProfile = () => {
    if (preview?.startsWith("blob:")) URL.revokeObjectURL(preview);
    setPreview(null);
    setData({ ...data, profileImageUrl: undefined });
  };

  const wh = data.weddingHostData ?? {};
  const setWh = (patch: Partial<typeof wh>) =>
    setData({ ...data, weddingHostData: { ...wh, ...patch } });

  return (
    <div className="bg-white rounded-2xl shadow-lg p-6 border border-green-100 space-y-4">
      <h3 className="text-sm font-semibold text-primary">{label}</h3>

      <ProfileUpload
        preview={preview}
        uploading={uploading}
        onFileSelect={handleFileSelect}
        onRemove={handleRemoveProfile}
      />

      <div>
        <label className={labelCls}>이름 *</label>
        <input
          value={data.name}
          onChange={(e) => setData({ ...data, name: e.target.value })}
          placeholder="이름"
          className={inputCls}
        />
        {errors[nameKey] && <p className={errorCls}>{errors[nameKey]}</p>}
      </div>

      {showParents && (
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className={labelCls}>아버지 성함</label>
            <input
              value={wh.fatherName ?? ""}
              onChange={(e) => setWh({ fatherName: e.target.value })}
              className={inputCls}
            />
            <label className="flex items-center gap-2 mt-1.5 text-xs text-gray-500">
              <input
                type="checkbox"
                checked={!(wh.isFatherAlive ?? true)}
                onChange={(e) => setWh({ isFatherAlive: !e.target.checked })}
                className="rounded"
              />
              故人
            </label>
          </div>
          <div>
            <label className={labelCls}>어머니 성함</label>
            <input
              value={wh.motherName ?? ""}
              onChange={(e) => setWh({ motherName: e.target.value })}
              className={inputCls}
            />
            <label className="flex items-center gap-2 mt-1.5 text-xs text-gray-500">
              <input
                type="checkbox"
                checked={!(wh.isMotherAlive ?? true)}
                onChange={(e) => setWh({ isMotherAlive: !e.target.checked })}
                className="rounded"
              />
              故人
            </label>
          </div>
        </div>
      )}

      <div>
        <label className={labelCls}>이메일 *</label>
        <input
          value={data.email ?? ""}
          onChange={(e) => setData({ ...data, email: e.target.value })}
          placeholder="example@email.com"
          className={inputCls}
        />
        {errors[`${nameKey}Email`] && (
          <p className={errorCls}>{errors[`${nameKey}Email`]}</p>
        )}
      </div>

      <div>
        <label className={labelCls}>연락처</label>
        <input
          value={data.contact ?? ""}
          onChange={(e) => setData({ ...data, contact: e.target.value })}
          placeholder="010-1234-5678"
          className={inputCls}
        />
      </div>

      <div>
        <label className={labelCls}>소개</label>
        <textarea
          value={data.introduction ?? ""}
          onChange={(e) => setData({ ...data, introduction: e.target.value })}
          placeholder="간단한 소개글"
          rows={2}
          className={`${inputCls} resize-none`}
        />
      </div>
    </div>
  );
};

export default PersonForm;
