import { type FC, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "../../auth/store/useAuthStore";
import { authApi } from "../../auth/api/authApi";

const MyPage: FC = () => {
  const navigate = useNavigate();
  const user = useAuthStore((state) => state.user);
  const setUser = useAuthStore((state) => state.setUser);
  const logout = useAuthStore((state) => state.logout);
  const [verifyMessage, setVerifyMessage] = useState<string | null>(null);
  const [isSending, setIsSending] = useState(false);

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const data = await authApi.getMe();
        setUser(data);
      } catch {
        // 인터셉터에서 401 처리
      }
    };
    fetchUser();
  }, [setUser]);

  const handleLogout = async () => {
    try {
      await authApi.logout();
    } catch {
      // 서버 로그아웃 실패해도 클라이언트 로그아웃 진행
    } finally {
      logout();
      navigate("/login");
    }
  };

  const handleSendVerification = async () => {
    if (!user) return;
    setIsSending(true);
    setVerifyMessage(null);
    try {
      await authApi.resendVerification(user.email);
      setVerifyMessage("인증 메일이 발송되었습니다. 이메일을 확인해주세요.");
    } catch {
      setVerifyMessage("인증 메일 발송에 실패했습니다.");
    } finally {
      setIsSending(false);
    }
  };

  if (!user) return null;

  return (
    <div className="max-w-lg mx-auto">
      <h2 className="text-2xl font-bold mb-6 text-primary">마이페이지</h2>

      <div className="bg-white rounded-2xl shadow-lg p-6 border border-green-100">
        <div className="flex items-center gap-4 mb-6">
          <img
            src={user.profileImageUrl ?? "/default-avatar.png"}
            alt="프로필"
            className="w-16 h-16 rounded-full object-cover bg-gray-100"
          />
          <div>
            <p className="text-lg font-semibold text-gray-800">
              {user.nickname}
            </p>
            <p className="text-sm text-gray-500">{user.email}</p>
          </div>
        </div>

        {verifyMessage && (
          <div className="mb-4 p-3 rounded-lg text-sm bg-bgSuccess text-success">
            {verifyMessage}
          </div>
        )}

        <div className="space-y-3 text-sm text-gray-600 mb-6">
          <div className="flex justify-between items-center py-2 border-b border-gray-100">
            <span>이메일 인증</span>
            {user.isEmailVerified ? (
              <span className="text-green-600">✅ 인증됨</span>
            ) : (
              <button
                onClick={handleSendVerification}
                disabled={isSending}
                className="px-3 py-1 rounded-md text-xs font-medium text-white disabled:opacity-50 bg-gold hover:bg-goldHover"
              >
                {isSending ? "발송 중..." : "인증 메일 발송"}
              </button>
            )}
          </div>
          <div className="flex justify-between py-2 border-b border-gray-100">
            <span>가입 방법</span>
            <span>{user.authProvider}</span>
          </div>
          <div className="flex justify-between py-2 border-b border-gray-100">
            <span>가입일</span>
            <span>{new Date(user.createdAt).toLocaleDateString("ko-KR")}</span>
          </div>
        </div>

        <div className="space-y-2">
          <button
            onClick={() => navigate("/edit-profile")}
            className="w-full py-2.5 rounded-lg text-white font-medium bg-primary hover:bg-primaryHover"
          >
            프로필 수정
          </button>
          <button
            onClick={() => navigate("/change-password")}
            className="w-full py-2.5 rounded-lg border border-gray-200 text-gray-600 font-medium hover:bg-gray-50 transition-colors"
          >
            비밀번호 변경
          </button>
          <button
            onClick={handleLogout}
            className="w-full py-2.5 rounded-lg border border-gray-200 text-gray-600 font-medium hover:bg-gray-50 transition-colors"
          >
            로그아웃
          </button>
          <button
            onClick={() => navigate("/delete-account")}
            className="w-full py-2.5 rounded-lg text-red-400 font-medium hover:bg-red-50 transition-colors"
          >
            회원 탈퇴
          </button>
        </div>
      </div>
    </div>
  );
};

export default MyPage;
