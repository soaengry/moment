import { type FC, useRef } from "react";
import { IoAdd, IoClose } from "react-icons/io5";
import type { LandingPhoto } from "./CoupleStep";
import { validateImageFile } from "../../../../global/utils/errorHandler";

const MAX_PHOTOS = 4;

interface Props {
  photos: LandingPhoto[];
  setPhotos: React.Dispatch<React.SetStateAction<LandingPhoto[]>>;
}

const LandingPhotoGrid: FC<Props> = ({ photos, setPhotos }) => {
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files) return;
    const validFiles = Array.from(files)
      .filter(validateImageFile)
      .slice(0, MAX_PHOTOS - photos.length);
    const newPhotos: LandingPhoto[] = validFiles.map((file) => ({
      file,
      preview: URL.createObjectURL(file),
    }));
    setPhotos((prev) => [...prev, ...newPhotos]);
    e.target.value = "";
  };

  const handleRemove = (index: number) => {
    setPhotos((prev) => {
      const removed = prev[index];
      if (removed.file) URL.revokeObjectURL(removed.preview);
      return prev.filter((_, i) => i !== index);
    });
  };

  return (
    <div className="bg-white rounded-2xl shadow-lg p-6 border border-green-100 space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-semibold text-primary">랜딩 사진</h3>
        <span className="text-xs text-gray-400">{photos.length}/{MAX_PHOTOS}장</span>
      </div>
      <p className="text-xs text-gray-400">
        초대장 상단에 표시될 사진을 선택해주세요 (최소 1장, 최대 {MAX_PHOTOS}장)
      </p>

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
              onClick={() => handleRemove(index)}
              className="absolute top-2 right-2 w-7 h-7 bg-black/50 rounded-full flex items-center justify-center hover:bg-black/70 transition-colors"
            >
              <IoClose size={16} className="text-white" />
            </button>
            <div className="absolute bottom-2 left-2 bg-black/40 text-white text-[10px] px-2 py-0.5 rounded-full">
              {index + 1}
            </div>
          </div>
        ))}

        {photos.length < MAX_PHOTOS && (
          <button
            type="button"
            onClick={() => fileInputRef.current?.click()}
            className="aspect-[3/4] rounded-xl border-2 border-dashed border-gray-300 flex flex-col items-center justify-center gap-2 text-gray-400 hover:border-primary hover:text-primary transition-colors"
          >
            <IoAdd size={28} />
            <span className="text-xs font-medium">사진 추가</span>
          </button>
        )}
      </div>

      <input
        ref={fileInputRef}
        type="file"
        accept="image/jpeg,image/jpg,image/png,image/webp"
        multiple
        onChange={handleFileChange}
        className="hidden"
      />
    </div>
  );
};

export default LandingPhotoGrid;
