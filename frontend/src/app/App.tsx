import { type FC, useEffect } from "react";
import AppRouter from "./routes/AppRouter";
import { useAuthStore } from "../domain/auth/store/useAuthStore";
import { authApi } from "../domain/auth/api/authApi";
import { tokenStorage, isTokenExpired } from "../domain/auth/auth.utils";

const App: FC = () => {
  const setAuth = useAuthStore((state) => state.setAuth);
  const setLoading = useAuthStore((state) => state.setLoading);
  const logout = useAuthStore((state) => state.logout);

  useEffect(() => {
    const restoreAuth = async () => {
      const accessToken = tokenStorage.getAccessToken();
      const refreshToken = tokenStorage.getRefreshToken();

      if (!accessToken || !refreshToken) {
        setLoading(false);
        return;
      }

      if (isTokenExpired(accessToken) && isTokenExpired(refreshToken)) {
        logout();
        return;
      }

      try {
        const user = await authApi.getMe();
        setAuth({ accessToken, refreshToken, expiresIn: 0 }, user);
      } catch {
        logout();
      }
    };

    restoreAuth();
  }, [setAuth, setLoading, logout]);

  return <AppRouter />;
};

export default App;
