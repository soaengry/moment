import { type FC, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { shake, buttonHover, buttonTap } from "../../../global/constants/animations";
import { useNavigate, Link } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { authApi } from "../api/authApi";
import { useAuthStore } from "../store/useAuthStore";
import { AUTH_VALIDATION } from "../auth.constants";
import { tokenStorage } from "../auth.utils";
import { isAxiosError } from "axios";
import { IoMailOutline, IoCheckmarkCircleOutline, IoRefreshOutline } from "react-icons/io5";

// ─── Schemas ───

const emailSchema = z.object({
  email: z
    .string()
    .min(1, "이메일을 입력해주세요.")
    .regex(AUTH_VALIDATION.EMAIL_REGEX, "올바른 이메일 형식이 아닙니다."),
});

const profileSchema = z
  .object({
    password: z
      .string()
      .min(AUTH_VALIDATION.PASSWORD_MIN_LENGTH, `비밀번호는 ${AUTH_VALIDATION.PASSWORD_MIN_LENGTH}자 이상이어야 합니다.`)
      .regex(AUTH_VALIDATION.PASSWORD_PATTERN, "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다."),
    passwordConfirm: z.string().min(1, "비밀번호 확인을 입력해주세요."),
    nickname: z
      .string()
      .min(AUTH_VALIDATION.NICKNAME_MIN_LENGTH, `닉네임은 ${AUTH_VALIDATION.NICKNAME_MIN_LENGTH}자 이상이어야 합니다.`)
      .max(AUTH_VALIDATION.NICKNAME_MAX_LENGTH, `닉네임은 ${AUTH_VALIDATION.NICKNAME_MAX_LENGTH}자 이하여야 합니다.`),
  })
  .refine((d) => d.password === d.passwordConfirm, {
    message: "비밀번호가 일치하지 않습니다.",
    path: ["passwordConfirm"],
  });

type EmailFormValues = z.infer<typeof emailSchema>;
type ProfileFormValues = z.infer<typeof profileSchema>;
type Stage = "email" | "waiting" | "profile";

// ─── Stage 1: 이메일 입력 ───

interface EmailStageProps {
  onVerificationSent: (email: string) => void;
}

const EmailStage: FC<EmailStageProps> = ({ onVerificationSent }) => {
  const [isSending, setIsSending] = useState(false);
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    getValues,
    formState: { errors },
  } = useForm<EmailFormValues>({
    resolver: zodResolver(emailSchema),
    defaultValues: { email: "" },
  });

  const onSubmit = async (values: EmailFormValues) => {
    setServerError(null);
    setIsSending(true);
    try {
      await authApi.sendSignupVerification(values.email);
      onVerificationSent(values.email);
    } catch (err) {
      if (isAxiosError(err) && err.response?.status === 409) {
        setServerError("이미 사용 중인 이메일입니다.");
      } else {
        setServerError("인증 메일 발송에 실패했습니다. 다시 시도해주세요.");
      }
    } finally {
      setIsSending(false);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
      <AnimatePresence>
        {serverError && (
          <motion.div animate={shake} className="p-3 rounded-lg text-sm bg-bgError text-error">
            {serverError}
          </motion.div>
        )}
      </AnimatePresence>

      <div>
        <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
          이메일
        </label>
        <input
          id="email"
          type="email"
          placeholder="example@email.com"
          autoComplete="email"
          className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary transition-colors"
          {...register("email")}
          onBlur={async () => {
            const email = getValues("email");
            if (!email || !AUTH_VALIDATION.EMAIL_REGEX.test(email)) return;
            try {
              const { exists } = await authApi.checkEmail(email);
              if (exists) setServerError("이미 사용 중인 이메일입니다.");
              else if (serverError === "이미 사용 중인 이메일입니다.") setServerError(null);
            } catch { /* ignore */ }
          }}
        />
        {errors.email && (
          <p className="mt-1 text-sm text-rose">{errors.email.message}</p>
        )}
      </div>

      <motion.button
        type="submit"
        disabled={isSending}
        whileHover={!isSending ? buttonHover : {}}
        whileTap={!isSending ? buttonTap : {}}
        className="w-full py-3 rounded-lg text-white font-medium bg-primary hover:bg-primaryHover transition-colors disabled:opacity-50 flex items-center justify-center gap-2"
      >
        {isSending ? (
          <motion.div
            animate={{ rotate: 360 }}
            transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
            className="w-4 h-4 border-2 border-white border-t-transparent rounded-full"
          />
        ) : (
          <IoMailOutline className="text-lg" />
        )}
        {isSending ? "발송 중..." : "인증 메일 발송"}
      </motion.button>
    </form>
  );
};

// ─── Stage 2: 인증 대기 ───

interface WaitingStageProps {
  email: string;
  onVerified: () => void;
}

const WaitingStage: FC<WaitingStageProps> = ({ email, onVerified }) => {
  const [isChecking, setIsChecking] = useState(false);
  const [isResending, setIsResending] = useState(false);
  const [resendMsg, setResendMsg] = useState<string | null>(null);
  const [showPopup, setShowPopup] = useState(false);

  const handleVerifiedClick = async () => {
    setIsChecking(true);
    try {
      const { isVerified } = await authApi.getVerificationStatus(email);
      if (isVerified) {
        onVerified();
      } else {
        setShowPopup(true);
      }
    } catch {
      setShowPopup(true);
    } finally {
      setIsChecking(false);
    }
  };

  const handleResend = async () => {
    setIsResending(true);
    setResendMsg(null);
    try {
      await authApi.sendSignupVerification(email);
      setResendMsg("인증 메일을 재발송했습니다.");
    } catch { /* ignore */ } finally {
      setIsResending(false);
    }
  };

  return (
    <div className="space-y-5">
      <div className="bg-green-50 border border-green-100 rounded-xl p-4 space-y-1">
        <div className="flex items-center gap-2 text-primary font-medium text-sm">
          <IoMailOutline className="text-base flex-shrink-0" />
          인증 메일을 발송했습니다
        </div>
        <p className="text-xs text-gray-500 pl-5">
          <span className="font-medium text-gray-700">{email}</span>로 발송된 메일의 링크를 클릭해 인증을 완료해주세요.
        </p>
      </div>

      <motion.button
        type="button"
        onClick={handleVerifiedClick}
        disabled={isChecking}
        whileHover={!isChecking ? buttonHover : {}}
        whileTap={!isChecking ? buttonTap : {}}
        className="w-full py-3 rounded-lg text-white font-medium bg-primary hover:bg-primaryHover transition-colors disabled:opacity-50 flex items-center justify-center gap-2"
      >
        {isChecking ? (
          <motion.div
            animate={{ rotate: 360 }}
            transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
            className="w-4 h-4 border-2 border-white border-t-transparent rounded-full"
          />
        ) : (
          <IoCheckmarkCircleOutline className="text-lg" />
        )}
        {isChecking ? "확인 중..." : "인증 완료했어요"}
      </motion.button>

      <div className="text-center">
        <button
          type="button"
          onClick={handleResend}
          disabled={isResending}
          className="text-sm text-gray-400 hover:text-primary transition-colors inline-flex items-center gap-1.5 disabled:opacity-50"
        >
          <IoRefreshOutline className={isResending ? "animate-spin" : ""} />
          {isResending ? "재발송 중..." : "메일을 못 받으셨나요? 재발송"}
        </button>
        {resendMsg && <p className="text-xs text-primary mt-1">{resendMsg}</p>}
      </div>

      {/* 미인증 팝업 */}
      <AnimatePresence>
        {showPopup && (
          <>
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="fixed inset-0 bg-black/40 z-50"
              onClick={() => setShowPopup(false)}
            />
            <motion.div
              initial={{ opacity: 0, scale: 0.9, y: 10 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.9, y: 10 }}
              className="fixed left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 z-50 w-[calc(100%-2rem)] max-w-sm bg-white rounded-2xl shadow-xl p-6 text-center"
            >
              <div className="w-12 h-12 rounded-full bg-amber-100 flex items-center justify-center mx-auto mb-3">
                <IoMailOutline className="text-2xl text-amber-500" />
              </div>
              <h3 className="font-bold text-gray-800 mb-1">메일 인증이 필요합니다</h3>
              <p className="text-sm text-gray-500 mb-5">
                이메일 인증을 완료한 후<br />다시 시도해주세요.
              </p>
              <button
                type="button"
                onClick={() => setShowPopup(false)}
                className="w-full py-2.5 rounded-xl bg-primary text-white text-sm font-semibold hover:bg-primaryHover transition-colors"
              >
                확인
              </button>
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </div>
  );
};

// ─── Stage 3: 비밀번호·닉네임 입력 ───

interface ProfileStageProps {
  email: string;
  onSuccess: () => void;
}

const ProfileStage: FC<ProfileStageProps> = ({ email, onSuccess }) => {
  const setAuth = useAuthStore((state) => state.setAuth);
  const [serverError, setServerError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [nicknameDupError, setNicknameDupError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    getValues,
    formState: { errors },
  } = useForm<ProfileFormValues>({
    resolver: zodResolver(profileSchema),
    defaultValues: { password: "", passwordConfirm: "", nickname: "" },
  });

  const handleNicknameBlur = async () => {
    const nickname = getValues("nickname");
    if (!nickname || nickname.length < AUTH_VALIDATION.NICKNAME_MIN_LENGTH) return;
    try {
      const { exists } = await authApi.checkNickname(nickname);
      setNicknameDupError(exists ? "이미 사용 중인 닉네임입니다." : null);
    } catch { /* ignore */ }
  };

  const onSubmit = async (values: ProfileFormValues) => {
    if (nicknameDupError) return;
    setServerError(null);
    setIsSubmitting(true);
    try {
      const signupResponse = await authApi.signup({
        email,
        password: values.password,
        nickname: values.nickname,
      });
      tokenStorage.setTokens(signupResponse.accessToken, signupResponse.refreshToken);
      const user = await authApi.getMe();
      setAuth(signupResponse, user);
      onSuccess();
    } catch (error) {
      if (isAxiosError(error) && error.response) {
        const status = error.response.status;
        if (status === 409) setServerError("이미 사용 중인 이메일 또는 닉네임입니다.");
        else if (status === 403) setServerError("이메일 인증을 완료한 후 회원가입이 가능합니다.");
        else setServerError("회원가입 중 오류가 발생했습니다. 다시 시도해주세요.");
      } else {
        setServerError("서버에 연결할 수 없습니다.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
      <div className="bg-green-50 border border-green-100 rounded-xl px-4 py-3 flex items-center gap-2">
        <IoCheckmarkCircleOutline className="text-primary text-base flex-shrink-0" />
        <p className="text-sm text-gray-600">
          <span className="font-medium text-primary">{email}</span> 인증 완료
        </p>
      </div>

      <AnimatePresence>
        {serverError && (
          <motion.div animate={shake} className="p-3 rounded-lg text-sm bg-bgError text-error">
            {serverError}
          </motion.div>
        )}
      </AnimatePresence>

      <div>
        <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
          비밀번호
        </label>
        <input
          id="password"
          type="password"
          placeholder="영문, 숫자, 특수문자 포함 8자 이상"
          autoComplete="new-password"
          className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary transition-colors"
          {...register("password")}
        />
        {errors.password && <p className="mt-1 text-sm text-rose">{errors.password.message}</p>}
      </div>

      <div>
        <label htmlFor="passwordConfirm" className="block text-sm font-medium text-gray-700 mb-1">
          비밀번호 확인
        </label>
        <input
          id="passwordConfirm"
          type="password"
          placeholder="비밀번호를 다시 입력해주세요"
          autoComplete="new-password"
          className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary transition-colors"
          {...register("passwordConfirm")}
        />
        {errors.passwordConfirm && <p className="mt-1 text-sm text-rose">{errors.passwordConfirm.message}</p>}
      </div>

      <div>
        <label htmlFor="nickname" className="block text-sm font-medium text-gray-700 mb-1">
          닉네임
        </label>
        <input
          id="nickname"
          type="text"
          placeholder="닉네임 (2~50자)"
          className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary transition-colors"
          {...register("nickname", { onBlur: handleNicknameBlur })}
        />
        {errors.nickname && <p className="mt-1 text-sm text-rose">{errors.nickname.message}</p>}
        {nicknameDupError && <p className="mt-1 text-sm text-error">{nicknameDupError}</p>}
      </div>

      <motion.button
        type="submit"
        disabled={isSubmitting || !!nicknameDupError}
        whileHover={!isSubmitting && !nicknameDupError ? buttonHover : {}}
        whileTap={!isSubmitting && !nicknameDupError ? buttonTap : {}}
        className="w-full py-3 rounded-lg text-white font-medium bg-primary hover:bg-primaryHover transition-colors disabled:opacity-50 flex items-center justify-center gap-2"
      >
        {isSubmitting && (
          <motion.div
            animate={{ rotate: 360 }}
            transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
            className="w-4 h-4 border-2 border-white border-t-transparent rounded-full"
          />
        )}
        {isSubmitting ? "가입 중..." : "회원가입"}
      </motion.button>
    </form>
  );
};

// ─── 단계 표시 ───

const STAGE_ORDER: Stage[] = ["email", "waiting", "profile"];
const STAGE_LABELS: Record<Stage, string> = {
  email: "이메일 입력",
  waiting: "메일 인증",
  profile: "정보 입력",
};

// ─── 메인 컴포넌트 ───

const SignUpForm: FC = () => {
  const navigate = useNavigate();
  const [stage, setStage] = useState<Stage>("email");
  const [verifiedEmail, setVerifiedEmail] = useState("");
  const currentIndex = STAGE_ORDER.indexOf(stage);

  return (
    <div className="w-full max-w-md mx-auto">
      <div className="bg-white rounded-2xl shadow-lg p-8 border border-green-100">
        <h2 className="text-2xl font-bold text-center mb-2 text-primary">회원가입</h2>
        <p className="text-sm text-gray-500 text-center mb-6">새로운 계정을 만들어보세요</p>

        {/* 단계 표시 */}
        <div className="flex items-center justify-between mb-7">
          {STAGE_ORDER.map((s, i) => {
            const isDone = i < currentIndex;
            const isActive = i === currentIndex;
            return (
              <div key={s} className="flex items-center flex-1">
                <div className="flex flex-col items-center">
                  <div
                    className={`w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold transition-colors ${
                      isDone
                        ? "bg-primary text-white"
                        : isActive
                          ? "bg-primary text-white ring-4 ring-primary/20"
                          : "bg-gray-100 text-gray-400"
                    }`}
                  >
                    {isDone ? "✓" : i + 1}
                  </div>
                  <span
                    className={`text-[10px] mt-1 whitespace-nowrap ${
                      isActive ? "text-primary font-semibold" : "text-gray-400"
                    }`}
                  >
                    {STAGE_LABELS[s]}
                  </span>
                </div>
                {i < STAGE_ORDER.length - 1 && (
                  <div
                    className={`flex-1 h-px mx-2 mb-4 ${i < currentIndex ? "bg-primary" : "bg-gray-200"}`}
                  />
                )}
              </div>
            );
          })}
        </div>

        {/* 단계별 콘텐츠 */}
        <AnimatePresence mode="wait">
          {stage === "email" && (
            <motion.div
              key="email"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              transition={{ duration: 0.2 }}
            >
              <EmailStage
                onVerificationSent={(email) => {
                  setVerifiedEmail(email);
                  setStage("waiting");
                }}
              />
            </motion.div>
          )}

          {stage === "waiting" && (
            <motion.div
              key="waiting"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              transition={{ duration: 0.2 }}
            >
              <WaitingStage
                email={verifiedEmail}
                onVerified={() => setStage("profile")}
              />
            </motion.div>
          )}

          {stage === "profile" && (
            <motion.div
              key="profile"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              transition={{ duration: 0.2 }}
            >
              <ProfileStage
                email={verifiedEmail}
                onSuccess={() => navigate("/")}
              />
            </motion.div>
          )}
        </AnimatePresence>

        <p className="mt-6 text-center text-sm text-gray-500">
          이미 계정이 있으신가요?{" "}
          <Link to="/login" className="font-medium hover:underline text-primary">
            로그인
          </Link>
        </p>
      </div>
    </div>
  );
};

export default SignUpForm;
