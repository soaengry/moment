import { type FC, useState, useEffect } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { isAxiosError } from "axios";
import { authApi } from "../api/authApi";

const PASSWORD_PATTERN =
  /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[~!@#$%^&*()_+<>?,./-=]).{8,}$/;

const ResetPasswordPage: FC = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token") ?? "";

  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isDone, setIsDone] = useState(false);
  const [countdown, setCountdown] = useState(3);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isDone) return;
    if (countdown === 0) {
      window.close();
      return;
    }
    const timer = setTimeout(() => setCountdown((c) => c - 1), 1000);
    return () => clearTimeout(timer);
  }, [isDone, countdown]);

  if (!token) {
    return (
      <div className="min-h-screen flex items-center justify-center px-4 bg-bgPrimary">
        <div className="text-center">
          <p className="text-gray-500 mb-2">유효하지 않은 링크입니다.</p>
          <Link to="/login" className="text-sm text-primary hover:underline">
            로그인으로 돌아가기
          </Link>
        </div>
      </div>
    );
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!PASSWORD_PATTERN.test(newPassword)) {
      setError("비밀번호는 8자 이상, 영문 대소문자·숫자·특수문자를 포함해야 합니다.");
      return;
    }
    if (newPassword !== confirmPassword) {
      setError("비밀번호가 일치하지 않습니다.");
      return;
    }

    setIsSubmitting(true);
    try {
      await authApi.confirmPasswordReset(token, newPassword);
      setIsDone(true);
    } catch (err) {
      if (isAxiosError(err) && err.response) {
        const status = err.response.status;
        if (status === 404) {
          setError("유효하지 않은 재설정 링크입니다.");
        } else if (status === 401) {
          setError("링크가 만료되었거나 이미 사용된 링크입니다.");
        } else {
          setError("비밀번호 재설정 중 오류가 발생했습니다.");
        }
      } else {
        setError("서버에 연결할 수 없습니다.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-4 bg-bgPrimary">
      <div className="w-full max-w-md mx-auto">
        <div className="text-center mb-8">
          <Link to="/" className="text-3xl font-bold text-primary tracking-widest select-none">
            MOMENT
          </Link>
        </div>
        <div className="bg-white rounded-2xl shadow-lg p-8 border border-green-100">
          {isDone ? (
            <div className="text-center">
              <div className="w-14 h-14 bg-bgSuccess rounded-full flex items-center justify-center mx-auto mb-4 text-success text-2xl font-bold">
                ✓
              </div>
              <h2 className="text-xl font-bold text-gray-800 mb-2">
                비밀번호가 재설정되었습니다
              </h2>
              <p className="text-sm text-gray-400">
                {countdown}초 후 창이 닫힙니다
              </p>
            </div>
          ) : (
            <>
              <h2 className="text-2xl font-bold text-center mb-2 text-primary">
                비밀번호 재설정
              </h2>
              <p className="text-sm text-gray-500 text-center mb-8">
                새로운 비밀번호를 입력해주세요
              </p>

              {error && (
                <div className="mb-4 p-3 rounded-lg text-sm bg-bgError text-error">
                  {error}
                </div>
              )}

              <form onSubmit={handleSubmit} className="space-y-5">
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
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    placeholder="8자 이상, 대소문자·숫자·특수문자 포함"
                    className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary transition-colors"
                  />
                </div>
                <div>
                  <label
                    htmlFor="confirmPassword"
                    className="block text-sm font-medium text-gray-700 mb-1"
                  >
                    비밀번호 확인
                  </label>
                  <input
                    id="confirmPassword"
                    type="password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    placeholder="비밀번호를 다시 입력해주세요"
                    className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary transition-colors"
                  />
                </div>
                <button
                  type="submit"
                  disabled={isSubmitting || !newPassword || !confirmPassword}
                  className="w-full py-3 rounded-lg text-white font-medium bg-primary hover:bg-primaryHover transition-colors disabled:opacity-50"
                >
                  {isSubmitting ? "처리 중..." : "비밀번호 재설정"}
                </button>
              </form>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default ResetPasswordPage;
