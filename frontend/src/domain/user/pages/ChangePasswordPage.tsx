import { type FC, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { authApi } from "../../auth/api/authApi";
import { useAuthStore } from "../../auth/store/useAuthStore";
import { AUTH_VALIDATION } from "../../auth/auth.constants";
import { isAxiosError } from "axios";

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

const ChangePasswordPage: FC = () => {
  const navigate = useNavigate();
  const logout = useAuthStore((state) => state.logout);
  const [serverError, setServerError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ChangePasswordFormValues>({
    resolver: zodResolver(changePasswordSchema),
    defaultValues: {
      currentPassword: "",
      newPassword: "",
      newPasswordConfirm: "",
    },
  });

  const onSubmit = async (values: ChangePasswordFormValues) => {
    setServerError(null);
    setIsSubmitting(true);

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
        setServerError("현재 비밀번호가 일치하지 않습니다.");
      } else {
        setServerError("비밀번호 변경 중 오류가 발생했습니다.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="max-w-lg mx-auto">
      <h2 className="text-2xl font-bold mb-6" style={{ color: "#88AF64" }}>
        비밀번호 변경
      </h2>

      <div className="bg-white rounded-2xl shadow-lg p-6 border border-green-100">
        <div className="mb-4 p-3 rounded-lg text-sm bg-yellow-50 text-yellow-700">
          ⚠️ 비밀번호 변경 시 모든 기기에서 로그아웃됩니다.
        </div>

        {serverError && (
          <div
            className="mb-4 p-3 rounded-lg text-sm"
            style={{ backgroundColor: "#FEF2F2", color: "#DC2626" }}
          >
            {serverError}
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
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
            {errors.currentPassword && (
              <p className="mt-1 text-sm" style={{ color: "#E6A5A5" }}>
                {errors.currentPassword.message}
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
            {errors.newPassword && (
              <p className="mt-1 text-sm" style={{ color: "#E6A5A5" }}>
                {errors.newPassword.message}
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
            {errors.newPasswordConfirm && (
              <p className="mt-1 text-sm" style={{ color: "#E6A5A5" }}>
                {errors.newPasswordConfirm.message}
              </p>
            )}
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full py-3 rounded-lg text-white font-medium transition-opacity disabled:opacity-50"
            style={{ backgroundColor: "#88AF64" }}
          >
            {isSubmitting ? "변경 중..." : "비밀번호 변경"}
          </button>
        </form>
      </div>
    </div>
  );
};

export default ChangePasswordPage;
