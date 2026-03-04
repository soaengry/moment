import { type FC, useState, useRef } from "react";
import { IoAdd, IoClose, IoPersonOutline } from "react-icons/io5";
import type { CoupleRequest, CoupleRole } from "../../types";
import { weddingApi } from "../../api/weddingApi";

export interface LandingPhoto {
  file?: File;
  preview: string;
  url?: string; // existing S3 URL (for edit mode)
}

interface Props {
  initialData: CoupleRequest[];
  initialPhotos?: LandingPhoto[];
  onSubmit: (couples: CoupleRequest[], photos: LandingPhoto[]) => void;
  onBack: () => void;
}

const emptyCoupleForm = (role: CoupleRole): CoupleRequest => ({
  role,
  name: "",
  email: "",
  fatherName: "",
  motherName: "",
  isFatherAlive: true,
  isMotherAlive: true,
  contact: "",
  introduction: "",
});

const CoupleStep: FC<Props> = ({
  initialData,
  initialPhotos,
  onSubmit,
  onBack,
}) => {
  const [groom, setGroom] = useState<CoupleRequest>(
    initialData.find((c) => c.role === "GROOM") ?? emptyCoupleForm("GROOM"),
  );
  const [bride, setBride] = useState<CoupleRequest>(
    initialData.find((c) => c.role === "BRIDE") ?? emptyCoupleForm("BRIDE"),
  );
  const [photos, setPhotos] = useState<LandingPhoto[]>(initialPhotos ?? []);
  const [groomPreview, setGroomPreview] = useState<string | null>(
    groom.profileImageUrl ?? null,
  );
  const [bridePreview, setBridePreview] = useState<string | null>(
    bride.profileImageUrl ?? null,
  );
  const [errors, setErrors] = useState<Record<string, string>>({});
  const fileInputRef = useRef<HTMLInputElement>(null);
  const groomProfileRef = useRef<HTMLInputElement>(null);
  const brideProfileRef = useRef<HTMLInputElement>(null);

  const handleAddPhoto = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files) return;

    const remaining = 4 - photos.length;
    const newFiles = Array.from(files).slice(0, remaining);

    const newPhotos: LandingPhoto[] = newFiles.map((file) => ({
      file,
      preview: URL.createObjectURL(file),
    }));

    setPhotos((prev) => [...prev, ...newPhotos]);

    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  const handleRemovePhoto = (index: number) => {
    setPhotos((prev) => {
      const removed = prev[index];
      if (removed.file) {
        URL.revokeObjectURL(removed.preview);
      }
      return prev.filter((_, i) => i !== index);
    });
  };

  const [uploadingProfile, setUploadingProfile] = useState<string | null>(null);

  const handleProfileSelect = async (
    e: React.ChangeEvent<HTMLInputElement>,
    role: "GROOM" | "BRIDE",
    data: CoupleRequest,
    setData: (d: CoupleRequest) => void,
    setPreview: (url: string | null) => void,
    preview: string | null,
  ) => {
    const file = e.target.files?.[0];
    if (!file) return;
    if (preview?.startsWith("blob:")) URL.revokeObjectURL(preview);
    const objectUrl = URL.createObjectURL(file);
    setPreview(objectUrl);
    setUploadingProfile(role);
    try {
      const url = await weddingApi.uploadFile(file);
      setData({ ...data, profileImageUrl: url });
    } catch {
      setPreview(null);
      setData({ ...data, profileImageUrl: undefined });
    } finally {
      setUploadingProfile(null);
      if (e.target) e.target.value = "";
    }
  };

  const handleRemoveProfile = (
    setData: (d: CoupleRequest) => void,
    data: CoupleRequest,
    setPreview: (url: string | null) => void,
    preview: string | null,
    inputRef: React.RefObject<HTMLInputElement | null>,
  ) => {
    if (preview?.startsWith("blob:")) URL.revokeObjectURL(preview);
    setPreview(null);
    setData({ ...data, profileImageUrl: undefined });
    if (inputRef.current) inputRef.current.value = "";
  };

  const handleSubmit = () => {
    const newErrors: Record<string, string> = {};
    if (!groom.name.trim()) newErrors.groomName = "신랑 이름을 입력해주세요";
    if (!bride.name.trim()) newErrors.brideName = "신부 이름을 입력해주세요";
    if (!groom.email.trim())
      newErrors.groomEmail = "신랑 이메일을 입력해주세요";
    if (!bride.email.trim())
      newErrors.brideEmail = "신부 이메일을 입력해주세요";

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    const couples: CoupleRequest[] = [groom, bride];
    onSubmit(couples, photos);
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
    role: "GROOM" | "BRIDE",
    preview: string | null,
    setPreview: (url: string | null) => void,
    profileInputRef: React.RefObject<HTMLInputElement | null>,
  ) => (
    <div className="bg-white rounded-2xl shadow-lg p-6 border border-green-100 space-y-4">
      <h3 className="text-sm font-semibold text-primary">{label}</h3>

      {/* 프로필 사진 */}
      <div className="flex flex-col items-center gap-2">
        <div className="relative">
          {preview ? (
            <div className="w-20 h-20 rounded-full overflow-hidden border-2 border-gray-200 bg-gray-100">
              <img src={preview} alt="프로필" className="w-full h-full object-cover" />
            </div>
          ) : (
            <button
              type="button"
              onClick={() => profileInputRef.current?.click()}
              className="w-20 h-20 rounded-full border-2 border-dashed border-gray-300 flex items-center justify-center text-gray-400 hover:border-primary hover:text-primary transition-colors"
            >
              <IoPersonOutline size={28} />
            </button>
          )}
          {preview && (
            <>
              <button
                type="button"
                onClick={() => handleRemoveProfile(setData, data, setPreview, preview, profileInputRef)}
                className="absolute -top-1 -right-1 w-6 h-6 bg-black/50 rounded-full flex items-center justify-center hover:bg-black/70 transition-colors"
              >
                <IoClose size={14} className="text-white" />
              </button>
              <button
                type="button"
                onClick={() => profileInputRef.current?.click()}
                className="absolute bottom-0 left-0 right-0 h-[28%] flex items-center justify-center rounded-b-full bg-black/50 text-white text-[10px] font-medium"
              >
                변경
              </button>
            </>
          )}
          {uploadingProfile === role && (
            <div className="absolute inset-0 flex items-center justify-center rounded-full bg-black/40">
              <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
            </div>
          )}
        </div>
        <input
          ref={profileInputRef}
          type="file"
          accept="image/jpeg,image/jpg,image/png,image/webp"
          onChange={(e) => handleProfileSelect(e, role, data, setData, setPreview, preview)}
          className="hidden"
        />
        <span className="text-[10px] text-gray-400">프로필 사진 (선택)</span>
      </div>

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
        <label className={labelClass}>이메일 *</label>
        <input
          value={data.email ?? ""}
          onChange={(e) => setData({ ...data, email: e.target.value })}
          placeholder="example@email.com"
          className={inputClass}
        />
        {errors[`${nameKey}Email`] && (
          <p className={errorClass}>{errors[`${nameKey}Email`]}</p>
        )}
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
      {renderPersonForm("신랑 정보", groom, setGroom, "groomName", "GROOM", groomPreview, setGroomPreview, groomProfileRef)}
      {renderPersonForm("신부 정보", bride, setBride, "brideName", "BRIDE", bridePreview, setBridePreview, brideProfileRef)}

      {/* Landing Photos */}
      <div className="bg-white rounded-2xl shadow-lg p-6 border border-green-100 space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-semibold text-primary">랜딩 사진</h3>
          <span className="text-xs text-gray-400">{photos.length}/4장</span>
        </div>
        <p className="text-xs text-gray-400">
          초대장 상단에 표시될 사진을 선택해주세요 (최소 1장, 최대 4장)
        </p>

        {/* Photo grid */}
        <div className="grid grid-cols-2 gap-3">
          {photos.map((photo, index) => (
            <div
              key={index}
              className="relative aspect-[3/4] rounded-xl overflow-hidden border border-gray-200 bg-gray-50"
            >
              <img
                src={photo.preview}
                alt={`랜딩 사진 ${index + 1}`}
                className="w-full h-full object-cover"
              />
              <button
                type="button"
                onClick={() => handleRemovePhoto(index)}
                className="absolute top-2 right-2 w-7 h-7 bg-black/50 rounded-full flex items-center justify-center hover:bg-black/70 transition-colors"
              >
                <IoClose size={16} className="text-white" />
              </button>
              <div className="absolute bottom-2 left-2 bg-black/40 text-white text-[10px] px-2 py-0.5 rounded-full">
                {index + 1}
              </div>
            </div>
          ))}

          {/* Add button */}
          {photos.length < 4 && (
            <button
              type="button"
              onClick={handleAddPhoto}
              className="aspect-[3/4] rounded-xl border-2 border-dashed border-gray-300 flex flex-col items-center justify-center gap-2 text-gray-400 hover:border-primary hover:text-primary transition-colors"
            >
              <IoAdd size={28} />
              <span className="text-xs font-medium">사진 추가</span>
            </button>
          )}
        </div>

        {/* Hidden file input */}
        <input
          ref={fileInputRef}
          type="file"
          accept="image/jpeg,image/jpg,image/png,image/webp"
          multiple
          onChange={handleFileChange}
          className="hidden"
        />
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

export default CoupleStep;
