import { type FC, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { authApi } from "../api/authApi";
import { useAuthStore } from "../store/useAuthStore";
import { AUTH_VALIDATION } from "../auth.constants";
import { isAxiosError } from "axios";

const signUpSchema = z
  .object({
    name: z
      .string()
      .min(
        AUTH_VALIDATION.NAME_MIN_LENGTH,
        `이름은 ${AUTH_VALIDATION.NAME_MIN_LENGTH}자 이상이어야 합니다.`,
      )
      .max(
        AUTH_VALIDATION.NAME_MAX_LENGTH,
        `이름은 ${AUTH_VALIDATION.NAME_MAX_LENGTH}자 이하여야 합니다.`,
      ),
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
    passwordConfirm: z.string().min(1, "비밀번호 확인을 입력해주세요."),
  })
  .refine((data) => data.password === data.passwordConfirm, {
    message: "비밀번호가 일치하지 않습니다.",
    path: ["passwordConfirm"],
  });

type SignUpFormValues = z.infer<typeof signUpSchema>;

const SignUpForm: FC = () => {
  const navigate = useNavigate();
  const setAuth = useAuthStore((state) => state.setAuth);
  const [serverError, setServerError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<SignUpFormValues>({
    resolver: zodResolver(signUpSchema),
    defaultValues: {
      name: "",
      email: "",
      password: "",
      passwordConfirm: "",
    },
  });

  const onSubmit = async (values: SignUpFormValues) => {
    setServerError(null);
    setIsSubmitting(true);

    try {
      const response = await authApi.signUp({
        name: values.name,
        email: values.email,
        password: values.password,
      });
      setAuth(response);
      navigate("/");
    } catch (error: unknown) {
      if (isAxiosError(error) && error.response) {
        const status = error.response.status;
        if (status === 409) {
          setServerError("이미 사용 중인 이메일입니다.");
        } else if (status === 400) {
          setServerError("입력 정보를 확인해주세요.");
        } else {
          setServerError("회원가입 중 오류가 발생했습니다. 다시 시도해주세요.");
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
          회원가입
        </h2>
        <p className="text-sm text-gray-500 text-center mb-8">
          새로운 계정을 만들어보세요
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
              htmlFor="name"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              이름
            </label>
            <input
              id="name"
              type="text"
              placeholder="이름을 입력해주세요"
              className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 transition-colors"
              {...register("name")}
            />
            {errors.name && (
              <p className="mt-1 text-sm" style={{ color: "#E6A5A5" }}>
                {errors.name.message}
              </p>
            )}
          </div>

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
              className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 transition-colors"
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
              placeholder="8자 이상 입력해주세요"
              className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 transition-colors"
              {...register("password")}
            />
            {errors.password && (
              <p className="mt-1 text-sm" style={{ color: "#E6A5A5" }}>
                {errors.password.message}
              </p>
            )}
          </div>

          <div>
            <label
              htmlFor="passwordConfirm"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              비밀번호 확인
            </label>
            <input
              id="passwordConfirm"
              type="password"
              placeholder="비밀번호를 다시 입력해주세요"
              className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 transition-colors"
              {...register("passwordConfirm")}
            />
            {errors.passwordConfirm && (
              <p className="mt-1 text-sm" style={{ color: "#E6A5A5" }}>
                {errors.passwordConfirm.message}
              </p>
            )}
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full py-3 rounded-lg text-white font-medium transition-opacity disabled:opacity-50"
            style={{ backgroundColor: "#88AF64" }}
          >
            {isSubmitting ? "가입 중..." : "회원가입"}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-gray-500">
          이미 계정이 있으신가요?{" "}
          <Link
            to="/login"
            className="font-medium hover:underline"
            style={{ color: "#88AF64" }}
          >
            로그인
          </Link>
        </p>
      </div>
    </div>
  );
};

export default SignUpForm;
