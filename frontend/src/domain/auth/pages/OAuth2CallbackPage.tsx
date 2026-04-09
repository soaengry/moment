import { type FC, useEffect, useRef, useState } from "react";
import { useNavigate, useSearchParams, Link } from "react-router-dom";
import { useAuthStore } from "../store/useAuthStore";
import { authApi } from "../api/authApi";
import { tokenStorage } from "../auth.utils";

const OAuth2CallbackPage: FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const setAuth = useAuthStore((state) => state.setAuth);
  const [error, setError] = useState<string | null>(null);
  const hasRun = useRef(false);

  useEffect(() => {
    if (hasRun.current) return;
    hasRun.current = true;

    const code = searchParams.get("code");
    const errorParam = searchParams.get("error");
    const errorDescription = searchParams.get("error_description");

    if (errorParam) {
      let message = "소셜 로그인에 실패했습니다.";
      switch (errorParam) {
        case "access_denied":
          message = "로그인이 취소되었습니다.";
          break;
        case "server_error":
          message = "서버 오류가 발생했습니다.";
          break;
        default:
          if (errorDescription) message = errorDescription;
      }
      setTimeout(() => setError(message), 0);

      return;
    }

    if (!code) {
      setTimeout(() => setError("인증 정보를 받지 못했습니다."), 0);
      return;
    }

    const processLogin = async () => {
      try {
        const { accessToken, refreshToken, expiresIn } =
          await authApi.exchangeOAuth2Token(code);
        tokenStorage.setTokens(accessToken, refreshToken);
        const user = await authApi.getMe();
        setAuth(
          { accessToken, refreshToken, expiresIn: Number(expiresIn) || 0 },
          user,
        );
        navigate("/", { replace: true });
      } catch {
        tokenStorage.clearTokens();
        setError("사용자 정보를 불러올 수 없습니다.");
      }
    };

    processLogin();
  }, [searchParams, navigate, setAuth]);

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center px-4 bg-bgPrimary">
        <div className="w-full max-w-md mx-auto">
          <div className="bg-white rounded-2xl shadow-lg p-8 border border-green-100 text-center">
            <div className="w-14 h-14 rounded-full flex items-center justify-center mx-auto mb-4 bg-red-100">
              <span className="text-error text-2xl">✕</span>
            </div>
            <h2 className="text-xl font-bold mb-2 text-error-500">
              로그인 실패
            </h2>
            <p className="text-sm text-gray-500 mb-6">{error}</p>
            <Link
              to="/login"
              className="inline-block px-6 py-2.5 rounded-lg bg-primary hover:bg-primaryHover text-white font-medium"
            >
              로그인으로 이동
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div
      className="min-h-screen flex items-center justify-center"
      style={{ backgroundColor: "#FAFFF4" }}
    >
      <div className="text-center">
        <div
          className="w-12 h-12 border-4 border-t-transparent rounded-full animate-spin mx-auto mb-4"
          style={{ borderColor: "#88AF64", borderTopColor: "transparent" }}
        />
        <p className="text-gray-600 font-medium">로그인 처리 중...</p>
      </div>
    </div>
  );
};

export default OAuth2CallbackPage;
