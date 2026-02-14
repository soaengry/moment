import { type FC, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { authApi } from "../api/authApi";
import { AUTH_VALIDATION } from "../auth.constants";
import { isAxiosError } from "axios";

const restoreSchema = z.object({
  email: z
    .string()
    .min(1, "이메일을 입력해주세요.")
    .regex(AUTH_VALIDATION.EMAIL_REGEX, "올바른 이메일 형식이 아닙니다."),
  password: z.string().min(1, "비밀번호를 입력해주세요."),
});

type RestoreFormValues = z.infer<typeof restoreSchema>;

const RestoreAccountPage: FC = () => {
  const navigate = useNavigate();
  const [serverError, setServerError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RestoreFormValues>({
    resolver: zodResolver(restoreSchema),
    defaultValues: { email: "", password: "" },
  });

  const onSubmit = async (values: RestoreFormValues) => {
    setServerError(null);
    setIsSubmitting(true);

    try {
      await authApi.restoreAccount(values);
      setSuccessMessage("계정이 복구되었습니다. 로그인해주세요.");
      setTimeout(() => navigate("/login"), 2000);
    } catch (error: unknown) {
      if (isAxiosError(error) && error.response) {
        const status = error.response.status;
        if (status === 404) {
          setServerError("복구 가능한 계정이 없습니다.");
        } else if (status === 401) {
          setServerError("비밀번호가 일치하지 않습니다.");
        } else {
          setServerError("계정 복구 중 오류가 발생했습니다.");
        }
      } else {
        setServerError("서버에 연결할 수 없습니다.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-4 bg-bgPrimary">
      <div className="w-full max-w-md mx-auto">
        <div className="bg-white rounded-2xl shadow-lg p-8 border border-green-100">
          <h2 className="text-2xl font-bold text-center mb-2 text-primary">
            계정 복구
          </h2>
          <p className="text-sm text-gray-500 text-center mb-8">
            탈퇴한 계정을 30일 이내에 복구할 수 있습니다
          </p>

          {serverError && (
            <div className="mb-4 p-3 rounded-lg text-sm bg-bgDanger text-danger">
              {serverError}
            </div>
          )}

          {successMessage && (
            <div className="mb-4 p-3 rounded-lg text-sm bg-bgSuccess text-success">
              {successMessage}
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
                className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 transition-colors"
                {...register("email")}
              />
              {errors.email && (
                <p className="mt-1 text-sm text-danger">
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
                <p className="mt-1 text-sm text-danger">
                  {errors.password.message}
                </p>
              )}
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full py-3 rounded-lg bg-primary hover:bg-primaryHover text-white font-medium transition-opacity disabled:opacity-50"
            >
              {isSubmitting ? "복구 중..." : "계정 복구"}
            </button>
          </form>

          <p className="mt-6 text-center text-sm text-gray-500">
            <Link to="/login" className="text-primary hover:underline">
              로그인으로 돌아가기
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default RestoreAccountPage;
