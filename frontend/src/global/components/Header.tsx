import { type FC, useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuthStore } from "../../domain/auth/store/useAuthStore";
import { authApi } from "../../domain/auth/api/authApi";

const Header: FC = () => {
  const user = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);
  const navigate = useNavigate();

  const [isVisible, setIsVisible] = useState(true);
  const [lastScrollY, setLastScrollY] = useState(0);

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

  useEffect(() => {
    const handleScroll = () => {
      const currentScrollY = window.scrollY;

      if (currentScrollY > lastScrollY) {
        // 스크롤 내림 → 헤더 숨김
        setIsVisible(false);
      } else {
        // 스크롤 올림 → 헤더 보임
        setIsVisible(true);
      }

      setLastScrollY(currentScrollY);
    };

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, [lastScrollY]);

  return (
    <header
      className={`fixed top-0 left-0 w-full bg-white shadow-sm border-b border-green-100 transition-transform duration-300 ${
        isVisible ? "translate-y-0" : "-translate-y-full"
      }`}
    >
      <div className="max-w-5xl mx-auto px-4 py-3 flex items-center justify-between">
        <Link
          to="/"
          className="text-xl font-bold text-primary select-none cursor-pointer"
        >
          MOMENT
        </Link>

        <div className="flex items-center gap-4">
          {user && (
            <>
              <div
                className="flex items-center gap-2 cursor-pointer hover:opacity-80 transition"
                onClick={() => navigate("/my-page")}
              >
                <img
                  src={user.profileImageUrl || "/default-avatar.png"}
                  alt="프로필"
                  className="w-8 h-8 rounded-full object-cover border border-gray-200"
                />
                <span className="text-sm text-gray-600">
                  <span className="font-semibold mr-1">{user.nickname}</span>님
                </span>
              </div>{" "}
              <button
                onClick={handleLogout}
                className="text-sm px-3 py-1.5 rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50 transition-colors"
              >
                로그아웃
              </button>
            </>
          )}
          {!user && (
            <Link
              to="/login"
              className="text-sm px-3 py-1.5 rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50 transition-colors"
            >
              로그인
            </Link>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;
