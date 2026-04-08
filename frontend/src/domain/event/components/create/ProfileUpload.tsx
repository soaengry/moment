import { type FC, useRef } from "react";
import { IoClose, IoPersonOutline } from "react-icons/io5";
import { validateImageFile } from "../../../../global/utils/errorHandler";

interface Props {
  preview: string | null;
  uploading: boolean;
  onFileSelect: (file: File) => void;
  onRemove: () => void;
}

const ProfileUpload: FC<Props> = ({ preview, uploading, onFileSelect, onRemove }) => {
  const inputRef = useRef<HTMLInputElement>(null);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    if (!validateImageFile(file)) { e.target.value = ""; return; }
    onFileSelect(file);
    e.target.value = "";
  };

  return (
    <div className="flex flex-col items-center gap-2">
      <div className="relative">
        {preview ? (
          <div className="w-20 h-20 rounded-full overflow-hidden border-2 border-gray-200 bg-gray-100">
            <img src={preview} alt="프로필" className="w-full h-full object-cover" />
          </div>
        ) : (
          <button
            type="button"
            onClick={() => inputRef.current?.click()}
            className="w-20 h-20 rounded-full border-2 border-dashed border-gray-300 flex items-center justify-center text-gray-400 hover:border-primary hover:text-primary transition-colors"
          >
            <IoPersonOutline size={28} />
          </button>
        )}

        {preview && (
          <>
            <button
              type="button"
              onClick={onRemove}
              className="absolute -top-1 -right-1 w-6 h-6 bg-black/50 rounded-full flex items-center justify-center hover:bg-black/70 transition-colors"
            >
              <IoClose size={14} className="text-white" />
            </button>
            <button
              type="button"
              onClick={() => inputRef.current?.click()}
              className="absolute bottom-0 left-0 right-0 h-[28%] flex items-center justify-center rounded-b-full bg-black/50 text-white text-[10px] font-medium"
            >
              변경
            </button>
          </>
        )}

        {uploading && (
          <div className="absolute inset-0 flex items-center justify-center rounded-full bg-black/40">
            <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
          </div>
        )}
      </div>

      <input
        ref={inputRef}
        type="file"
        accept="image/jpeg,image/jpg,image/png,image/webp"
        onChange={handleChange}
        className="hidden"
      />
      <span className="text-[10px] text-gray-400">프로필 사진 (선택)</span>
    </div>
  );
};

export default ProfileUpload;
