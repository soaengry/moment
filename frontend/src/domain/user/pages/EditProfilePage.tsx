import { type FC, useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { IoArrowBack } from "react-icons/io5";
import { useAuthStore } from "../../auth/store/useAuthStore";
import { authApi } from "../../auth/api/authApi";
import { AUTH_VALIDATION } from "../../auth/auth.constants";
import { isAxiosError } from "axios";
import axiosInstance from "../../../global/api/axiosInstance";
import { USER_API } from "../../auth/auth.constants";
import type { UserResponse } from "../../auth/types";
import { useScrollVisibility } from "../../../global/hooks/useScrollVisibility";

const MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
const ALLOWED_TYPES = ["image/jpeg", "image/png"];
const DEFAULT_AVATAR = "/default-avatar.png";

const changePasswordSchema = z
  .object({
    currentPassword: z.string().min(1, "현재 비밀번호를 입력해주세요."),
    newPassword: z
      .string()
      .min(
        AUTH_VALIDATION.PASSWORD_MIN_LENGTH,
        `비밀번호는 ${AUTH_VALIDATION.PASSWORD_MIN_LENGTH}자 이상이어야 합니다.`,
      )
      .regex(
        AUTH_VALIDATION.PASSWORD_PATTERN,
        "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.",
      ),
    newPasswordConfirm: z.string().min(1, "새 비밀번호 확인을 입력해주세요."),
  })
  .refine((data) => data.newPassword === data.newPasswordConfirm, {
    message: "새 비밀번호가 일치하지 않습니다.",
    path: ["newPasswordConfirm"],
  });

type ChangePasswordFormValues = z.infer<typeof changePasswordSchema>;

const EditProfilePage: FC = () => {
  const navigate = useNavigate();
  const user = useAuthStore((state) => state.user);
  const setUser = useAuthStore((state) => state.setUser);
  const logout = useAuthStore((state) => state.logout);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Profile state
  const [nickname, setNickname] = useState("");
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [imageRemoved, setImageRemoved] = useState(false);
  const [nicknameDupError, setNicknameDupError] = useState<string | null>(null);
  const [imageError, setImageError] = useState<string | null>(null);
  const [serverError, setServerError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Password state
  const [pwServerError, setPwServerError] = useState<string | null>(null);
  const [isPwSubmitting, setIsPwSubmitting] = useState(false);

  const {
    register,
    handleSubmit: handlePwSubmit,
    formState: { errors: pwErrors },
  } = useForm<ChangePasswordFormValues>({
    resolver: zodResolver(changePasswordSchema),
    defaultValues: {
      currentPassword: "",
      newPassword: "",
      newPasswordConfirm: "",
    },
  });

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

  const handleProfileSubmit = async (e: React.FormEvent) => {
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

  const onPasswordSubmit = async (values: ChangePasswordFormValues) => {
    setPwServerError(null);
    setIsPwSubmitting(true);

    try {
      await authApi.changePassword({
        currentPassword: values.currentPassword,
        newPassword: values.newPassword,
      });
      logout();
      navigate("/login", {
        state: { message: "비밀번호가 변경되었습니다. 다시 로그인해주세요." },
      });
    } catch (error: unknown) {
      if (isAxiosError(error) && error.response?.status === 401) {
        setPwServerError("현재 비밀번호가 일치하지 않습니다.");
      } else {
        setPwServerError("비밀번호 변경 중 오류가 발생했습니다.");
      }
    } finally {
      setIsPwSubmitting(false);
    }
  };

  const headerVisible = useScrollVisibility();

  if (!user) return null;

  return (
    <div className="min-h-screen bg-[#faf9f6]">
      <div className="max-w-lg mx-auto">
        <header className={`sticky top-0 z-20 bg-white/80 backdrop-blur-md border-b border-gray-50 transition-transform duration-300 ${headerVisible ? "translate-y-0" : "-translate-y-full"}`}>
          <div className="flex items-center gap-3 px-4 py-3">
            <button onClick={() => navigate(-1)} className="text-gray-600">
              <IoArrowBack size={22} />
            </button>
            <h1 className="text-base font-semibold text-gray-800">프로필 수정</h1>
          </div>
        </header>

      <div className="px-4 py-4 space-y-4">
      {/* 프로필 수정 섹션 */}
      <div className="bg-white rounded-2xl shadow-sm p-6 border border-gray-100">
        {serverError && (
          <div className="mb-4 p-3 rounded-lg text-sm bg-bgError text-error">
            {serverError}
          </div>
        )}

        <form onSubmit={handleProfileSubmit} className="space-y-6">
          {/* 프로필 이미지 */}
          <div className="flex flex-col items-center gap-2">
            <div className="relative w-28 h-28">
              <img
                src={previewUrl ?? DEFAULT_AVATAR}
                alt="프로필"
                className="w-28 h-28 rounded-full object-cover bg-gray-100 border-2 border-gray-200"
              />

              {isSubmitting && (
                <div className="absolute inset-0 flex items-center justify-center rounded-full bg-black/40">
                  <div className="w-6 h-6 border-2 border-white border-t-transparent rounded-full animate-spin" />
                </div>
              )}

              {!isSubmitting && (
                <button
                  type="button"
                  onClick={() => fileInputRef.current?.click()}
                  className="absolute bottom-0 left-0 right-0 h-[20%] flex items-center justify-center rounded-b-full bg-black/50 text-white text-xs font-medium cursor-pointer"
                >
                  변경
                </button>
              )}

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

            {imageError && <p className="text-xs text-error">{imageError}</p>}
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
              <p className="mt-1 text-sm text-error">{nicknameDupError}</p>
            )}
            {isNicknameEmpty && (
              <p className="mt-1 text-sm text-rose">닉네임을 입력해주세요.</p>
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

      {/* 비밀번호 변경 섹션 — LOCAL 사용자만 */}
      {user.authProvider === "LOCAL" && (
        <div className="bg-white rounded-2xl shadow-sm p-6 border border-gray-100">
          <h3 className="text-lg font-semibold text-gray-800 mb-4">비밀번호 변경</h3>

          <div className="mb-4 p-3 rounded-lg text-sm bg-yellow-50 text-yellow-700">
            비밀번호 변경 시 모든 기기에서 로그아웃됩니다.
          </div>

          {pwServerError && (
            <div className="mb-4 p-3 rounded-lg text-sm bg-bgError text-error">
              {pwServerError}
            </div>
          )}

          <form onSubmit={handlePwSubmit(onPasswordSubmit)} className="space-y-4">
            <div>
              <label
                htmlFor="currentPassword"
                className="block text-sm font-medium text-gray-700 mb-1"
              >
                현재 비밀번호
              </label>
              <input
                id="currentPassword"
                type="password"
                placeholder="현재 비밀번호"
                className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 transition-colors"
                {...register("currentPassword")}
              />
              {pwErrors.currentPassword && (
                <p className="mt-1 text-sm text-rose">
                  {pwErrors.currentPassword.message}
                </p>
              )}
            </div>

            <div>
              <label
                htmlFor="newPassword"
                className="block text-sm font-medium text-gray-700 mb-1"
              >
                새 비밀번호
              </label>
              <input
                id="newPassword"
                type="password"
                placeholder="영문, 숫자, 특수문자 포함 8자 이상"
                className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 transition-colors"
                {...register("newPassword")}
              />
              {pwErrors.newPassword && (
                <p className="mt-1 text-sm text-rose">
                  {pwErrors.newPassword.message}
                </p>
              )}
            </div>

            <div>
              <label
                htmlFor="newPasswordConfirm"
                className="block text-sm font-medium text-gray-700 mb-1"
              >
                새 비밀번호 확인
              </label>
              <input
                id="newPasswordConfirm"
                type="password"
                placeholder="새 비밀번호를 다시 입력해주세요"
                className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 transition-colors"
                {...register("newPasswordConfirm")}
              />
              {pwErrors.newPasswordConfirm && (
                <p className="mt-1 text-sm text-rose">
                  {pwErrors.newPasswordConfirm.message}
                </p>
              )}
            </div>

            <button
              type="submit"
              disabled={isPwSubmitting}
              className="w-full py-3 rounded-lg text-white font-medium transition-opacity disabled:opacity-50 bg-primary hover:bg-primaryHover"
            >
              {isPwSubmitting ? "변경 중..." : "비밀번호 변경"}
            </button>
          </form>
        </div>
      )}

      {/* 회원 탈퇴 */}
      <div className="text-center">
        <button
          onClick={() => navigate("/delete-account")}
          className="text-sm text-gray-400 hover:text-red-400 transition-colors"
        >
          회원 탈퇴
        </button>
      </div>
      </div>

        <div className="h-20" />
      </div>
    </div>
  );
};

export default EditProfilePage;
