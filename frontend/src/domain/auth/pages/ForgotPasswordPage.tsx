import { type FC, useState } from "react";
import { Link } from "react-router-dom";
import { isAxiosError } from "axios";
import { authApi } from "../api/authApi";

const ForgotPasswordPage: FC = () => {
  const [email, setEmail] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isDone, setIsDone] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      await authApi.requestPasswordReset(email);
      setIsDone(true);
    } catch (err) {
      if (isAxiosError(err)) {
        setError("요청 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
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
              <h2 className="text-xl font-bold text-gray-800 mb-2">메일이 발송되었습니다</h2>
              <p className="text-sm text-gray-500 mb-6">
                <span className="font-medium text-gray-700">{email}</span>로<br />
                비밀번호 재설정 링크를 보냈습니다.<br />
                메일함을 확인해주세요. (유효시간 1시간)
              </p>
              <Link to="/login" className="text-sm text-primary hover:underline font-medium">
                로그인으로 돌아가기
              </Link>
            </div>
          ) : (
            <>
              <h2 className="text-2xl font-bold text-center mb-2 text-primary">
                비밀번호 찾기
              </h2>
              <p className="text-sm text-gray-500 text-center mb-8">
                가입한 이메일을 입력하면 재설정 링크를 보내드립니다
              </p>

              {error && (
                <div className="mb-4 p-3 rounded-lg text-sm bg-bgError text-error">
                  {error}
                </div>
              )}

              <form onSubmit={handleSubmit} className="space-y-5">
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
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="example@email.com"
                    required
                    className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary transition-colors"
                  />
                </div>
                <button
                  type="submit"
                  disabled={isSubmitting || !email}
                  className="w-full py-3 rounded-lg text-white font-medium bg-primary hover:bg-primaryHover transition-colors disabled:opacity-50"
                >
                  {isSubmitting ? "발송 중..." : "재설정 링크 발송"}
                </button>
              </form>

              <div className="mt-6 text-center">
                <Link to="/login" className="text-sm text-gray-400 hover:underline">
                  로그인으로 돌아가기
                </Link>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default ForgotPasswordPage;
