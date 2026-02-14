import { type FC, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { authApi } from "../api/authApi";
import { AUTH_VALIDATION } from "../auth.constants";
import { isAxiosError } from "axios";

const signUpSchema = z
  .object({
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
      .regex(
        AUTH_VALIDATION.PASSWORD_PATTERN,
        "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.",
      ),
    nickname: z
      .string()
      .min(
        AUTH_VALIDATION.NICKNAME_MIN_LENGTH,
        `닉네임은 ${AUTH_VALIDATION.NICKNAME_MIN_LENGTH}자 이상이어야 합니다.`,
      )
      .max(
        AUTH_VALIDATION.NICKNAME_MAX_LENGTH,
        `닉네임은 ${AUTH_VALIDATION.NICKNAME_MAX_LENGTH}자 이하여야 합니다.`,
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
  const [serverError, setServerError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [emailDupError, setEmailDupError] = useState<string | null>(null);
  const [nicknameDupError, setNicknameDupError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    getValues,
    formState: { errors },
  } = useForm<SignUpFormValues>({
    resolver: zodResolver(signUpSchema),
    defaultValues: {
      email: "",
      password: "",
      nickname: "",
      passwordConfirm: "",
    },
  });

  const handleEmailBlur = async () => {
    const email = getValues("email");
    if (!email || !AUTH_VALIDATION.EMAIL_REGEX.test(email)) return;

    try {
      const { exists } = await authApi.checkEmail(email);
      setEmailDupError(exists ? "이미 사용 중인 이메일입니다." : null);
    } catch {
      // 중복 체크 실패 시 무시 (가입 시 서버에서 재검증)
    }
  };

  const handleNicknameBlur = async () => {
    const nickname = getValues("nickname");
    if (!nickname || nickname.length < AUTH_VALIDATION.NICKNAME_MIN_LENGTH)
      return;

    try {
      const { exists } = await authApi.checkNickname(nickname);
      setNicknameDupError(exists ? "이미 사용 중인 닉네임입니다." : null);
    } catch {
      // 중복 체크 실패 시 무시
    }
  };

  const onSubmit = async (values: SignUpFormValues) => {
    if (emailDupError || nicknameDupError) return;

    setServerError(null);
    setIsSubmitting(true);

    try {
      await authApi.signup({
        email: values.email,
        password: values.password,
        nickname: values.nickname,
      });
      navigate("/login", {
        state: { message: "회원가입이 완료되었습니다. 로그인해주세요." },
      });
    } catch (error: unknown) {
      if (isAxiosError(error) && error.response) {
        const status = error.response.status;
        if (status === 409) {
          setServerError("이미 사용 중인 이메일 또는 닉네임입니다.");
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
        <h2 className="text-2xl font-bold text-center mb-2 text-primary">
          회원가입
        </h2>
        <p className="text-sm text-gray-500 text-center mb-8">
          새로운 계정을 만들어보세요
        </p>

        {serverError && (
          <div className="mb-4 p-3 rounded-lg text-sm bg-white text-danger">
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
              tabIndex={-1}
              className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 transition-colors"
              {...register("email", { onBlur: handleEmailBlur })}
            />
            {errors.email && (
              <p className="mt-1 text-sm text-rose">{errors.email.message}</p>
            )}
            {emailDupError && (
              <p className="mt-1 text-sm text-danger">{emailDupError}</p>
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
              placeholder="영문, 숫자, 특수문자 포함 8자 이상"
              className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 transition-colors"
              {...register("password")}
            />
            {errors.password && (
              <p className="mt-1 text-sm text-rose">
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
              <p className="mt-1 text-sm text-rose">
                {errors.passwordConfirm.message}
              </p>
            )}
          </div>

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
              placeholder="닉네임 (2-50자)"
              className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 transition-colors"
              {...register("nickname", { onBlur: handleNicknameBlur })}
            />
            {errors.nickname && (
              <p className="mt-1 text-sm text-rose">
                {errors.nickname.message}
              </p>
            )}
            {nicknameDupError && (
              <p className="mt-1 text-sm text-danger">{nicknameDupError}</p>
            )}
          </div>

          <button
            type="submit"
            disabled={isSubmitting || !!emailDupError || !!nicknameDupError}
            className="w-full py-3 rounded-lg bg-primary text-white font-medium transition-opacity disabled:opacity-50"
          >
            {isSubmitting ? "가입 중..." : "회원가입"}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-gray-500">
          이미 계정이 있으신가요?{" "}
          <Link
            to="/login"
            className="font-medium text-primary hover:underline"
          >
            로그인
          </Link>
        </p>
      </div>
    </div>
  );
};

export default SignUpForm;
