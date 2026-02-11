import { type FC, type ReactNode } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "../../domain/auth/store/useAuthStore";
import { authApi } from "../../domain/auth/api/authApi";

interface LayoutProps {
  children: ReactNode;
}

const Layout: FC<LayoutProps> = ({ children }) => {
  const user = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);
  const navigate = useNavigate();

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

  return (
    <div className="min-h-screen" style={{ backgroundColor: "#FAFFF4" }}>
      <header className="bg-white shadow-sm border-b border-green-100">
        <div className="max-w-5xl mx-auto px-4 py-3 flex items-center justify-between">
          <h1
            className="text-xl font-bold cursor-pointer"
            style={{ color: "#88AF64" }}
            onClick={() => navigate("/")}
          >
            Moment
          </h1>

          <div className="flex items-center gap-4">
            {user && (
              <span className="text-sm text-gray-600">{user.nickname}님</span>
            )}
            <button
              onClick={handleLogout}
              className="text-sm px-3 py-1.5 rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50 transition-colors"
            >
              로그아웃
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-5xl mx-auto px-4 py-6">{children}</main>
    </div>
  );
};

export default Layout;
