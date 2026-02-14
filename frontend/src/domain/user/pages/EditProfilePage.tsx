import { type FC, useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "../../auth/store/useAuthStore";
import { authApi } from "../../auth/api/authApi";
import { AUTH_VALIDATION } from "../../auth/auth.constants";
import { isAxiosError } from "axios";
import axiosInstance from "../../../global/api/axiosInstance";
import { USER_API } from "../../auth/auth.constants";
import type { UserResponse } from "../../auth/types";

const MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
const ALLOWED_TYPES = ["image/jpeg", "image/png"];
const DEFAULT_AVATAR = "/default-avatar.png";

const EditProfilePage: FC = () => {
  const navigate = useNavigate();
  const user = useAuthStore((state) => state.user);
  const setUser = useAuthStore((state) => state.setUser);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [nickname, setNickname] = useState("");
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [imageRemoved, setImageRemoved] = useState(false);
  const [nicknameDupError, setNicknameDupError] = useState<string | null>(null);
  const [imageError, setImageError] = useState<string | null>(null);
  const [serverError, setServerError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (user) {
      setNickname(user.nickname);
      setPreviewUrl(user.profileImageUrl);
    }
  }, [user]);

  useEffect(() => {
    return () => {
      if (previewUrl?.startsWith("blob:")) {
        URL.revokeObjectURL(previewUrl);
      }
    };
  }, [previewUrl]);

  const handleNicknameBlur = async () => {
    if (!nickname || nickname.length < AUTH_VALIDATION.NICKNAME_MIN_LENGTH)
      return;
    if (nickname === user?.nickname) {
      setNicknameDupError(null);
      return;
    }

    try {
      const { exists } = await authApi.checkNickname(nickname);
      setNicknameDupError(exists ? "이미 사용 중인 닉네임입니다." : null);
    } catch {
      // 무시
    }
  };

  const handleImageSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setImageError(null);

    if (!ALLOWED_TYPES.includes(file.type)) {
      setImageError("JPG, PNG 파일만 업로드 가능합니다.");
      return;
    }

    if (file.size > MAX_FILE_SIZE) {
      setImageError("파일 크기는 2MB 이하여야 합니다.");
      return;
    }

    const objectUrl = URL.createObjectURL(file);
    setPreviewUrl(objectUrl);
    setSelectedFile(file);
    setImageRemoved(false);

    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  const handleRemoveImage = () => {
    setPreviewUrl(null);
    setSelectedFile(null);
    setImageRemoved(true);
    setImageError(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  const isNicknameEmpty = nickname.trim().length === 0;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (nicknameDupError || isNicknameEmpty) return;

    setServerError(null);
    setIsSubmitting(true);

    try {
      const formData = new FormData();
      formData.append("nickname", nickname.trim());

      if (selectedFile) {
        formData.append("profileImage", selectedFile);
      } else if (imageRemoved) {
        formData.append("removeProfileImage", "true");
      }

      const { data } = await axiosInstance.patch<UserResponse>(
        USER_API.ME,
        formData,
        { headers: { "Content-Type": "multipart/form-data" } },
      );

      setUser(data);
      navigate("/my-page");
    } catch (error: unknown) {
      if (isAxiosError(error) && error.response?.status === 409) {
        setServerError("이미 사용 중인 닉네임입니다.");
      } else {
        setServerError("프로필 수정 중 오류가 발생했습니다.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!user) return null;

  return (
    <div className="max-w-lg mx-auto">
      <h2 className="text-2xl font-bold mb-6 text-primary">프로필 수정</h2>

      <div className="bg-white rounded-2xl shadow-lg p-6 border border-green-100">
        {serverError && (
          <div className="mb-4 p-3 rounded-lg text-sm bg-bgDanger text-danger">
            {serverError}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* 프로필 이미지 */}
          <div className="flex flex-col items-center gap-2">
            <div className="relative w-28 h-28">
              <img
                src={previewUrl ?? DEFAULT_AVATAR}
                alt="프로필"
                className="w-28 h-28 rounded-full object-cover bg-gray-100 border-2 border-gray-200"
              />

              {/* 업로드 중 오버레이 */}
              {isSubmitting && (
                <div className="absolute inset-0 flex items-center justify-center rounded-full bg-black/40">
                  <div className="w-6 h-6 border-2 border-white border-t-transparent rounded-full animate-spin" />
                </div>
              )}

              {/* 하단 변경 오버레이 */}
              {!isSubmitting && (
                <button
                  type="button"
                  onClick={() => fileInputRef.current?.click()}
                  className="absolute bottom-0 left-0 right-0 flex items-center justify-center rounded-b-full text-white text-xs font-medium cursor-pointer"
                  style={{
                    height: "20%",
                    backgroundColor: "rgba(0, 0, 0, 0.5)",
                  }}
                >
                  변경
                </button>
              )}

              {/* 우측 상단 삭제 버튼 */}
              {!isSubmitting && previewUrl && (
                <button
                  type="button"
                  onClick={handleRemoveImage}
                  className="absolute -top-1 -right-1 w-6 h-6 rounded-full bg-gray-600 text-white flex items-center justify-center text-sm leading-none hover:bg-gray-800 transition-colors shadow"
                >
                  −
                </button>
              )}
            </div>

            <input
              ref={fileInputRef}
              type="file"
              accept="image/jpeg,image/png"
              onChange={handleImageSelect}
              className="hidden"
            />

            {imageError && <p className="text-xs text-danger">{imageError}</p>}
          </div>

          {/* 닉네임 */}
          <div>
            <label
              htmlFor="nickname"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              닉네임
            </label>
            <input
              id="nickname"
              type="text"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              onBlur={handleNicknameBlur}
              placeholder="닉네임 (2-50자)"
              className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 transition-colors"
            />
            {nicknameDupError && (
              <p className="mt-1 text-sm text-danger">{nicknameDupError}</p>
            )}
            {isNicknameEmpty && (
              <p className="mt-1 text-sm text-warning">
                닉네임을 입력해주세요.
              </p>
            )}
          </div>

          {/* 버튼 */}
          <div className="flex gap-3">
            <button
              type="submit"
              disabled={isSubmitting || !!nicknameDupError || isNicknameEmpty}
              className="flex-1 py-3 rounded-lg text-white font-medium transition-opacity disabled:opacity-50 bg-primary hover:bg-primaryHover"
            >
              {isSubmitting ? "수정 중..." : "수정 완료"}
            </button>
            <button
              type="button"
              onClick={() => navigate(-1)}
              className="flex-1 py-3 rounded-lg border border-gray-200 text-gray-600 font-medium hover:bg-gray-50 transition-colors"
            >
              취소
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EditProfilePage;
