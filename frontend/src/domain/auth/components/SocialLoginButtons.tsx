import { type FC } from "react";
import { ENV } from "../../../global/config/env";

const SocialLoginButtons: FC = () => {
  const handleSocialLogin = (provider: string) => {
    window.location.href = `${ENV.API_BASE_URL}${ENV.OAUTH2}/${provider}`;
  };

  return (
    <div className="space-y-3">
      <button
        type="button"
        onClick={() => handleSocialLogin("kakao")}
        className="w-full flex items-center justify-center gap-2 px-4 py-3 rounded-lg font-medium transition-colors"
        style={{ backgroundColor: "#FEE500", color: "#191919" }}
      >
        <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
          <path
            d="M9 1C4.58 1 1 3.79 1 7.21c0 2.17 1.45 4.09 3.64 5.18-.16.56-.58 2.03-.66 2.35-.1.39.14.39.3.28.12-.08 1.94-1.31 2.73-1.85.64.09 1.3.14 1.99.14 4.42 0 8-2.79 8-6.21S13.42 1 9 1Z"
            fill="#191919"
          />
        </svg>
        카카오로 시작하기
      </button>

      <button
        type="button"
        onClick={() => handleSocialLogin("naver")}
        className="w-full flex items-center justify-center gap-2 px-4 py-3 rounded-lg font-medium text-white transition-colors"
        style={{ backgroundColor: "#03C75A" }}
      >
        <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
          <path
            d="M12.13 9.72L5.61 1H1v16h4.87V9.28L12.39 17H17V1h-4.87v8.72Z"
            fill="white"
          />
        </svg>
        네이버로 시작하기
      </button>

      <button
        type="button"
        onClick={() => handleSocialLogin("google")}
        className="w-full flex items-center justify-center gap-2 px-4 py-3 rounded-lg font-medium border border-gray-300 text-gray-700 bg-white hover:bg-gray-50 transition-colors"
      >
        <svg width="18" height="18" viewBox="0 0 18 18">
          <path
            d="M17.64 9.2c0-.637-.057-1.251-.164-1.84H9v3.481h4.844a4.14 4.14 0 01-1.796 2.716v2.259h2.908c1.702-1.567 2.684-3.875 2.684-6.615z"
            fill="#4285F4"
          />
          <path
            d="M9 18c2.43 0 4.467-.806 5.956-2.184l-2.908-2.259c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332A8.997 8.997 0 009 18z"
            fill="#34A853"
          />
          <path
            d="M3.964 10.706A5.41 5.41 0 013.682 9c0-.593.102-1.17.282-1.706V4.962H.957A8.996 8.996 0 000 9c0 1.452.348 2.827.957 4.038l3.007-2.332z"
            fill="#FBBC05"
          />
          <path
            d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0A8.997 8.997 0 00.957 4.962L3.964 7.294C4.672 5.163 6.656 3.58 9 3.58z"
            fill="#EA4335"
          />
        </svg>
        Google로 시작하기
      </button>
    </div>
  );
};

export default SocialLoginButtons;
