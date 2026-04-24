import { type FC, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
  shake,
  buttonHover,
  buttonTap,
} from "../../../global/constants/animations";
import { useNavigate, Link } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { authApi } from "../api/authApi";
import { useAuthStore } from "../store/useAuthStore";
import { AUTH_VALIDATION } from "../auth.constants";
import { tokenStorage } from "../auth.utils";
import { isAxiosError } from "axios";

type SignupStep = "email" | "verifying" | "info";

const emailSchema = z.object({
  email: z
    .string()
    .min(1, "이메일을 입력해주세요.")
    .regex(AUTH_VALIDATION.EMAIL_REGEX, "올바른 이메일 형식이 아닙니다."),
});

const infoSchema = z
  .object({
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

type EmailFormValues = z.infer<typeof emailSchema>;
type InfoFormValues = z.infer<typeof infoSchema>;

const SignUpForm: FC = () => {
  const navigate = useNavigate();
  const setAuth = useAuthStore((state) => state.setAuth);

  const [step, setStep] = useState<SignupStep>("email");
  const [verifiedEmail, setVerifiedEmail] = useState("");
  const [serverError, setServerError] = useState<string | null>(null);
  const [resendMessage, setResendMessage] = useState<string | null>(null);
  const [isSending, setIsSending] = useState(false);
  const [isChecking, setIsChecking] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [nicknameDupError, setNicknameDupError] = useState<string | null>(null);

  const emailForm = useForm<EmailFormValues>({
    resolver: zodResolver(emailSchema),
    defaultValues: { email: "" },
  });

  const infoForm = useForm<InfoFormValues>({
    resolver: zodResolver(infoSchema),
    defaultValues: { password: "", nickname: "", passwordConfirm: "" },
  });

  const handleSendVerification = async (values: EmailFormValues) => {
    setServerError(null);
    setIsSending(true);
    try {
      await authApi.sendSignupVerification(values.email);
      setVerifiedEmail(values.email);
      setResendMessage(null);
      setStep("verifying");
    } catch (error: unknown) {
      if (isAxiosError(error) && error.response) {
        const status = error.response.status;
        if (status === 409) {
          emailForm.setError("email", { message: "이미 사용 중인 이메일입니다." });
        } else {
          setServerError("인증 메일 발송에 실패했습니다. 다시 시도해주세요.");
        }
      } else {
        setServerError("서버에 연결할 수 없습니다.");
      }
    } finally {
      setIsSending(false);
    }
  };

  const handleResend = async () => {
    setServerError(null);
    setResendMessage(null);
    setIsSending(true);
    try {
      await authApi.sendSignupVerification(verifiedEmail);
      setResendMessage("인증 메일을 재발송했습니다. 이메일을 확인해주세요.");
    } catch {
      setServerError("이메일 재발송에 실패했습니다. 다시 시도해주세요.");
    } finally {
      setIsSending(false);
    }
  };

  const handleCheckVerification = async () => {
    setServerError(null);
    setResendMessage(null);
    setIsChecking(true);
    try {
      const { isVerified } = await authApi.getVerificationStatus(verifiedEmail);
      if (isVerified) {
        setStep("info");
      } else {
        setServerError(
          "아직 이메일 인증이 완료되지 않았습니다. 이메일에서 인증 링크를 클릭해주세요.",
        );
      }
    } catch {
      setServerError("인증 상태 확인에 실패했습니다. 다시 시도해주세요.");
    } finally {
      setIsChecking(false);
    }
  };

  const handleNicknameBlur = async () => {
    const nickname = infoForm.getValues("nickname");
    if (!nickname || nickname.length < AUTH_VALIDATION.NICKNAME_MIN_LENGTH)
      return;
    try {
      const { exists } = await authApi.checkNickname(nickname);
      setNicknameDupError(exists ? "이미 사용 중인 닉네임입니다." : null);
    } catch {
      // 중복 체크 실패 시 무시 (가입 시 서버에서 재검증)
    }
  };

  const onSubmit = async (values: InfoFormValues) => {
    if (nicknameDupError) return;
    setServerError(null);
    setIsSubmitting(true);
    try {
      const signupResponse = await authApi.signup({
        email: verifiedEmail,
        password: values.password,
        nickname: values.nickname,
      });
      tokenStorage.setTokens(
        signupResponse.accessToken,
        signupResponse.refreshToken,
      );
      const user = await authApi.getMe();
      setAuth(signupResponse, user);
      navigate("/");
    } catch (error: unknown) {
      if (isAxiosError(error) && error.response) {
        const status = error.response.status;
        if (status === 409) {
          setServerError("이미 사용 중인 이메일 또는 닉네임입니다.");
        } else if (status === 403) {
          setServerError(
            "이메일 인증이 만료되었습니다. 처음부터 다시 시도해주세요.",
          );
          setStep("email");
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

  const Spinner = () => (
    <motion.div
      animate={{ rotate: 360 }}
      transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
      className="w-4 h-4 border-2 border-white border-t-transparent rounded-full"
    />
  );

  return (
    <div className="w-full max-w-md mx-auto">
      <div className="bg-white rounded-2xl shadow-lg p-8 border border-green-100">
        <h2 className="text-2xl font-bold text-center mb-2 text-primary">
          회원가입
        </h2>
        <p className="text-sm text-gray-500 text-center mb-8">
          {step === "email" && "이메일을 입력하여 인증을 시작해주세요"}
          {step === "verifying" && "이메일 인증을 완료해주세요"}
          {step === "info" && "나머지 정보를 입력해주세요"}
        </p>

        <AnimatePresence>
          {serverError && (
            <motion.div
              animate={shake}
              className="mb-4 p-3 rounded-lg text-sm bg-bgError text-error"
            >
              {serverError}
            </motion.div>
          )}
        </AnimatePresence>

        {/* Step 1: 이메일 입력 */}
        {step === "email" && (
          <form
            onSubmit={emailForm.handleSubmit(handleSendVerification)}
            className="space-y-5"
          >
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
                {...emailForm.register("email")}
              />
              {emailForm.formState.errors.email && (
                <p className="mt-1 text-sm text-rose">
                  {emailForm.formState.errors.email.message}
                </p>
              )}
            </div>

            <motion.button
              type="submit"
              disabled={isSending}
              whileHover={!isSending ? buttonHover : {}}
              whileTap={!isSending ? buttonTap : {}}
              className="w-full py-3 rounded-lg text-white font-medium bg-primary hover:bg-primaryHover transition-colors disabled:opacity-50 flex items-center justify-center gap-2"
            >
              {isSending && <Spinner />}
              {isSending ? "발송 중..." : "인증 메일 발송"}
            </motion.button>
          </form>
        )}

        {/* Step 2: 이메일 인증 대기 */}
        {step === "verifying" && (
          <div className="space-y-5">
            <div className="p-4 bg-green-50 rounded-lg text-sm text-gray-700">
              <p className="font-medium mb-1">
                <span className="text-primary">{verifiedEmail}</span>
                로 인증 메일을 발송했습니다.
              </p>
              <p className="text-gray-500">
                이메일에서 인증 링크를 클릭한 후 아래 버튼을 눌러주세요.
                인증 링크는 30분간 유효합니다.
              </p>
            </div>

            {resendMessage && (
              <p className="text-sm text-primary text-center">{resendMessage}</p>
            )}

            <motion.button
              type="button"
              disabled={isChecking}
              onClick={handleCheckVerification}
              whileHover={!isChecking ? buttonHover : {}}
              whileTap={!isChecking ? buttonTap : {}}
              className="w-full py-3 rounded-lg text-white font-medium bg-primary hover:bg-primaryHover transition-colors disabled:opacity-50 flex items-center justify-center gap-2"
            >
              {isChecking && <Spinner />}
              {isChecking ? "확인 중..." : "인증 완료 → 계속"}
            </motion.button>

            <div className="flex items-center justify-between text-sm">
              <button
                type="button"
                disabled={isSending}
                onClick={handleResend}
                className="text-gray-500 hover:text-primary transition-colors disabled:opacity-50"
              >
                {isSending ? "발송 중..." : "메일 재발송"}
              </button>
              <button
                type="button"
                onClick={() => {
                  setStep("email");
                  setServerError(null);
                }}
                className="text-gray-500 hover:text-primary transition-colors"
              >
                이메일 변경
              </button>
            </div>
          </div>
        )}

        {/* Step 3: 나머지 정보 입력 */}
        {step === "info" && (
          <form
            onSubmit={infoForm.handleSubmit(onSubmit)}
            className="space-y-5"
          >
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                이메일
              </label>
              <div className="w-full px-4 py-3 rounded-lg border border-gray-100 bg-gray-50 text-gray-500 text-sm">
                {verifiedEmail}
                <span className="ml-2 text-xs text-primary font-medium">
                  ✓ 인증됨
                </span>
              </div>
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
                className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary transition-colors"
                {...infoForm.register("password")}
              />
              {infoForm.formState.errors.password && (
                <p className="mt-1 text-sm text-rose">
                  {infoForm.formState.errors.password.message}
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
                className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary transition-colors"
                {...infoForm.register("passwordConfirm")}
              />
              {infoForm.formState.errors.passwordConfirm && (
                <p className="mt-1 text-sm text-rose">
                  {infoForm.formState.errors.passwordConfirm.message}
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
                className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary transition-colors"
                {...infoForm.register("nickname", {
                  onBlur: handleNicknameBlur,
                })}
              />
              {infoForm.formState.errors.nickname && (
                <p className="mt-1 text-sm text-rose">
                  {infoForm.formState.errors.nickname.message}
                </p>
              )}
              {nicknameDupError && (
                <p className="mt-1 text-sm text-error">{nicknameDupError}</p>
              )}
            </div>

            <motion.button
              type="submit"
              disabled={isSubmitting || !!nicknameDupError}
              whileHover={!isSubmitting && !nicknameDupError ? buttonHover : {}}
              whileTap={!isSubmitting && !nicknameDupError ? buttonTap : {}}
              className="w-full py-3 rounded-lg text-white font-medium bg-primary hover:bg-primaryHover transition-colors disabled:opacity-50 flex items-center justify-center gap-2"
            >
              {isSubmitting && <Spinner />}
              {isSubmitting ? "가입 중..." : "회원가입"}
            </motion.button>
          </form>
        )}

        <p className="mt-6 text-center text-sm text-gray-500">
          이미 계정이 있으신가요?{" "}
          <Link
            to="/login"
            className="font-medium hover:underline text-primary"
          >
            로그인
          </Link>
        </p>
      </div>
    </div>
  );
};

export default SignUpForm;
