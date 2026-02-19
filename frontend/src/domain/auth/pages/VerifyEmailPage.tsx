import { type FC, useState, useEffect } from "react";
import { useNavigate, useSearchParams, Link } from "react-router-dom";
import { authApi } from "../api/authApi";

const VerifyEmailPage: FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");

  const [status, setStatus] = useState<"loading" | "success" | "error">(
    "loading",
  );
  const [message, setMessage] = useState("");

  useEffect(() => {
    const verify = async () => {
      try {
        if (!token) {
          setStatus("error");
          setMessage("유효하지 않은 인증 링크입니다.");
          return;
        }

        const response = await authApi.verifyEmail(token);
        setStatus("success");
        setMessage(response.message || "이메일 인증이 완료되었습니다.");
        setTimeout(() => window.close(), 1000);
      } catch {
        setStatus("error");
        setMessage(
          "인증에 실패했습니다. 링크가 만료되었거나 이미 인증된 계정입니다.",
        );
      }
    };

    verify();
  }, [token, navigate]);

  return (
    <div className="min-h-screen flex items-center justify-center px-4 bg-bgPrimary">
      <div className="w-full max-w-md mx-auto">
        <div className="bg-white rounded-2xl shadow-lg p-8 border border-green-100 text-center">
          {status === "loading" && (
            <>
              <div className="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-4" />
              <p className="text-gray-600">이메일 인증 중...</p>
            </>
          )}

          {status === "success" && (
            <>
              <div className="w-14 h-14 rounded-full flex items-center justify-center mx-auto mb-4 bg-primary hover:bg-primaryHover">
                <span className="text-white text-2xl">✓</span>
              </div>
              <h2 className="text-xl font-bold mb-2 text-primary">인증 완료</h2>
              <p className="text-sm text-gray-500 mb-4">{message}</p>
              <p className="text-xs text-gray-400">
                잠시 후 로그인 페이지로 이동합니다...
              </p>
            </>
          )}

          {status === "error" && (
            <>
              <div className="w-14 h-14 rounded-full flex items-center justify-center mx-auto mb-4 bg-red-100">
                <span className="text-red-500 text-2xl">✕</span>
              </div>
              <h2 className="text-xl font-bold mb-2 text-red-500">인증 실패</h2>
              <p className="text-sm text-gray-500 mb-6">{message}</p>
              <Link
                to="/login"
                className="inline-block px-6 py-2.5 rounded-lg text-white font-medium bg-primary hover:bg-primaryHover"
              >
                로그인으로 이동
              </Link>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default VerifyEmailPage;
