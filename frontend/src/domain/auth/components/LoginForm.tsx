import { type FC, useState } from "react";
import { useNavigate, Link, useLocation } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { authApi } from "../api/authApi";
import { useAuthStore } from "../store/useAuthStore";
import { AUTH_VALIDATION } from "../auth.constants";
import { tokenStorage } from "../auth.utils";
import { isAxiosError } from "axios";
import SocialLoginButtons from "./SocialLoginButtons";

const loginSchema = z.object({
  email: z
    .string()
    .min(1, "이메일을 입력해주세요.")
    .regex(AUTH_VALIDATION.EMAIL_REGEX, "올바른 이메일 형식이 아닙니다."),
  password: z.string().min(1, "비밀번호를 입력해주세요."),
});

type LoginFormValues = z.infer<typeof loginSchema>;

const LoginForm: FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const setAuth = useAuthStore((state) => state.setAuth);
  const [serverError, setServerError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const stateMessage =
    (location.state as { message?: string })?.message ?? null;

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
      const tokenResponse = await authApi.login(values);
      tokenStorage.setTokens(
        tokenResponse.accessToken,
        tokenResponse.refreshToken,
      );
      const user = await authApi.getMe();
      setAuth(tokenResponse, user);
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
      <div className="text-center mb-8">
        <Link to="/" className="text-3xl font-bold text-primary tracking-widest select-none">
          MOMENT
        </Link>
      </div>
      <div className="bg-white rounded-2xl shadow-lg p-8 border border-green-100">
        <h2 className="text-2xl font-bold text-center mb-2 text-primary">
          로그인
        </h2>
        <p className="text-sm text-gray-500 text-center mb-8">
          소중한 순간을 함께 나눠보세요
        </p>

        {stateMessage && (
          <div className="mb-4 p-3 rounded-lg text-sm bg-bgSuccess text-success">
            {stateMessage}
          </div>
        )}

        {serverError && (
          <div className="mb-4 p-3 rounded-lg text-sm bg-bgError text-error">
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
              className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary transition-colors"
              {...register("email")}
            />
            {errors.email && (
              <p className="mt-1 text-sm text-rose">{errors.email.message}</p>
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
              className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary transition-colors"
              {...register("password")}
            />
            {errors.password && (
              <p className="mt-1 text-sm text-rose">
                {errors.password.message}
              </p>
            )}
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full py-3 rounded-lg text-white font-medium bg-primary hover:bg-primaryHover transition-colors disabled:opacity-50"
          >
            {isSubmitting ? "로그인 중..." : "로그인"}
          </button>
        </form>

        {/* 구분선 */}
        <div className="relative my-6">
          <div className="absolute inset-0 flex items-center">
            <div className="w-full border-t border-gray-200" />
          </div>
          <div className="relative flex justify-center text-sm">
            <span className="px-4 bg-white text-gray-400">또는</span>
          </div>
        </div>

        {/* 소셜 로그인 */}
        <SocialLoginButtons />

        <div className="mt-6 flex items-center justify-between text-sm text-gray-500">
          <Link
            to="/signup"
            className="font-medium hover:underline text-primary"
          >
            회원가입
          </Link>
          <div className="flex items-center gap-4">
            <Link to="/forgot-password" className="hover:underline text-gray-400">
              비밀번호 찾기
            </Link>
            <Link to="/restore-account" className="hover:underline text-gray-400">
              계정 복구
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginForm;
