import { type FC, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { authApi } from "../api/authApi";
import { useAuthStore } from "../store/useAuthStore";
import { AUTH_VALIDATION } from "../auth.constants";
import type { LoginRequest } from "../types";
import { isAxiosError } from "axios";

const loginSchema = z.object({
  email: z
    .string()
    .min(1, "이메일을 입력해주세요.")
    .regex(AUTH_VALIDATION.EMAIL_REGEX, "올바른 이메일 형식이 아닙니다."),
  password: z
    .string()
    .min(
      AUTH_VALIDATION.PASSWORD_MIN_LENGTH,
      `비밀번호는 ${AUTH_VALIDATION.PASSWORD_MIN_LENGTH}자 이상이어야 합니다.`,
    )
    .max(
      AUTH_VALIDATION.PASSWORD_MAX_LENGTH,
      `비밀번호는 ${AUTH_VALIDATION.PASSWORD_MAX_LENGTH}자 이하여야 합니다.`,
    ),
});

type LoginFormValues = z.infer<typeof loginSchema>;

const LoginForm: FC = () => {
  const navigate = useNavigate();
  const setAuth = useAuthStore((state) => state.setAuth);
  const [serverError, setServerError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: "",
      password: "",
    },
  });

  const onSubmit = async (values: LoginFormValues) => {
    setServerError(null);
    setIsSubmitting(true);

    try {
      const response = await authApi.login(values);
      setAuth(response);
      navigate("/");
    } catch (error: unknown) {
      if (isAxiosError(error) && error.response) {
        const status = error.response.status;
        if (status === 401) {
          setServerError("이메일 또는 비밀번호가 올바르지 않습니다.");
        } else {
          setServerError("로그인 중 오류가 발생했습니다. 다시 시도해주세요.");
        }
      } else {
        setServerError("서버에 연결할 수 없습니다.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="w-full max-w-md mx-auto">
      <div className="bg-white rounded-2xl shadow-lg p-8 border border-green-100">
        <h2
          className="text-2xl font-bold text-center mb-2"
          style={{ color: "#88AF64" }}
        >
          로그인
        </h2>
        <p className="text-sm text-gray-500 text-center mb-8">
          소중한 순간을 함께 나눠보세요
        </p>

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
              htmlFor="email"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              이메일
            </label>
            <input
              id="email"
              type="email"
              placeholder="example@email.com"
              className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 focus:ring-[#88AF64]  transition-colors"
              {...register("email")}
            />
            {errors.email && (
              <p className="mt-1 text-sm" style={{ color: "#E6A5A5" }}>
                {errors.email.message}
              </p>
            )}
          </div>

          <div>
            <label
              htmlFor="password"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              비밀번호
            </label>
            <input
              id="password"
              type="password"
              placeholder="비밀번호를 입력해주세요"
              className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 transition-colors"
              {...register("password")}
            />
            {errors.password && (
              <p className="mt-1 text-sm" style={{ color: "#E6A5A5" }}>
                {errors.password.message}
              </p>
            )}
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full py-3 rounded-lg text-white font-medium transition-opacity disabled:opacity-50"
            style={{ backgroundColor: "#88AF64" }}
          >
            {isSubmitting ? "로그인 중..." : "로그인"}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-gray-500">
          아직 계정이 없으신가요?{" "}
          <Link
            to="/signup"
            className="font-medium hover:underline"
            style={{ color: "#88AF64" }}
          >
            회원가입
          </Link>
        </p>
      </div>
    </div>
  );
};

export default LoginForm;
